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

/**
 *
 */
public class User {
    /** */
    private String id;

    /** */
    private String login;

    /** */
    private String name;

    /**
     *
     */
    public User() {
        // No-op.
    }

    /**
     * @param login Login.
     */
    public User(String login) {
        this.login = login;
    }

    /**
     * @return ID.
     */
    public String id() {
        return id;
    }

    /**
     * @param id ID.
     */
    public void id(String id) {
        this.id = id;
    }

    /**
     * @return User login.
     */
    public String login() {
        return login;
    }

    /**
     * @param login Login.
     */
    public void login(String login) {
        this.login = login;
    }

    /**
     * @return User name.
     */
    public String name() {
        return name;
    }

    /**
     * @param name User name.
     */
    public void name(String name) {
        this.name = name;
    }

    /**
     * @return JSON user representation.
     */
    public String toJson() {
        StringBuilder sb = new StringBuilder("{\"id\":\"").append(id)
            .append("\", \"login\":\"").append(login)
            .append("\", \"name\":");
        if (name == null)
            sb.append("null");
        else
            sb.append("\"").append(name).append("\"");

        sb.append("}");

        return sb.toString();
    }
}
