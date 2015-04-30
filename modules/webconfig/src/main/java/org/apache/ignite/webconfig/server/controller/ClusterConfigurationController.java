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

import org.apache.ignite.webconfig.server.dao.*;
import org.apache.ignite.webconfig.server.model.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.web.bind.annotation.*;

/**
 *
 */
@Controller
@RequestMapping("/configuration")
public class ClusterConfigurationController {
    /** Configuration DAO. */
    @Autowired
    private ClusterConfigurationDao cfgDao;

    /**
     * @param cfgDao Configuration DAO.
     */
    public void setCfgDao(ClusterConfigurationDao cfgDao) {
        this.cfgDao = cfgDao;
    }

    /**
     * @param cfg Configuration to save.
     * @return Response body.
     */
    @ResponseBody
    @RequestMapping(value = "save.do", method = RequestMethod.POST)
    public JsonResponseBean saveConfiguration(@RequestBody ClusterConfiguration cfg) {
        cfg = cfgDao.saveConfiguration(cfg);

        JsonResponseBean res = new JsonResponseBean(true, null);

        res.addField("cfg", cfg);

        return res;
    }
}
