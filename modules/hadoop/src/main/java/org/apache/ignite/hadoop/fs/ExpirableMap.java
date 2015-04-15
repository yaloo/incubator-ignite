package org.apache.ignite.hadoop.fs;

import org.apache.ignite.*;
import org.jetbrains.annotations.*;
import org.jsr166.*;

import java.lang.ref.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * Maps values by keys.
 * Uses affective strategy of object caching:
 * Values are lazily created via special {@link ValueFactory};
 *
 * When a value is not used longer than 'expirationTime', the value gets held by {@link WeakReference}, and may
 * disappear from the map, if it is no longer strongly reachable from the client code.
 * The values must implement {@link AccessTimeAware} interface in order to give information about last access
 * time to a value object.
 *
 * If a value has expired and has been removed from the map, the method {@link #get} will return null,
 * unless method {@link #getOrCreate} is invoked, which will create a new value and map it to the key again.
 */
public class ExpirableMap<K, T extends ExpirableMap.AccessTimeAware> {

    private final ConcurrentMap<K, Wrapper> map = new ConcurrentHashMap8<>();

    private final ValueFactory<K, T> factory;

    private final long expirationTimeMs;

    private final ReferenceQueue<T> refQueue = new ReferenceQueue<>();

    public ExpirableMap(ValueFactory<K, T> factory, final long expirationTimeMs) {
        this.factory = factory;

        this.expirationTimeMs = expirationTimeMs;

        // Expiration checker thread:
        Thread t = new Thread(new Runnable() {
            @Override public void run() {
                while (true) {
                    System.out.println("checking expiration.");
                    updateExpiration();
                    removeStale();
                    try {
                        Thread.sleep(expirationTimeMs);
                    } catch (InterruptedException ie) {
                        break;
                    }
                }
            }
        });
        t.setName("ExpirableMap expiration checker " + Integer.toHexString(this.hashCode()));
        t.setDaemon(true);
        t.start();
    }

    void updateExpiration() {
        for (Wrapper w: map.values()) {
            if (w != null)
                w.checkExpired(expirationTimeMs);
        }
    }

    /**
     * Gets cached or creates a new value of V.
     * @param k the key to associate the value with.
     * @return the cached or newly created value, never null.
     * @throws IgniteCheckedException on error
     */
    public T getOrCreate(K k) throws IgniteCheckedException {
        final Wrapper w = new Wrapper(k);

        try {
            while (true) {
                Wrapper wOld = map.putIfAbsent(k, w);

                if (wOld == null) {
                    // new wrapper 'w' has been put:
                    w.init();

                    return w.getValue();
                }
                else {
                    // get the value from existing wrapper:
                    T v = wOld.getValue();

                    if (v != null)
                        return v; // value found in the old wrapper.

                    // The value expired and possibly destroyed.
                    // We have to replace the wrapper with a new one:
                    if (map.replace(k, wOld, w)) {
                        w.init();

                        return w.getValue();
                    }
                    // Somebody already replaced the wrapper, loop again.
                }
            }
        }
        catch (InterruptedException ie) {
            throw new IgniteException(ie);
        }
    }

    public @Nullable T get(K k) {
        Wrapper w = map.get(k);

        if (w == null)
            return null;

        try {
            return w.getValue();
        } catch (InterruptedException ie) {
            throw new IgniteException(ie);
        }
    }

    public Set<K> keySet() {
        return map.keySet();
    }

    private class Wrapper {

        private final CountDownLatch latch = new CountDownLatch(1);

        private final K key;

        private volatile T v;

        private DataWeakReference<Wrapper, T> weakRef;

        private Wrapper(K key) {
            this.key = key;
        }

        private void init() throws IgniteCheckedException {
            final T v0 = factory.createValue(key);

            if (v0 == null)
                throw new IgniteCheckedException("Failed to create value. [key=" + key + ']');

            weakRef = new DataWeakReference<>(this, v0, refQueue);

            v = v0;

            latch.countDown();
        }

        /**
         * Blocks until the value is initialized.
         * @return
         * @throws InterruptedException
         */
        @Nullable T getValue() throws InterruptedException {
            latch.await();

            T v0 = v;

            if (v0 != null)
                return v0;

            // Value may be not reachable strongly (expired), but may still be reachable weakly:
            return weakRef.get();
        }

        void checkExpired(long expirationTimeMs) {
            T v0 = v;

            if (v0 == null) // The value is already expired:
                return;

            long a = v0.accessTimeMs();

            long usedAgo = System.currentTimeMillis() - a;

            if (usedAgo >= expirationTimeMs) {
                v = null; // null the strong reference; 'v' remains only weakly reachable.

                System.out.println("expired: " + v0 );
            }
        }
    }

    void removeStale() {
        DataWeakReference<Wrapper,T> ref;

        while ((ref = (DataWeakReference<Wrapper,T>)refQueue.poll()) != null) {
            Wrapper w = ref.getData();

            K key = w.key;

            boolean removed = map.remove(key, w);

            System.out.println("dequeued: " + ref + " -> " + ref.get() + " removed: " + removed);
        }
    }

    public static interface AccessTimeAware {
        public long accessTimeMs();
    }

    /**
     * Interface representing the factory that creates map values.
     * @param <K>
     * @param <V>
     */
    public interface ValueFactory <K, V> {
        /**
         * Creates the new value.
         * @param key
         * @return
         * @throws IgniteCheckedException
         */
        public V createValue(K key) throws IgniteCheckedException;
    }

    /**
     * Weak reference with an associated data object.
     * @param <D> type of the data object.
     * @param <V> type of the Reference referent.
     */
    private static class DataWeakReference <D, V> extends WeakReference<V> {
        /** The data object. */
        private final D data;

        /**
         * Guess, what is this?? Yes, this is Constructor!
         * @param data
         * @param referent
         * @param q the reference refQueue to refQueue the reference into.
         */
        DataWeakReference(D data, V referent, ReferenceQueue q) {
            super(referent, q);
            this.data = data;
        }

        /**
         * Getter for the data object.
         */
        D getData() {
            return data;
        }
    }
}
