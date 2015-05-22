/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.ignite.internal.processors.cache;

import org.apache.ignite.cache.*;
import org.apache.ignite.configuration.*;
import org.apache.ignite.testframework.junits.common.*;

/**
 * Check stop and remove methods behavior.
 */
public class CacheStopAndDestroySelfTest extends GridCommonAbstractTest {
    /** key-value used at test. */
    protected static String KEY_VAL = "1";

    /** cache name 1. */
    protected static String CACHE_NAME_1 = "cache1";

    /** cache name 2. */
    protected static String CACHE_NAME_2 = "cache2";

    /** cache name 3. */
    protected static String CACHE_NAME_3 = "cache3";

    /** cache name 4. */
    protected static String CACHE_NAME_4 = "cache4";

    /** local cache name 1. */
    protected static String CACHE_NAME_LOC_1 = "local1";

    /** local cache name 2. */
    protected static String CACHE_NAME_LOC_2 = "local2";

    /**
     * @return Grids count to start.
     */
    protected int gridCount() {
        return 3;
    }

    /** {@inheritDoc} */
    @Override protected IgniteConfiguration getConfiguration(String gridName) throws Exception {
        IgniteConfiguration iCfg = super.getConfiguration(gridName);

        if (getTestGridName(2).equals(gridName))
            iCfg.setClientMode(true);

        iCfg.setCacheConfiguration();

        return iCfg;
    }

