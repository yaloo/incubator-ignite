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
import org.apache.ignite.webVisor.render.*;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;
import java.util.regex.*;

/**
 * 
 */
public class NodeSwitchFilter implements Filter {
    /** */
    private static final Pattern GRID_NAME_PATTERN = Pattern.compile("/g_([0-9a-zA-Z\\-\\._]+)(/.*)?");
    
    /** {@inheritDoc} */
    @Override public void init(FilterConfig cfg) throws ServletException {
        // No-op.
    }

    /**
     * @param req Request.
     * @param ignite Ignite.
     */
    private void initAttributes(HttpServletRequest req, Ignite ignite) {
        req.setAttribute("g", ignite);
        req.setAttribute("wu", new WU(ignite));
    }
    
    /** {@inheritDoc} */
    @Override public void doFilter(ServletRequest req, ServletResponse res,
        FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpReq = (HttpServletRequest)req;
        HttpServletResponse httpResp = (HttpServletResponse)res;

        Matcher matcher = GRID_NAME_PATTERN.matcher(httpReq.getRequestURI());
        
        if (!matcher.matches()) {
            Ignite dfltIgnite;
            
            try {
                dfltIgnite = Ignition.ignite();
            }
            catch (IgniteIllegalStateException e) {
                dfltIgnite = null;
            }

            if (dfltIgnite != null) {
                initAttributes(httpReq, dfltIgnite);
                
                chain.doFilter(req, res);
                
                return;
            }
            
            List<Ignite> ignites = Ignition.allGrids();

            if (ignites.isEmpty()) {
                req.getRequestDispatcher("/noIgnitesFound.jsp").forward(req, res);
                
                return;
            }

            String igniteName = ignites.get(0).name();

            if (igniteName == null || igniteName.length() == 0) {
                initAttributes(httpReq, ignites.get(0));

                chain.doFilter(req, res);
                
                return;
            }

            StringBuilder redirectUrl = new StringBuilder();
            
            redirectUrl.append(httpReq.getContextPath());
            redirectUrl.append("/g_").append(IgniteNameRepository.instance().getOrRegisterShortName(igniteName));
            redirectUrl.append(httpReq.getRequestURI());
            
            if (httpReq.getQueryString() != null)
                redirectUrl.append('?').append(httpReq.getQueryString());
            
            httpResp.sendRedirect(redirectUrl.toString());
            
            return;
        }

        String shortName = matcher.group(1);

        String igniteName = IgniteNameRepository.instance().igniteName(shortName);

        Ignite ignite = null;
        
        if (igniteName != null) {
            try {
                ignite = Ignition.ignite(igniteName);
            }
            catch (IgniteIllegalStateException e) {
                // No-op.
            }
        }
        
        if (ignite == null) {
            req.getRequestDispatcher("/igniteNotFound.jsp").forward(req, res);
            
            return;
        }
        
        initAttributes(httpReq, ignite);

        String uri = matcher.group(2);
        
        httpReq.getRequestDispatcher(uri == null ? "/" : uri).forward(req, res);
    }

    /** {@inheritDoc} */
    @Override public void destroy() {
        // No-op.
    }
}
