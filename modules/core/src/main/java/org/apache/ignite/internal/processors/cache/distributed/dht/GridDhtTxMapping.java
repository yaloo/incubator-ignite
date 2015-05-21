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

package org.apache.ignite.internal.processors.cache.distributed.dht;

import org.apache.ignite.cluster.*;
import org.apache.ignite.internal.processors.cache.distributed.*;
import org.apache.ignite.internal.util.*;
import org.apache.ignite.internal.util.typedef.*;
import org.jetbrains.annotations.*;

import java.util.*;

/**
 * DHT transaction mapping.
 */
public class GridDhtTxMapping {
    /** */
    private UUID locNodeId;

    /** Transaction nodes mapping (primary node -> related backup nodes). */
    private final Map<UUID, Collection<UUID>> txNodes = new GridLeanMap<>();

    /** */
    private final List<TxMapping> mappings = new ArrayList<>();

    /** */
    private TxMapping last;

    /**
     * @param locNodeId Local node ID.
     */
    public GridDhtTxMapping(UUID locNodeId) {
        this.locNodeId = locNodeId;
    }

    /**
     * Adds information about next mapping.
     *
     * @param nodes Nodes.
     */
    @SuppressWarnings("ConstantConditions")
    public void addMapping(List<ClusterNode> nodes, boolean near) {
        ClusterNode primary = F.first(nodes);

        Collection<ClusterNode> backups = F.view(nodes, F.notEqualTo(primary));

        if (last == null || !last.primary.equals(primary.id()) || last.near != near) {
            last = new TxMapping(primary, backups, near);

            mappings.add(last);
        }
        else
            last.add(backups);

        Collection<UUID> storedBackups = txNodes.get(last.primary);

        if (storedBackups == null)
            txNodes.put(last.primary, storedBackups = new HashSet<>());

        storedBackups.addAll(last.backups);
    }

    /**
     * @return Primary to backup mapping.
     */
    public Map<UUID, Collection<UUID>> transactionNodes() {
        return txNodes;
    }

    /**
     * For each mapping sets flags indicating if mapping is last for node.
     *
     * @param mappings Mappings.
     */
    public void initLast(Collection<GridDistributedTxMapping> mappings) {
        assert this.mappings.size() == mappings.size() :
            "this.size=" + this.mappings.size() + ", size=" + mappings.size();

        int idx = 0;

        for (GridDistributedTxMapping map : mappings) {
            TxMapping mapping = this.mappings.get(idx);

            map.lastBackups(lastBackups(mapping, idx));

            boolean last = true;

            for (int i = idx + 1; i < this.mappings.size(); i++) {
                TxMapping nextMap = this.mappings.get(i);

                if (nextMap.primary.equals(mapping.primary) && (!F.eq(locNodeId, nextMap.primary) || nextMap.near == mapping.near)) {
                    last = false;

                    break;
                }
            }

            map.last(last);

            idx++;
        }
    }

    /**
     * @param mapping Mapping.
     * @param idx Mapping index.
     * @return IDs of backup nodes receiving last prepare request during this mapping.
     */
    @Nullable private Collection<UUID> lastBackups(TxMapping mapping, int idx) {
        Collection<UUID> res = null;

        for (UUID backup : mapping.backups) {
            boolean foundNext = false;

            for (int i = idx + 1; i < mappings.size(); i++) {
                TxMapping nextMap = mappings.get(i);

                if (nextMap.primary.equals(mapping.primary) && nextMap.backups.contains(backup)) {
                    foundNext = true;

                    break;
                }
            }

            if (!foundNext) {
                if (res == null)
                    res = new ArrayList<>(mapping.backups.size());

                res.add(backup);
            }
        }

        return res;
    }

    /**
     */
    private static class TxMapping {
        /** */
        private final UUID primary;

        /** */
        private final Set<UUID> backups;

        /** */
        private final boolean near;

        /**
         * @param primary Primary node.
         * @param backups Backup nodes.
         */
        private TxMapping(ClusterNode primary, Iterable<ClusterNode> backups, boolean near) {
            this.primary = primary.id();

            this.backups = new HashSet<>();

            this.near = near;

            add(backups);
        }

        /**
         * @param backups Backup nodes.
         */
        private void add(Iterable<ClusterNode> backups) {
            for (ClusterNode n : backups)
                this.backups.add(n.id());
        }
    }
}