    /**
     * Test.
     *
     * @throws Exception If failed.
     */
    public void testCacheStopAndDestroy() throws Exception {
        startGridsMultiThreaded(gridCount());

        CacheConfiguration cCfg1 = defaultCacheConfiguration();
        cCfg1.setName(CACHE_NAME_1);
        cCfg1.setCacheMode(CacheMode.PARTITIONED);

        CacheConfiguration cCfg2 = defaultCacheConfiguration();
        cCfg2.setName(CACHE_NAME_2);
        cCfg2.setCacheMode(CacheMode.PARTITIONED);

        CacheConfiguration cCfg3 = defaultCacheConfiguration();
        cCfg3.setName(CACHE_NAME_3);
        cCfg3.setCacheMode(CacheMode.PARTITIONED);

        CacheConfiguration cCfg4 = defaultCacheConfiguration();
        cCfg4.setName(CACHE_NAME_4);
        cCfg4.setCacheMode(CacheMode.PARTITIONED);

        CacheConfiguration cCfgLoc1 = defaultCacheConfiguration();
        cCfgLoc1.setName(CACHE_NAME_LOC_1);
        cCfgLoc1.setCacheMode(CacheMode.LOCAL);

        CacheConfiguration cCfgLoc2 = defaultCacheConfiguration();
        cCfgLoc2.setName(CACHE_NAME_LOC_2);
        cCfgLoc2.setCacheMode(CacheMode.LOCAL);

        grid(0).getOrCreateCache(cCfg1);
        grid(0).getOrCreateCache(cCfg2);
        grid(0).getOrCreateCache(cCfg3);
        grid(0).getOrCreateCache(cCfg4);
        grid(0).getOrCreateCache(cCfgLoc1);
        grid(0).getOrCreateCache(cCfgLoc2);

        grid(0).cache(CACHE_NAME_1).put(KEY_VAL, KEY_VAL);
        grid(0).cache(CACHE_NAME_2).put(KEY_VAL, KEY_VAL);
        grid(0).cache(CACHE_NAME_3).put(KEY_VAL, KEY_VAL);
        grid(0).cache(CACHE_NAME_4).put(KEY_VAL, KEY_VAL);
        grid(0).cache(CACHE_NAME_LOC_1).put(KEY_VAL, KEY_VAL + 0);
        grid(0).cache(CACHE_NAME_LOC_2).put(KEY_VAL, KEY_VAL + 0);
        grid(1).cache(CACHE_NAME_LOC_1).put(KEY_VAL, KEY_VAL + 1);
        grid(1).cache(CACHE_NAME_LOC_2).put(KEY_VAL, KEY_VAL + 1);

        assert grid(0).cache(CACHE_NAME_1).get(KEY_VAL).equals(KEY_VAL);
        assert grid(0).cache(CACHE_NAME_2).get(KEY_VAL).equals(KEY_VAL);
        assert grid(0).cache(CACHE_NAME_3).get(KEY_VAL).equals(KEY_VAL);
        assert grid(0).cache(CACHE_NAME_4).get(KEY_VAL).equals(KEY_VAL);
        assert grid(0).cache(CACHE_NAME_LOC_1).get(KEY_VAL).equals(KEY_VAL + 0);
        assert grid(0).cache(CACHE_NAME_LOC_2).get(KEY_VAL).equals(KEY_VAL + 0);
        assert grid(1).cache(CACHE_NAME_LOC_1).get(KEY_VAL).equals(KEY_VAL + 1);
        assert grid(1).cache(CACHE_NAME_LOC_2).get(KEY_VAL).equals(KEY_VAL + 1);

        //Destroy:

        //DHT Destroy. Cache should be removed from each node.
        grid(0).cache(CACHE_NAME_1).destroy();

        try {
            grid(0).cache(CACHE_NAME_1).get(KEY_VAL).equals(KEY_VAL);
            assert false;
        }
        catch (IllegalArgumentException | IllegalStateException ignored0) {
            try {
                grid(1).cache(CACHE_NAME_1).get(KEY_VAL).equals(KEY_VAL);
                assert false;
            }
            catch (IllegalArgumentException | IllegalStateException ignored1) {
                try {
                    grid(2).cache(CACHE_NAME_1).get(KEY_VAL).equals(KEY_VAL);
                    assert false;
                }
                catch (IllegalArgumentException | IllegalStateException ignored2) {
                    // No-op
                }
            }
        }

        //DHT Destroy from client node. Cache should be removed from each node.
        grid(2).cache(CACHE_NAME_2).destroy();// Client node.

        try {
            grid(0).cache(CACHE_NAME_2).get(KEY_VAL).equals(KEY_VAL);
            assert false;
        }
        catch (IllegalArgumentException | IllegalStateException ignored0) {
            try {
                grid(1).cache(CACHE_NAME_2).get(KEY_VAL).equals(KEY_VAL);
                assert false;
            }
            catch (IllegalArgumentException | IllegalStateException ignored1) {
                try {
                    grid(2).cache(CACHE_NAME_2).get(KEY_VAL).equals(KEY_VAL);
                    assert false;
                }
                catch (IllegalArgumentException | IllegalStateException ignored2) {
                    // No-op
                }
            }
        }

        //Local destroy. Cache should be removed from each node.
        grid(0).cache(CACHE_NAME_LOC_1).destroy();

        try {
            grid(0).cache(CACHE_NAME_LOC_1).get(KEY_VAL).equals(KEY_VAL);
            assert false;
        }
        catch (IllegalArgumentException | IllegalStateException ignored0) {
            try {
                grid(1).cache(CACHE_NAME_LOC_1).get(KEY_VAL).equals(KEY_VAL);
                assert false;
            }
            catch (IllegalArgumentException | IllegalStateException ignored1) {
                try {
                    grid(2).cache(CACHE_NAME_LOC_1).get(KEY_VAL).equals(KEY_VAL);
                    assert false;
                }
                catch (IllegalArgumentException | IllegalStateException ignored2) {
                    // No-op
                }
            }
        }

        //Close:

        //DHT Close. No-op.
        grid(0).cache(CACHE_NAME_3).close();

        assert grid(0).cache(CACHE_NAME_3).get(KEY_VAL).equals(KEY_VAL);// Not affected.
        assert grid(1).cache(CACHE_NAME_3).get(KEY_VAL).equals(KEY_VAL);// Not affected.
        assert grid(2).cache(CACHE_NAME_3).get(KEY_VAL).equals(KEY_VAL);// Not affected.

        //DHT Close from client node. Should affect only client node.
        grid(2).cache(CACHE_NAME_4).close();// Client node.

        assert grid(0).cache(CACHE_NAME_4).get(KEY_VAL).equals(KEY_VAL);// Not affected.
        assert grid(1).cache(CACHE_NAME_4).get(KEY_VAL).equals(KEY_VAL);// Not affected.

        try {
            grid(2).cache(CACHE_NAME_4).get(KEY_VAL).equals(KEY_VAL);// Affected.
            assert false;
        }
        catch (IllegalArgumentException | IllegalStateException ignored) {
            // No-op
        }

        //Local close. Same as Local destroy.
        grid(1).cache(CACHE_NAME_LOC_2).close();

        try {
            grid(0).cache(CACHE_NAME_LOC_2).get(KEY_VAL).equals(KEY_VAL);
            assert false;
        }
        catch (IllegalArgumentException | IllegalStateException ignored0) {
            try {
                grid(1).cache(CACHE_NAME_LOC_2).get(KEY_VAL).equals(KEY_VAL);
                assert false;
            }
            catch (IllegalArgumentException | IllegalStateException ignored1) {
                try {
                    grid(2).cache(CACHE_NAME_LOC_2).get(KEY_VAL).equals(KEY_VAL);
                    assert false;
                }
                catch (IllegalArgumentException | IllegalStateException ignored2) {
                    // No-op
                }
            }
        }
    }
}
