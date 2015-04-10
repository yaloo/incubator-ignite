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
import org.apache.ignite.cluster.*;
import org.apache.ignite.configuration.*;
import org.apache.ignite.internal.*;
import org.apache.ignite.testframework.junits.common.*;
import org.apache.ignite.transactions.*;
import org.jsr166.*;

import java.util.*;

import static org.apache.ignite.cache.CacheAtomicityMode.*;
import static org.apache.ignite.transactions.TransactionConcurrency.*;
import static org.apache.ignite.transactions.TransactionIsolation.*;

/**
 * Tests specific combinations of cross-cache transactions.
 */
public class IgniteCrossCacheTxSelfTest extends GridCommonAbstractTest {
    /** */
    private static final String FIRST_CACHE = "FirstCache";

    /** */
    private static final String SECOND_CACHE = "SecondCache";

    /** */
    private static final int TX_CNT = 500;

    /**
     * @return Node count for this test.
     */
    protected int nodeCount() {
        return 4;
    }

    /** {@inheritDoc} */
    @Override protected void beforeTestsStarted() throws Exception {
        startGridsMultiThreaded(nodeCount());

        CacheConfiguration firstCfg = new CacheConfiguration(FIRST_CACHE);
        firstCfg.setBackups(1);
        firstCfg.setAtomicityMode(TRANSACTIONAL);
        firstCfg.setWriteSynchronizationMode(CacheWriteSynchronizationMode.FULL_SYNC);

        grid(0).createCache(firstCfg);

        CacheConfiguration secondCfg = new CacheConfiguration(SECOND_CACHE);
        secondCfg.setBackups(1);
        secondCfg.setAtomicityMode(TRANSACTIONAL);
        secondCfg.setWriteSynchronizationMode(CacheWriteSynchronizationMode.FULL_SYNC);
        secondCfg.setNearConfiguration(new NearCacheConfiguration());

        grid(0).createCache(secondCfg);
    }

    /** {@inheritDoc} */
    @Override protected void afterTestsStopped() throws Exception {
        stopAllGrids();
    }

    /**
     * @throws Exception If failed.
     */
    public void testPessimisticReadCommitted() throws Exception {
        checkTxsSingleOp(PESSIMISTIC, READ_COMMITTED);
    }

    /**
     * @throws Exception If failed.
     */
    public void testPessimisticRepeatableRead() throws Exception {
        checkTxsSingleOp(PESSIMISTIC, REPEATABLE_READ);
    }

    /**
     * @throws Exception If failed.
     */
    public void testOptimisticReadCommitted() throws Exception {
        checkTxsSingleOp(OPTIMISTIC, READ_COMMITTED);
    }

    /**
     * @throws Exception If failed.
     */
    public void testOptimisticRepeatableRead() throws Exception {
        checkTxsSingleOp(OPTIMISTIC, REPEATABLE_READ);
    }

    /**
     * @param concurrency Concurrency.
     * @param isolation Isolation.
     * @throws Exception If failed.
     */
    private void checkTxsSingleOp(TransactionConcurrency concurrency, TransactionIsolation isolation) throws Exception {
        Map<Integer, String> firstCheck = new HashMap<>();
        Map<Integer, String> secondCheck = new HashMap<>();

        for (int i = 0; i < TX_CNT; i++) {
            int grid = ThreadLocalRandom8.current().nextInt(nodeCount());

            try (Transaction tx = grid(grid).transactions().txStart(concurrency, isolation)) {
                try {
                    IgniteCache<Integer, String> first = grid(grid).cache(FIRST_CACHE);
                    IgniteCache<Integer, String> second = grid(grid).cache(SECOND_CACHE);

                    int size = ThreadLocalRandom8.current().nextInt(24) + 1;

                    for (int k = 0; k < size; k++) {
                        boolean rnd = ThreadLocalRandom8.current().nextBoolean();

                        IgniteCache<Integer, String> cache = rnd ? first : second;
                        Map<Integer, String> check = rnd ? firstCheck : secondCheck;

                        String val = rnd ? "first" + i : "second" + i;

                        cache.put(k, val);
                        check.put(k, val);
                    }

                    tx.commit();
                }
                catch (Throwable e) {
                    e.printStackTrace();

                    throw e;
                }
            }

            if (i > 0 && i % 100 == 0)
                info("Finished iteration: " + i);
        }

        for (int g = 0; g < nodeCount(); g++) {
            IgniteEx grid = grid(g);

            assertEquals(0, grid.context().cache().context().tm().idMapSize());

            ClusterNode locNode = grid.localNode();

            IgniteCache<Object, Object> firstCache = grid.cache(FIRST_CACHE);

            for (Map.Entry<Integer, String> entry : firstCheck.entrySet()) {
                boolean primary = grid.affinity(FIRST_CACHE).isPrimary(locNode, entry.getKey());

                boolean backup = grid.affinity(FIRST_CACHE).isBackup(locNode, entry.getKey());

                assertEquals("Invalid value found first cache [primary=" + primary + ", backup=" + backup +
                        ", node=" + locNode.id() + ", key=" + entry.getKey() + ']',
                    entry.getValue(), firstCache.get(entry.getKey()));
            }

            for (Map.Entry<Integer, String> entry : secondCheck.entrySet()) {
                boolean primary = grid.affinity(SECOND_CACHE).isPrimary(locNode, entry.getKey());

                boolean backup = grid.affinity(SECOND_CACHE).isBackup(locNode, entry.getKey());

                assertEquals("Invalid value found second cache [primary=" + primary + ", backup=" + backup +
                    ", node=" + locNode.id() + ", key=" + entry.getKey() + ']',
                    entry.getValue(), grid.cache(SECOND_CACHE).get(entry.getKey()));
            }
        }
    }
}
