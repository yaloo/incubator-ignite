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

import org.apache.ignite.webconfig.server.auth.*;
import org.apache.ignite.webconfig.server.dao.*;
import org.apache.ignite.webconfig.server.model.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.http.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.*;
import org.springframework.security.core.context.*;
import org.springframework.security.web.authentication.*;
import org.springframework.stereotype.*;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.*;

/**
 *
 */
@Controller
@RequestMapping("/users")
public class UsersController {
    /** */
    @Autowired
    private UserDao userDao;

    /** */
    @Autowired
    private AuthenticationManager authenticationManager;

    /**
     * @param userDao User DAO.
     */
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    /**
     * @param authenticationManager Authentication manager.
     */
    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    /**
     * @param login User name.
     * @param password User password.
     * @param confirmPassword Confirmed user password.
     * @return Response body.
     */
    @ResponseBody
    @RequestMapping(value = "register.do", method = RequestMethod.POST)
    public ResponseEntity<String> registerUser(
        @RequestParam("login") String login,
        @RequestParam("password") String password,
        @RequestParam("confirmPassword") String confirmPassword,
        HttpServletRequest request,
        HttpServletResponse response) {
        if (!password.equals(confirmPassword))
            return new ResponseEntity<>("Passwords do not match.", HttpStatus.BAD_REQUEST);

        User user = new User(login);

        user = userDao.createUser(user, password);

        JsonResponseBean res = new JsonResponseBean(user != null, user == null ? "User with the given login already exists" : "");

        if (user != null) {
            String username = user.login();

            UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(username, password);

            // generate session if one doesn't exist
            request.getSession();

            token.setDetails(new WebAuthenticationDetails(request));

            Authentication authenticatedUser = authenticationManager.authenticate(token);

            SecurityContextHolder.getContext().setAuthentication(authenticatedUser);

            res.addField("user", user.toJson());
        }

        HttpHeaders hdrs = new HttpHeaders();

        hdrs.setContentType(MediaType.APPLICATION_JSON);

        return new ResponseEntity<>(res.toJson(), hdrs, HttpStatus.OK);
    }

    /**
     * @return Response body.
     */
    @ResponseBody
    @RequestMapping(value = "current.do", method = RequestMethod.POST)
    public ResponseEntity<String> currentUser() {
        UserAuthentication auth = (UserAuthentication) SecurityContextHolder.getContext().getAuthentication();

        JsonResponseBean res = new JsonResponseBean(auth != null, auth == null ? "Not authenticated" : "");

        if (auth != null)
            res.addField("user", auth.user().toJson());

        HttpHeaders hdrs = new HttpHeaders();

        hdrs.setContentType(MediaType.APPLICATION_JSON);

        return new ResponseEntity<>(res.toJson(), hdrs, HttpStatus.OK);
    }
}
