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

import org.apache.ignite.webconfig.server.dao.*;
import org.apache.ignite.webconfig.server.model.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.*;

/**
 *
 */
public class AuthenticationProviderImpl implements AuthenticationProvider {
    /** */
    private UserDao userDao;

    /**
     * @param userDao User DAO.
     */
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    /** {@inheritDoc} */
    @Override public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = (String) authentication.getPrincipal();
        String password = (String) authentication.getCredentials();

        User user = userDao.findUser(username, password);

        if (user == null)
            throw new BadCredentialsException("Invalid username or password: " + username);

        UserAuthentication customAuthentication = new UserAuthentication("ROLE_USER", authentication);

        customAuthentication.user(user);

        customAuthentication.setAuthenticated(true);

        return customAuthentication;
    }

    /** {@inheritDoc} */
    @Override public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
