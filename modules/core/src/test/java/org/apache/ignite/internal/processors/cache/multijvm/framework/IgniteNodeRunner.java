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

import org.apache.ignite.*;
import org.apache.ignite.configuration.*;
import org.apache.ignite.internal.util.*;
import org.apache.ignite.internal.util.typedef.*;
import org.apache.ignite.internal.util.typedef.internal.*;
import org.apache.ignite.marshaller.optimized.*;
import org.apache.ignite.spi.discovery.tcp.ipfinder.*;
import org.apache.ignite.spi.discovery.tcp.ipfinder.multicast.*;
import org.jetbrains.annotations.*;

import java.io.*;
import java.util.*;

/**
 * Run ignite node.
 */
public class IgniteNodeRunner {
    /** VM ip finder for TCP discovery. */
    public static final TcpDiscoveryIpFinder ipFinder = new TcpDiscoveryMulticastIpFinder();

    /** */
    public static final String IGNITE_CONFIGURATION_TMP_FILE = System.getProperty("java.io.tmpdir") +
        File.separator + "igniteConfiguration.tmp";

    /**
     * Starts {@link Ignite} instance accorging to given arguments.
     *
     * @param args Arguments.
     * @throws Exception If failed.
     */
    public static void main(String[] args) throws Exception {
        try {
            X.println(GridJavaProcess.PID_MSG_PREFIX + U.jvmPid());

            X.println("Starting Ignite Node... Args" + Arrays.toString(args));

            IgniteConfiguration cfg = configuration(IGNITE_CONFIGURATION_TMP_FILE);

            Ignition.start(cfg);
        }
        catch (Throwable e) {
            e.printStackTrace();

            System.exit(1);
        }
    }

    /**
     * @param id Grid id.
     * @param cfg Configuration.
     * @return Given paramethers as command line string arguments.
     */
    public static String asParams(UUID id, IgniteConfiguration cfg) {
        return id.toString() + ' ' + cfg.getGridName();
    }

    /**
     * @param fileName File name of file with serialized configuration.
     * @return Ignite configuration.
     * @throws Exception If failed.
     */
    private static IgniteConfiguration configuration(String fileName) throws Exception {
        try(ObjectInputStream in = new ObjectInputStream(new FileInputStream(new File(fileName)))) {
            OptimizedMarshaller marshaller = new OptimizedMarshaller(false);

            FileMarshallerContext ctx = FileMarshallerContext.fromFile(fileName + "_ctx");

            X.println("Map 2=" + ctx.getMap().toString());

            marshaller.setContext(ctx);

            IgniteConfiguration cfg = (IgniteConfiguration)marshaller.unmarshal(in, U.gridClassLoader());

            X.println(">>>>> Cfg=" + cfg);

            return cfg;
        }
    }

    /**
     * Stors configuration to file.
     *
     * @param fileName File name to store.
     * @param cfg Configuration.
     */
    public static void storeToFile(final String fileName, IgniteConfiguration cfg) throws Exception {
        File ccfgTmpFile = new File(fileName);

        // TODO: add file created check (and delete the file after tests).
        boolean created = ccfgTmpFile.createNewFile();

        try(ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(ccfgTmpFile))) {
            OptimizedMarshaller marshaller = new OptimizedMarshaller(false) {
                @Override public void marshal(@Nullable Object obj, OutputStream out) throws IgniteCheckedException {
                    try {
                        super.marshal(obj, out);

                        ((FileMarshallerContext)ctx).storeToFile(fileName + "_ctx");
                    }
                    catch (IOException e) {
                        throw new IgniteCheckedException(e);
                    }
                }
            };

            marshaller.setContext(new FileMarshallerContext());

            marshaller.marshal(cfg, out);
        }
    }
}
