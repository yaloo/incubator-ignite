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

package org.apache.ignite.webVisor.render;

import org.apache.ignite.*;
import org.apache.ignite.cluster.*;
import org.apache.ignite.spi.discovery.tcp.internal.*;
import org.apache.ignite.webVisor.*;
import org.jetbrains.annotations.*;

/**
 *
 */
public class WU {
    /** */
    private final Ignite g;

    /**
     * @param g G.
     */
    public WU(Ignite g) {
        this.g = g;
    }

    /**
     * @param node Node.
     */
    public String nodeAddr(ClusterNode node) {
        String host = WebVisorUtils.hostname(node);
        
        if (!node.isClient() && node instanceof TcpDiscoveryNode)
            return host + ':' + ((TcpDiscoveryNode)node).discoveryPort();
        
        return host;
    }

    /**
     * @param node Node.
     */
    @Nullable public String visorUrl(ClusterNode node) {
        Integer port = node.attribute(WebVisorUtils.WEB_VISOR_NODE_PORT);

        if (port == null)
            return null;

        String uri = node.attribute(WebVisorUtils.WEB_VISOR_NODE_URI);

        return "http://" + WebVisorUtils.hostname(node) + ':' + port + (uri == null ? "" : uri);
    }
    
    /**
     *
     */
    public ClusterNode localNode() {
        return g.cluster().localNode();
    }
}
