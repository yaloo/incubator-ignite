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
package org.apache.ignite.webconfig.server.model;

import org.bson.types.*;
import org.codehaus.jackson.annotate.*;
import org.mongodb.morphia.annotations.*;

/**
 *
 */
@Entity("clusterConfiguration")
public class ClusterConfiguration {
    /** Object ID. */
    @Id private ObjectId id;

    /** */
    @JsonProperty("idStr")
    @Transient private String idStr;

    /** Cluster name. */
    @JsonProperty("name")
    private String name;

    /** Max memory. */
    @JsonProperty("maxMemory")
    private long maxMemory;

    /**
     * @return Object ID.
     */
    public String id() {
        return idStr;
    }

    /**
     * @param idStr ID string.
     */
    public void id(String idStr) {
        this.idStr = idStr;
    }

    /**
     * @return Name.
     */
    public String name() {
        return name;
    }

    /**
     * @param name Name.
     */
    public void name(String name) {
        this.name = name;
    }

    /**
     * @return Max memory.
     */
    public long maxMemory() {
        return maxMemory;
    }

    /**
     * @param maxMemory Max memory.
     */
    public void maxMemory(long maxMemory) {
        this.maxMemory = maxMemory;
    }

    /**
     * Pre-persist callback.
     */
    @PrePersist
    public void prePersist() {
        if (id == null && idStr != null)
            id = new ObjectId(idStr);
    }

    /**
     * Post persist callback.
     */
    @PostPersist
    public void postPersist() {
        initIdStr();
    }

    /**
     * Post-load callback.
     */
    @PostLoad
    public void postLoad() {
        initIdStr();
    }

    /**
     *
     */
    private void initIdStr() {
        if (idStr == null && id != null)
            idStr = id.toHexString();
    }
}
