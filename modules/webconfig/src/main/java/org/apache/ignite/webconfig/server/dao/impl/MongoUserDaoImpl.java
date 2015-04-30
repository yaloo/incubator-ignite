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

import com.mongodb.*;
import org.apache.ignite.webconfig.server.dao.*;
import org.apache.ignite.webconfig.server.model.*;
import org.bson.types.*;
import org.springframework.beans.factory.*;

import java.util.*;

/**
 *
 */
public class MongoUserDaoImpl implements UserDao, InitializingBean {
    /** */
    private MongoSpringBean mongo;

    /**
     * @param mongo Mongo spring bean.
     */
    public void setMongo(MongoSpringBean mongo) {
        this.mongo = mongo;
    }

    /** {@inheritDoc} */
    @Override public void afterPropertiesSet() throws Exception {
        try {
            mongo.db().getCollection("users").createIndex(new BasicDBObject("login", 1), new BasicDBObject("unique", true));
        }
        catch (Exception ignored) {
            // No-op.
        }
    }

    /** {@inheritDoc} */
    @Override public User findUser(String login, String password) {
        DBCollection users = db().getCollection("users");

        DBObject search = new BasicDBObject("login", login).append("pass", password);

        try (DBCursor cur = users.find(search)) {
            if (cur.hasNext()) {
                DBObject user = cur.next();

                return userFromDbObject(user);
            }
        }

        return null;
    }

    /** {@inheritDoc} */
    @Override public User createUser(User user, String password) {
        try {
            BasicDBObject usr = new BasicDBObject("login", user.login()).append("pass", password);

            db().getCollection("users").insert(usr);

            ObjectId id = (ObjectId)usr.get("_id");

            if (id != null)
                user.id(id.toHexString());

            return user;
        }
        catch (DuplicateKeyException e) {
            return null;
        }
    }

    /** {@inheritDoc} */
    @Override public Collection<User> allUsers() {
        Collection<User> res = new ArrayList<>();

        try (DBCursor cur = db().getCollection("users").find()) {
            while (cur.hasNext()) {
                DBObject usrObj = cur.next();

                res.add(userFromDbObject(usrObj));
            }
        }

        return res;
    }

    /**
     * @return Mongo database.
     */
    private DB db() {
        return mongo.db();
    }

    /**
     * @param obj Object to build user from.
     * @return User.
     */
    private User userFromDbObject(DBObject obj) {
        User usr = new User((String)obj.get("login"));

        ObjectId id = (ObjectId)obj.get("_id");

        if (id != null)
            usr.id(id.toHexString());

        return usr;
    }

    public static void main(String[] args) throws Exception {
        MongoSpringBean bean = new MongoSpringBean();

        bean.setHost("localhost");
        bean.setDbName("igniteweb");

        bean.afterPropertiesSet();

//        bean.db().getCollection("users").dropIndexes();
        bean.db().getCollection("users").remove(new BasicDBObject());
    }
}
