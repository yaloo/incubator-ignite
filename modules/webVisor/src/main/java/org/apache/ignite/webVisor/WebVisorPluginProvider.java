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

package org.apache.ignite.webVisor;

import org.apache.ignite.*;
import org.apache.ignite.cluster.*;
import org.apache.ignite.configuration.*;
import org.apache.ignite.internal.*;
import org.apache.ignite.plugin.*;
import org.jetbrains.annotations.*;

import java.io.*;
import java.util.*;

/**
 *
 */
public class WebVisorPluginProvider implements PluginProvider<WebVisorPluginConfiguration> {
    /** */
    private final WebVisorPlugin plugin = new WebVisorPlugin();

    /** */
    private WebVisorPluginConfiguration cfg;

    /** */
    private VisorServer srv;

    /** */
    private GridKernalContext igniteCtx;

    /** {@inheritDoc} */
    @Override public String name() {
        return "web-visor";
    }

    /** {@inheritDoc} */
    @Override public String version() {
        return "1.0.0";
    }

    /** {@inheritDoc} */
    @Override public String copyright() {
        return null;
    }

    /** {@inheritDoc} */
    @Override public WebVisorPlugin plugin() {
        return plugin;
    }

    /** {@inheritDoc} */
    @Nullable @Override public <T> T createComponent(PluginContext ctx, Class<T> cls) {
        return null;
    }

    /** {@inheritDoc} */
    @Override public void initExtensions(PluginContext ctx, ExtensionRegistry registry) {
        IgniteConfiguration igniteCfg = ctx.igniteConfiguration();

        if (igniteCfg.getPluginConfigurations() != null) {
            for (PluginConfiguration pluginCfg : igniteCfg.getPluginConfigurations()) {
                if (pluginCfg instanceof WebVisorPluginConfiguration) {
                    cfg = (WebVisorPluginConfiguration)pluginCfg;

                    break;
                }
            }
        }

        if (cfg == null)
            cfg = new WebVisorPluginConfiguration();

        srv = VisorServer.getOrStart(cfg, ctx.log(VisorServer.class));

        igniteCtx = ((IgniteKernal)ctx.grid()).context();
    }

    /** {@inheritDoc} */
    @Override public void start(PluginContext ctx) throws IgniteCheckedException {
        if (srv.isStarted()) {
            igniteCtx.addNodeAttribute(WebVisorUtils.WEB_VISOR_NODE_PORT, srv.getPort());

            String igniteName = ctx.grid().name();

            if (igniteName != null && !igniteName.isEmpty()) {
                String shortName = IgniteNameRepository.instance().getOrRegisterShortName(igniteName);
                
                igniteCtx.addNodeAttribute(WebVisorUtils.WEB_VISOR_NODE_URI, "/g_" + shortName);
            }
        }
    }

    /** {@inheritDoc} */
    @Override public void stop(boolean cancel) throws IgniteCheckedException {
        // No-op.
    }

    /** {@inheritDoc} */
    @Override public void onIgniteStart() throws IgniteCheckedException {
        // No-op.
    }

    /** {@inheritDoc} */
    @Override public void onIgniteStop(boolean cancel) {
        // No-op.
    }

    /** {@inheritDoc} */
    @Nullable @Override public Serializable provideDiscoveryData(UUID nodeId) {
        return null;
    }

    /** {@inheritDoc} */
    @Override public void receiveDiscoveryData(UUID nodeId, Serializable data) {
        // No-op.
    }

    /** {@inheritDoc} */
    @Override public void validateNewNode(ClusterNode node) throws PluginValidationException {
        // No-op.
    }
}
