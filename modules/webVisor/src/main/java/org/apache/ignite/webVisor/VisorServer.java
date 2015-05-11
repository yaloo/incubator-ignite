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
import org.apache.ignite.internal.util.*;
import org.apache.ignite.internal.util.typedef.internal.*;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.util.log.*;
import org.eclipse.jetty.util.resource.*;
import org.eclipse.jetty.webapp.*;
import org.jetbrains.annotations.*;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 *
 */
public class VisorServer {
    /** */
    private static VisorServer instance;
    
    /** */
    private Server srv;

    /** */
    private int port = -1;

    /** */
    private final WebVisorPluginConfiguration cfg;
    
    /**
     * @param cfg Config.
     */
    private VisorServer(WebVisorPluginConfiguration cfg) {
        this.cfg = cfg;
    }

    /**
     * @param cfg Config.
     * @param log Logger.
     */
    public synchronized static VisorServer getOrStart(@NotNull WebVisorPluginConfiguration cfg, 
        @Nullable IgniteLogger log) {
        if (instance == null) {
            instance = new VisorServer(cfg);
            
            instance.startup(log);
        }
        
        return instance;
    }
    
    /**
     *
     */
    public boolean isStarted() {
        return srv != null && srv.isStarted();
    }
    
    /**
     *
     */
    public void startup(@Nullable IgniteLogger log) {
        try {
            Properties p = new Properties();

            p.setProperty("org.eclipse.jetty.LEVEL", "WARN");
            p.setProperty("org.eclipse.jetty.util.log.LEVEL", "OFF");
            p.setProperty("org.eclipse.jetty.util.component.LEVEL", "OFF");

            StdErrLog.setProperties(p);

            URL webXmlRes = VisorServer.class.getClassLoader().getResource("webapp/WEB-INF/web.xml");

            String webXmlStr = webXmlRes.toString();

            int port;

            for (port = cfg.getFirstPort(); port < cfg.getFirstPort() + cfg.getPortRangeSize(); port++) {
                Server srv = new Server(port);
                
                WebAppContext webAppCtx = new WebAppContext();

                webAppCtx.setContextPath("/");

                if (webXmlStr.startsWith("file:/")) {
                    // Run from IDEA.
                    File webApp = new File(webXmlRes.getFile()).getParentFile().getParentFile();
                    File classes = webApp.getParentFile();
                    File target = classes.getParentFile();
                    File moduleRoot = target.getParentFile();

                    webAppCtx.setResourceBase(moduleRoot.getAbsolutePath() + "/src/main/webapp");

                    Configuration.ClassList classlist = Configuration.ClassList.setServerDefault(srv);
                    classlist.addBefore(
                        "org.eclipse.jetty.webapp.JettyWebXmlConfiguration",
                        "org.eclipse.jetty.annotations.AnnotationConfiguration" );

                    // Set the ContainerIncludeJarPattern so that jetty examines these
                    // container-path jars for tlds, web-fragments etc.
                    // If you omit the jar that contains the jstl .tlds, the jsp engine will
                    // scan for them instead.
                    webAppCtx.setAttribute(
                        "org.eclipse.jetty.server.webapp.ContainerIncludeJarPattern",
                        ".*/[^/]*servlet-api-[^/]*\\.jar$|.*/javax.servlet.jsp.jstl-.*\\.jar$|.*/[^/]*taglibs.*\\.jar$" );
                }
                else {
                    // Run from JAR
                    URL webAppUrl = new URL(webXmlStr.substring(0, webXmlStr.length() - "WEB-INF/web.xml".length()));

                    webAppCtx.setBaseResource(Resource.newResource(webAppUrl));
                }

                webAppCtx.setAttribute("debugUtil", DebugUtil.getInstance());

                srv.setHandler(webAppCtx);

                try {
                    srv.start();

                    this.srv = srv;

                    break;
                }
                catch (BindException ignored) {
                    // No-op.
                }
            }

            if (srv == null) {
                U.error(log, "Failed to start web server all ports are busy [firstPort=" + cfg.getFirstPort()
                    + ", portsCount=" + cfg.getPortRangeSize() + "]");
            }
            else {
                this.port = port;

                if (log != null)
                    log.info("Visor server started: http://localhost:" + port);
            }
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     *
     */
    public void stop() throws Exception {
        if (srv != null)
            srv.stop();
    }

    /**
     *
     */
    public int getPort() {
        assert isStarted();
        
        return port;
    }
}
