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
package org.apache.ignite.internal.processors.cache.distributed;

import org.apache.ignite.*;
import org.apache.ignite.cache.*;
import org.apache.ignite.configuration.*;
import org.apache.ignite.internal.*;
import org.apache.ignite.internal.processors.cache.*;
import org.apache.ignite.internal.util.lang.*;
import org.apache.ignite.testframework.*;
import org.apache.ignite.testframework.junits.common.*;
import org.apache.ignite.transactions.*;

import java.util.concurrent.*;
import java.util.concurrent.locks.*;

import static org.apache.ignite.transactions.TransactionConcurrency.*;
import static org.apache.ignite.transactions.TransactionIsolation.*;

/**
 * Test checks that atomic operations are not blocked when invoked inside transaction.
 */
@SuppressWarnings({"unchecked", "TypeMayBeWeakened"})
public class IgniteCacheAtomicOpWithinTxSelfTest extends GridCommonAbstractTest {
    /** */
    private static final String TRANSACTIONAL_CACHE = "tx";

    /** */
    private static final String ATOMIC_CACHE = "atomic";

    /** */
    private static final int NODE_COUNT = 3;

    /** */
    private CacheWriteSynchronizationMode syncMode;

    /** */
    private CacheAtomicWriteOrderMode orderMode;

    /** {@inheritDoc} */
    @Override protected void beforeTestsStarted() throws Exception {
        super.beforeTestsStarted();

        startGridsMultiThreaded(NODE_COUNT);
    }

    /** {@inheritDoc} */
    @Override protected void afterTestsStopped() throws Exception {
        stopAllGrids();
    }

    /**
     *
     */
    private void initCaches() {
        CacheConfiguration txCfg = new CacheConfiguration(TRANSACTIONAL_CACHE);

        txCfg.setAtomicityMode(CacheAtomicityMode.TRANSACTIONAL);
        txCfg.setBackups(1);

        grid(0).createCache(txCfg);

        CacheConfiguration atomicCfg = new CacheConfiguration(ATOMIC_CACHE);

        atomicCfg.setBackups(1);
        atomicCfg.setWriteSynchronizationMode(syncMode);
        atomicCfg.setAtomicWriteOrderMode(orderMode);

        grid(0).createCache(atomicCfg);
    }

    /**
     *
     */
    private void destroyCaches() {
        grid(0).destroyCache(TRANSACTIONAL_CACHE);
        grid(0).destroyCache(ATOMIC_CACHE);
    }

    /**
     * @throws Exception If failed.
     */
    public void testAtomicOpTxPrimarySyncPrimaryNodeJoined() throws Exception {
        syncMode = CacheWriteSynchronizationMode.PRIMARY_SYNC;
        orderMode = CacheAtomicWriteOrderMode.PRIMARY;

        checkAtomicOpTx(true);
    }

    /**
     * @throws Exception If failed.
     */
    public void testAtomicOpTxPrimarySyncClockNodeJoined() throws Exception {
        syncMode = CacheWriteSynchronizationMode.PRIMARY_SYNC;
        orderMode = CacheAtomicWriteOrderMode.CLOCK;

        checkAtomicOpTx(true);
    }

    /**
     * @throws Exception If failed.
     */
    public void testAtomicOpTxFullSyncPrimaryNodeJoined() throws Exception {
        syncMode = CacheWriteSynchronizationMode.FULL_SYNC;
        orderMode = CacheAtomicWriteOrderMode.PRIMARY;

        checkAtomicOpTx(true);
    }

    /**
     * @throws Exception If failed.
     */
    public void testAtomicOpTxFullSyncClockNodeJoined() throws Exception {
        syncMode = CacheWriteSynchronizationMode.FULL_SYNC;
        orderMode = CacheAtomicWriteOrderMode.CLOCK;

        checkAtomicOpTx(true);
    }

    /**
     * @throws Exception If failed.
     */
    public void testAtomicOpTxPrimarySyncPrimary() throws Exception {
        syncMode = CacheWriteSynchronizationMode.PRIMARY_SYNC;
        orderMode = CacheAtomicWriteOrderMode.PRIMARY;

        checkAtomicOpTx(false);
    }

    /**
     * @throws Exception If failed.
     */
    public void testAtomicOpTxPrimarySyncClock() throws Exception {
        syncMode = CacheWriteSynchronizationMode.PRIMARY_SYNC;
        orderMode = CacheAtomicWriteOrderMode.CLOCK;

        checkAtomicOpTx(false);
    }

