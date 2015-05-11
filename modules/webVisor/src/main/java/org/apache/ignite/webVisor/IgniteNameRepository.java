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

package org.apache.ignite.webVisor;

import org.apache.ignite.*;
import org.jetbrains.annotations.*;

import java.util.*;
import java.util.regex.*;

/**
 * Each unit test starts several nodes with name like "partitioned.GridCachePartitionedAtomicReferenceApiSelfTest0".
 * Utl like http://localhost:8100/partitioned.GridCachePartitionedAtomicReferenceApiSelfTest0 lookslooks bad, We should 
 * cut it to http://localhost:8100/g_0 , Ignite started by startGrids(1) will has suffix "g_1", Ignite started with 
 * startGrids(2) will has suffix "g_2"
 */
public class IgniteNameRepository {
    /** */
    private static final IgniteNameRepository instance = new IgniteNameRepository();

    /** 
     * Names that should not be cut, For example TcpClientDiscoverySelfTest starts nodes with names like "server-1", 
     * "client-2", in this case URL like http://localhost:8100/server-1 looks good. 
     */
    private static final Pattern GOOD_NAME_PATTERN = Pattern.compile("[0-9a-zA-Z\\-\\._]{1,15}");

    /** Map short name to ignite name */
    private final Map<String, String> shortName2Name = new HashMap<>();

    /** Map short name to ignite name */
    private final Map<String, String> name2shortName = new HashMap<>();

    /**
     * Default constructor.
     */
    private IgniteNameRepository() {
        Ignition.addListener(new IgnitionListener() {
            @Override public void onStateChange(@Nullable String name, IgniteState state) {
                if (name != null && (state == IgniteState.STOPPED || state == IgniteState.STOPPED_ON_SEGMENTATION)) {
                    synchronized (IgniteNameRepository.this) {
                        String shortName = name2shortName.remove(name);

                        shortName2Name.remove(shortName);
                    }
                }
            }
        });
    }

    /**
     * @param shortName Short name.
     */
    @Nullable public synchronized String igniteName(@NotNull String shortName) {
        return shortName2Name.get(shortName);
    }

    /**
     * @param igniteName Ignite name.
     */
    public synchronized String getOrRegisterShortName(@NotNull String igniteName) {
        assert !igniteName.isEmpty();

        String res = name2shortName.get(igniteName);

        if (res != null)
            return res;

        if (GOOD_NAME_PATTERN.matcher(igniteName).matches()) {
            if (!shortName2Name.containsKey(igniteName)) {
                shortName2Name.put(igniteName, igniteName);

                name2shortName.put(igniteName, igniteName);

                return igniteName;
            }
        }

        char lastC = igniteName.charAt(igniteName.length() - 1);

        if (lastC >= '0' && lastC <= '9') {
            String oneNumName = igniteName.substring(igniteName.length() - 1);

            if (!shortName2Name.containsKey(oneNumName)) {
                shortName2Name.put(oneNumName, igniteName);

                name2shortName.put(igniteName, oneNumName);

                return oneNumName;
            }
        }

        int idx = 100;

        while (true) {
            String shortName = "n" + idx;

            if (!shortName2Name.containsKey(shortName)) {
                shortName2Name.put(shortName, igniteName);

                name2shortName.put(igniteName, shortName);

                return shortName;
            }
        }
    }

    /**
     * @return Instance
     */
    public static IgniteNameRepository instance() {
        return instance;
    }
}
