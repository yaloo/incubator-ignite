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

package org.apache.ignite.internal.processors.cache.multijvm.framework;

import org.apache.ignite.internal.util.typedef.*;
import org.apache.ignite.marshaller.*;

import java.io.*;
import java.util.*;

/**
 * TODO: Add class description.
 */
public class FileMarshallerContext extends MarshallerContextTestImpl {
    /**
     * Default constructor.
     */
    public FileMarshallerContext() {
    }

    /**
     * @param map Map.
     */
    public FileMarshallerContext(Map<Integer, String> map) {
        super(map);
    }

    public void storeToFile(String fileName) throws IOException {
        File file = new File(fileName);

        // TODO: add file created check (and delete the file after tests).
        boolean created = file.createNewFile();

        try(ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file))) {
            out.writeObject(new HashMap<>(map));
        }
    }

    public static FileMarshallerContext fromFile(String fileName) throws IOException, ClassNotFoundException {
        try(ObjectInputStream in = new ObjectInputStream(new FileInputStream(new File(fileName)))) {
            Map<Integer, String> map = (Map<Integer, String>)in.readObject();

            X.println("Map=" + map);

            return new FileMarshallerContext(map);
        }
    }

    public Map getMap() {
        return map;
    }
}