    /**
     * @throws Exception If failed.
     */
    public void testAtomicOpTxFullSyncPrimary() throws Exception {
        syncMode = CacheWriteSynchronizationMode.FULL_SYNC;
        orderMode = CacheAtomicWriteOrderMode.PRIMARY;

        checkAtomicOpTx(false);
    }

    /**
     * @throws Exception If failed.
     */
    public void testAtomicOpTxFullSyncClock() throws Exception {
        syncMode = CacheWriteSynchronizationMode.FULL_SYNC;
        orderMode = CacheAtomicWriteOrderMode.CLOCK;

        checkAtomicOpTx(false);
    }

    
    
    /**
     * @throws Exception If failed.
     */
    public void testAtomicOpLockPrimarySyncPrimaryNodeJoined() throws Exception {
        syncMode = CacheWriteSynchronizationMode.PRIMARY_SYNC;
        orderMode = CacheAtomicWriteOrderMode.PRIMARY;

        checkAtomicOpLock(true);
    }

    /**
     * @throws Exception If failed.
     */
    public void testAtomicOpLockPrimarySyncClockNodeJoined() throws Exception {
        syncMode = CacheWriteSynchronizationMode.PRIMARY_SYNC;
        orderMode = CacheAtomicWriteOrderMode.CLOCK;

        checkAtomicOpLock(true);
    }

    /**
     * @throws Exception If failed.
     */
    public void testAtomicOpLockFullSyncPrimaryNodeJoined() throws Exception {
        syncMode = CacheWriteSynchronizationMode.FULL_SYNC;
        orderMode = CacheAtomicWriteOrderMode.PRIMARY;

        checkAtomicOpLock(true);
    }

    /**
     * @throws Exception If failed.
     */
    public void testAtomicOpLockFullSyncClockNodeJoined() throws Exception {
        syncMode = CacheWriteSynchronizationMode.FULL_SYNC;
        orderMode = CacheAtomicWriteOrderMode.CLOCK;

        checkAtomicOpLock(true);
    }

    /**
     * @throws Exception If failed.
     */
    public void testAtomicOpLockPrimarySyncPrimary() throws Exception {
        syncMode = CacheWriteSynchronizationMode.PRIMARY_SYNC;
        orderMode = CacheAtomicWriteOrderMode.PRIMARY;

        checkAtomicOpLock(false);
    }

    /**
     * @throws Exception If failed.
     */
    public void testAtomicOpLockPrimarySyncClock() throws Exception {
        syncMode = CacheWriteSynchronizationMode.PRIMARY_SYNC;
        orderMode = CacheAtomicWriteOrderMode.CLOCK;

        checkAtomicOpLock(false);
    }

    /**
     * @throws Exception If failed.
     */
    public void testAtomicOpLockFullSyncPrimary() throws Exception {
        syncMode = CacheWriteSynchronizationMode.FULL_SYNC;
        orderMode = CacheAtomicWriteOrderMode.PRIMARY;

        checkAtomicOpLock(false);
    }

    /**
     * @throws Exception If failed.
     */
    public void testAtomicOpLockFullSyncClock() throws Exception {
        syncMode = CacheWriteSynchronizationMode.FULL_SYNC;
        orderMode = CacheAtomicWriteOrderMode.CLOCK;

        checkAtomicOpLock(false);
    }

    /**
     * @param startNode {@code True} if should start node inside tx.
     * @throws Exception If failed.
     */
    private void checkAtomicOpTx(boolean startNode) throws Exception {
        initCaches();

        try {
            IgniteCache<Object, Object> txCache = grid(0).cache(TRANSACTIONAL_CACHE);
            IgniteCache<Object, Object> atomicCache = grid(0).cache(ATOMIC_CACHE);

            for (int i = 0; i < 10; i++)
                atomicCache.put("key" + i, -1);

            try {
                IgniteInternalFuture<Object> fut = null;

                try (Transaction tx = grid(0).transactions().txStart(PESSIMISTIC, REPEATABLE_READ)) {
                    for (int i = 0; i < 10; i++)
                        txCache.put("key" + i, i);

                    if (startNode) {
                        fut = GridTestUtils.runAsync(new Callable<Object>() {
                            @Override public Object call() throws Exception {
                                return startGrid(NODE_COUNT);
                            }
                        });

                        GridTestUtils.waitForCondition(new GridAbsPredicate() {
                            @Override public boolean apply() {
                                return grid(0).cluster().nodes().size() == NODE_COUNT + 1;
                            }
                        }, getTestTimeout());
                    }

                    info("Starting atomic puts.");

                    for (int i = 0; i < 10; i++)
                        atomicCache.put("key" + i, i);

                    info("Atomic puts done.");

                    tx.commit();
                }

                if (fut != null)
                    fut.get();

                for (int g = 0; g < NODE_COUNT + (startNode ? 1 : 0); g++) {
                    IgniteCache<Object, Object> cache = grid(g).cache(ATOMIC_CACHE);

                    for (int i = 0; i < 10; i++)
                        assertEquals(i, cache.get("key" + i));
                }
            }
            finally {
                if (startNode)
                    stopGrid(NODE_COUNT);
            }
        }
        finally {
            destroyCaches();
        }
    }

