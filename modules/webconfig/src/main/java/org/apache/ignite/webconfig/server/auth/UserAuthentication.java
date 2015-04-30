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

import java.util.*;

import org.apache.ignite.webconfig.server.model.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.*;

/**
 *
 */
public class UserAuthentication implements Authentication {
    /** */
    private static final long serialVersionUID = 0L;

    /** */
    private boolean authenticated;

    /** */
    private Collection<GrantedAuthority> grantedAuthorities;

    /** */
    private Authentication authentication;

    /** User. */
    private User user;

    /**
     * @param role Role.
     * @param authentication Authentication.
     */
    public UserAuthentication(String role, Authentication authentication) {
        this.grantedAuthorities = Collections.<GrantedAuthority>singletonList(new SimpleGrantedAuthority(role));
        this.authentication = authentication;
    }

    /** {@inheritDoc} */
    @Override public Collection<GrantedAuthority> getAuthorities() {
        return grantedAuthorities;
    }

    /** {@inheritDoc} */
    @Override public Object getCredentials() {
        return authentication.getCredentials();
    }

    /** {@inheritDoc} */
    @Override public Object getDetails() {
        return authentication.getDetails();
    }

    /** {@inheritDoc} */
    @Override public Object getPrincipal() {
        return authentication.getPrincipal();
    }

    /** {@inheritDoc} */
    @Override public boolean isAuthenticated() {
        return authenticated;
    }

    /** {@inheritDoc} */
    @Override public void setAuthenticated(boolean authenticated) throws IllegalArgumentException {
        this.authenticated = authenticated;
    }

    /** {@inheritDoc} */
    @Override public String getName() {
        return this.getClass().getSimpleName();
    }

    /**
     * @return Authenticated user.
     */
    public User user() {
        return user;
    }

    /**
     * @param user User.
     */
    public void user(User user) {
        this.user = user;
    }
}