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

import org.apache.ignite.webconfig.server.dao.*;
import org.apache.ignite.webconfig.server.model.*;

import java.util.*;

/**
 *
 */
public class MongoClusterConfigurationDaoImpl implements ClusterConfigurationDao {
    /** Morphia spring bean. */
    private MorphiaSpringBean morphiaSpringBean;

    /**
     * @param morphiaSpringBean Morphia spring bean.
     */
    public void setMorphiaSpringBean(MorphiaSpringBean morphiaSpringBean) {
        this.morphiaSpringBean = morphiaSpringBean;
    }

    /** {@inheritDoc} */
    @Override public Collection<ClusterConfiguration> allConfigurations() {
        return morphiaSpringBean.datastore().find(ClusterConfiguration.class).asList();
    }

    /** {@inheritDoc} */
    @Override public ClusterConfiguration saveConfiguration(ClusterConfiguration cfg) {
        morphiaSpringBean.datastore().save(cfg);

        return cfg;
    }

    /** {@inheritDoc} */
    @Override public void deleteConfiguration(ClusterConfiguration cfg) {
        morphiaSpringBean.datastore().delete(cfg);
    }
}