    /**
     * @param startNode {@code True} if should start node inside tx.
     * @throws Exception If failed.
     */
    @SuppressWarnings("TooBroadScope")
    private void checkAtomicOpLock(boolean startNode) throws Exception {
        initCaches();

        try {
            IgniteCache<Object, Object> txCache = grid(0).cache(TRANSACTIONAL_CACHE);
            IgniteCache<Object, Object> atomicCache = grid(0).cache(ATOMIC_CACHE);

            for (int i = 0; i < 10; i++)
                atomicCache.put("key" + i, -1);

            try {
                Lock lock = txCache.lock("key0");

                IgniteInternalFuture<Object> fut = null;

                lock.lock();

                try {
                    for (int i = 0; i < 10; i++)
                        txCache.put("key" + i, i);

                    if (startNode) {
                        fut = GridTestUtils.runAsync(new Callable<Object>() {
                            @Override public Object call() throws Exception {
                                return startGrid(NODE_COUNT);
                            }
                        });

                        GridTestUtils.waitForCondition(new GridAbsPredicate() {
                            @Override public boolean apply() {
                                return grid(0).cluster().nodes().size() == NODE_COUNT + 1;
                            }
                        }, getTestTimeout());
                    }

                    info("Starting atomic puts.");

                    for (int i = 0; i < 10; i++)
                        atomicCache.put("key" + i, i);

                    info("Atomic puts done.");
                }
                finally {
                    lock.unlock();
                }

                if (fut != null)
                    fut.get();

                for (int g = 0; g < NODE_COUNT + (startNode ? 1 : 0); g++) {
                    IgniteCache<Object, Object> cache = grid(g).cache(ATOMIC_CACHE);

                    for (int i = 0; i < 10; i++)
                        assertEquals(i, cache.get("key" + i));
                }
            }
            finally {
                if (startNode)
                    stopGrid(NODE_COUNT);
            }
        }
        finally {
            destroyCaches();
        }
    }

    /**
     * @throws Exception If failed.
     */
    public void testAtomicOpSystemCacheTxNodeJoined() throws Exception {
        initCaches();

        try {
            GridCacheAdapter txCache = (GridCacheAdapter)grid(0).utilityCache();
            IgniteCache<Object, Object> atomicCache = grid(0).cache(ATOMIC_CACHE);

            for (int i = 0; i < 10; i++)
                atomicCache.put("key" + i, -1);

            try {
                IgniteInternalFuture<Object> fut;

                // Intentionally start system tx.
                try (Transaction tx = txCache.txStart(PESSIMISTIC, REPEATABLE_READ)) {
                    for (int i = 0; i < 10; i++)
                        txCache.put("key" + i, i);

                    fut = GridTestUtils.runAsync(new Callable<Object>() {
                        @Override public Object call() throws Exception {
                            return startGrid(NODE_COUNT);
                        }
                    });

                    GridTestUtils.waitForCondition(new GridAbsPredicate() {
                        @Override public boolean apply() {
                            return grid(0).cluster().nodes().size() == NODE_COUNT + 1;
                        }
                    }, getTestTimeout());

                    info("Starting atomic puts.");

                    for (int i = 0; i < 10; i++)
                        atomicCache.put("key" + i, i);

                    info("Atomic puts done.");

                    tx.commit();
                }

                if (fut != null)
                    fut.get();

                for (int g = 0; g < NODE_COUNT + 1; g++) {
                    IgniteCache<Object, Object> cache = grid(g).cache(ATOMIC_CACHE);

                    for (int i = 0; i < 10; i++)
                        assertEquals(i, cache.get("key" + i));
                }
            }
            finally {
                stopGrid(NODE_COUNT);
            }
        }
        finally {
            destroyCaches();
        }
    }
}
