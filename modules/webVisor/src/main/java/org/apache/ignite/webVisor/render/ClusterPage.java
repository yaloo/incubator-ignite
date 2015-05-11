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

import org.apache.ignite.cluster.*;
import org.jetbrains.annotations.*;

import javax.servlet.http.*;
import java.util.*;

/**
 *
 */
public class ClusterPage extends AbstractPageBean {
    /** */
    private final List<ClusterNode> nodes;

    /** */
    private boolean showClients = true;

    /**
     * @param req Request.
     */
    public ClusterPage(@NotNull HttpServletRequest req) {
        super(req);

        nodes = new ArrayList<>();

        if ("true".equals(req.getParameter("filter")))
            showClients = req.getParameter("showClients") != null;

        for (ClusterNode node : getIgnite().cluster().nodes()) {
            if (showClients || !node.isClient())
                nodes.add(node);
        }

        Collections.sort(nodes, new Comparator<ClusterNode>() {
            @Override public int compare(ClusterNode o1, ClusterNode o2) {
                int res = Long.compare(o1.order(), o2.order());

                if (res != 0)
                    return res;

                return o1.id().compareTo(o2.id());
            }
        });
    }

    /**
     *
     */
    @NotNull public List<ClusterNode> getNodes() {
        return nodes;
    }

    /**
     *
     */
    public boolean isShowClients() {
        return showClients;
    }
}
