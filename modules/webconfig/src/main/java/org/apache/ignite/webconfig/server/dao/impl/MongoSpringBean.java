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
package org.apache.ignite.webconfig.server.dao.impl;

import com.mongodb.*;
import org.springframework.beans.factory.*;

/**
 * Utility class providing connection to mongo DB.
 */
public class MongoSpringBean implements InitializingBean {
    /** */
    private String host;

    /** */
    private int port;

    /** */
    private String dbName;

    /** Mongo client. */
    private MongoClient client;

    /** DB instance. */
    private DB db;

    /** {@inheritDoc} */
    @Override public void afterPropertiesSet() throws Exception {
        client = port == 0 ? new MongoClient(host) : new MongoClient(host, port);

        db = client.getDB(dbName);
    }

    /**
     * @return Host.
     */
    public String getHost() {
        return host;
    }

    /**
     * @param host Host.
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * @return Port.
     */
    public int getPort() {
        return port;
    }

    /**
     * @param port Port.
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * @return DB name.
     */
    public String getDbName() {
        return dbName;
    }

    /**
     * @param dbName DB name.
     */
    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    /**
     * @return Mongo client.
     */
    public MongoClient client() {
        return client;
    }

    /**
     * @return Database instance.
     */
    public DB db() {
        return db;
    }
}
