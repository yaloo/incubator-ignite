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

import org.apache.ignite.webconfig.server.model.*;
import org.mongodb.morphia.*;
import org.springframework.beans.factory.*;

/**
 *
 */
public class MorphiaSpringBean implements InitializingBean {
    /** Morphia instance. */
    private Morphia morphia;

    /** Mongo spring bean. */
    private MongoSpringBean mongo;

    /** Datastore instance. */
    private Datastore datastore;

    /**
     * @param mongo Mongo bean.
     */
    public void setMongo(MongoSpringBean mongo) {
        this.mongo = mongo;
    }

    /** {@inheritDoc} */
    @Override public void afterPropertiesSet() throws Exception {
        morphia = new Morphia();

        morphia.mapPackage(User.class.getPackage().getName());

        datastore = morphia.createDatastore(mongo.client(), mongo.getDbName());
    }

    /**
     * @return Datastore.
     */
    public Datastore datastore() {
        return datastore;
    }
}
