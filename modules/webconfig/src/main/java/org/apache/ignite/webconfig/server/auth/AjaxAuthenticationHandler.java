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

package org.apache.ignite.webconfig.server.auth;

import org.springframework.security.core.*;
import org.springframework.security.web.authentication.*;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;

/**
 *
 */
public class AjaxAuthenticationHandler implements AuthenticationSuccessHandler, AuthenticationFailureHandler {
    /** {@inheritDoc} */
    @Override public void onAuthenticationFailure(
        HttpServletRequest req,
        HttpServletResponse res,
        AuthenticationException e
    ) throws IOException, ServletException {
        res.setStatus(HttpServletResponse.SC_OK);
        res.setContentType("application/json");
        writeResponse(res.getOutputStream(), null, e);
    }

    /** {@inheritDoc} */
    @Override public void onAuthenticationSuccess(
        HttpServletRequest req,
        HttpServletResponse res,
        Authentication authentication
    ) throws IOException, ServletException {
        res.setStatus(HttpServletResponse.SC_OK);
        res.setContentType("application/json");
        writeResponse(res.getOutputStream(), authentication, null);
    }

    /**
     * @param out Output stream.
     * @param auth Authentication.
     * @param e Error, if any.
     * @throws IOException If failed to write response.
     */
    private void writeResponse(OutputStream out, Authentication auth, AuthenticationException e) throws IOException {
        try {
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(out));

            pw.write("{\"success\": ");
            pw.write(auth == null ? "false" : "true");

            if (e != null) {
                pw.write(", \"err\": \"");
                pw.write(e.getMessage());
                pw.write("\"");
            }

            pw.write("}");

            pw.flush();
        }
        finally {
            out.close();
        }
    }
}
