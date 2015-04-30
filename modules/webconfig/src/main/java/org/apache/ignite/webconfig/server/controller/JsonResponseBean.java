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

package org.apache.ignite.webconfig.server.controller;

import org.codehaus.jackson.annotate.*;

import java.util.*;

/**
 *
 */
public class JsonResponseBean {
    /** */
    @JsonProperty("success")
    private boolean success;

    /** */
    @JsonProperty("err")
    private String err;

    /** */
    @JsonProperty("data")
    private Map<String, Object> fields;

    /**
     * @param success Success flag.
     * @param err Error message.
     */
    public JsonResponseBean(boolean success, String err) {
        this.success = success;
        this.err = err;
    }

    /**
     * @param key Key.
     * @param val Value.
     */
    public void addField(String key, Object val) {
        if (fields == null)
            fields = new HashMap<>();

        fields.put(key, val);
    }

    /**
     * @return JSON string.
     */
    public String toJson() {
        StringBuilder sb = new StringBuilder("{\"success\":").append(success)
                .append(", \"err\":\"").append(err).append("\"");

        if (fields != null) {
            for (Map.Entry<String, Object> entry : fields.entrySet())
                sb.append(", \"").append(entry.getKey()).append("\":").append(entry.getValue());
        }

        sb.append("}");

        return sb.toString();
    }
}
