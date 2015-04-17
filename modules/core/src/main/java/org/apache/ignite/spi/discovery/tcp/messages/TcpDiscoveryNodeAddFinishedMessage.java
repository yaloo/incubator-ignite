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

package org.apache.ignite.spi.discovery.tcp.messages;

import org.apache.ignite.internal.util.typedef.internal.*;

import java.util.*;

/**
 * Sent by coordinator across the ring to finish node add process.
 */
@TcpDiscoveryEnsureDelivery
@TcpDiscoveryRedirectToClient
public class TcpDiscoveryNodeAddFinishedMessage extends TcpDiscoveryAbstractMessage {
    /** */
    private static final long serialVersionUID = 0L;

    /** Added node ID. */
    private final UUID nodeId;

    /** Discovery data from new node. */
    private Map<Integer, byte[]> newNodeDiscoData;

    /** Discovery data from old nodes. */
    private Map<UUID, Map<Integer, byte[]>> oldNodesDiscoData;

    /**
     * Constructor.
     *
     * @param creatorNodeId ID of the creator node (coordinator).
     * @param nodeId Added node ID.
     */
    public TcpDiscoveryNodeAddFinishedMessage(
        UUID creatorNodeId,
        UUID nodeId,
        Map<Integer, byte[]> newNodeDiscoData,
        Map<UUID, Map<Integer, byte[]>> oldNodesDiscoData
    ) {
        super(creatorNodeId);

        this.nodeId = nodeId;
        this.newNodeDiscoData = newNodeDiscoData;
        this.oldNodesDiscoData = oldNodesDiscoData;
    }

    /**
     * Gets ID of the node added.
     *
     * @return ID of the node added.
     */
    public UUID nodeId() {
        return nodeId;
    }

    /**
     * @return Discovery data from new node.
     */
    public Map<Integer, byte[]> newNodeDiscoveryData() {
        return newNodeDiscoData;
    }

    /**
     * @return Discovery data from old nodes.
     */
    public Map<UUID, Map<Integer, byte[]>> oldNodesDiscoveryData() {
        return oldNodesDiscoData;
    }

    /**
     * @param nodeId Node ID.
     * @param discoData Discovery data to add.
     */
    public void addDiscoveryData(UUID nodeId, Map<Integer, byte[]> discoData) {
        // Old nodes disco data may be null if message
        // makes more than 1 pass due to stopping of the nodes in topology.
        if (oldNodesDiscoData != null)
            oldNodesDiscoData.put(nodeId, discoData);
    }

    /** {@inheritDoc} */
    @Override public String toString() {
        return S.toString(TcpDiscoveryNodeAddFinishedMessage.class, this, "super", super.toString());
    }
}
