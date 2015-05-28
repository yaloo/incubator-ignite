/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.ignite.internal.processors.hadoop.fs;

import org.apache.ignite.*;
import org.apache.ignite.internal.*;
import org.apache.ignite.internal.util.future.*;
// TODO: Remove unused
import org.apache.ignite.internal.util.typedef.internal.*;
import org.jetbrains.annotations.*;
import org.jsr166.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

/**
 * Maps values by keys.
 * Values are created lazily using {@link ValueFactory}.
 * Currently only {@link #clear()} method can remove a value.
 *
 * Despite of the name, does not depend on any Hadoop classes.
 */
public class HadoopLazyConcurrentMap<K, V> {
    /** The map storing the actual values. */
    private final ConcurrentMap<K, ValueWrapper> map = new ConcurrentHashMap8<>();

    /** The factory passed in by the client. Will be used for lazy value creation. */
    private final ValueFactory<K, V> factory;

    /**
     * Constructor.
     * @param factory the factory to create new values lazily.
     */
    public HadoopLazyConcurrentMap(ValueFactory<K, V> factory) {
        this.factory = factory;
    }

    /**
     * Gets cached or creates a new value of V.
     * Never returns null.
     * @param k the key to associate the value with.
     * @return the cached or newly created value, never null.
     * @throws IgniteException on error
     */
    public V getOrCreate(K k) {
        // TODO: Do "get" first.
        final ValueWrapper wNew = new ValueWrapper(k);

        ValueWrapper w = map.putIfAbsent(k, wNew);

        if (w == null) {
            // new wrapper 'w' has been put, so init the value:
            wNew.init();

            w = wNew;
        }

        try {
            V v = w.getValue();

            assert v != null;

            return v;
        }
        catch (IgniteCheckedException ie) {
            throw new IgniteException(ie);
        }
    }

    /**
     * Gets the value without any attempt to create a new one.
     * @param k the key
     * @return the value, or null if there is no value for this key.
     */
    public @Nullable V get(K k) {
        ValueWrapper w = map.get(k);

        if (w == null)
            return null;

        try {
            return w.getValue();
        }
        catch (IgniteCheckedException ie) {
            throw new IgniteException(ie);
        }
    }

    /**
     * Gets the keySet of this map,
     * the contract is as per {@link ConcurrentMap#keySet()}
     * @return the set of keys, never null.
     */
    public Set<K> keySet() {
        return map.keySet();
    }

    /**
     * Clears the map.
     * Follows the contract of {@link ConcurrentMap#clear()}
     */
    public void clear() {
        map.clear();
    }

    /**
     * Helper class that drives the lazy value creation.
     */
    private class ValueWrapper {
        /** Future. */
        private final GridFutureAdapter<V> fut = new GridFutureAdapter<>();

        /** the key */
        private final K key;

        /**
         * Creates new wrapper.
         */
        private ValueWrapper(K key) {
            this.key = key;
        }

        /**
         * Initializes the value using the factory.
         */
        private void init() {
            try {
                final V v0 = factory.createValue(key);

                if (v0 == null)
                    throw new IgniteException("Failed to create non-null value. [key=" + key + ']');

                fut.onDone(v0);
            }
            catch (Throwable e) {
                fut.onDone(e);
            }
        }

        /**
         * Blocks until the value is initialized.
         * @return the value
         * @throws IgniteInterruptedCheckedException if interrupted during wait.
         */
        @Nullable V getValue() throws IgniteCheckedException {
            return fut.get();
        }
    }

    /**
     * Interface representing the factory that creates map values.
     * @param <K> the type of the key.
     * @param <V> the type of the value.
     */
    public interface ValueFactory <K, V> {
        /**
         * Creates the new value. Must never return null.
         *
         * @param key the key to create value for
         * @return the value.
         * @throws IgniteException on failure.
         */
        public V createValue(K key);
    }
}