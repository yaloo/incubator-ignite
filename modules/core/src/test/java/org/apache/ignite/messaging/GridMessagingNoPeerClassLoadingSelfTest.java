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

package org.apache.ignite.messaging;

import org.apache.ignite.*;
import org.apache.ignite.configuration.*;
import org.apache.ignite.internal.util.typedef.*;
import org.apache.ignite.resources.*;
import org.apache.ignite.testframework.config.*;

import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

/**
 * Tests for Messaging public API with disabled
 * peer class loading.
 */
public class GridMessagingNoPeerClassLoadingSelfTest extends GridMessagingSelfTest {
    /** {@inheritDoc} */
    @Override protected IgniteConfiguration getConfiguration(String gridName) throws Exception {
        IgniteConfiguration cfg = super.getConfiguration(gridName);

        cfg.setPeerClassLoadingEnabled(false);

        return cfg;
    }

    /**
     * Checks that the message, loaded with external
     * class loader, won't be unmarshalled on remote node, because
     * peer class loading is disabled.
     *
     * @throws Exception If error occurs.
     */
    @Override public void testSendMessageWithExternalClassLoader() throws Exception {
        URL[] urls = new URL[] { new URL(GridTestProperties.getProperty("p2p.uri.cls")) };

        ClassLoader extLdr = new URLClassLoader(urls);

        Class rcCls = extLdr.loadClass(EXT_RESOURCE_CLS_NAME);

        MessageListener list = new MessageListener(ignite1);

        ignite2.message().remoteListen("", list);

        message(ignite1.cluster().forRemotes()).send(null, Collections.singleton(rcCls.newInstance()));

        /*
            We shouldn't get a message, because remote node won't be able to
            unmarshal it (peer class loading is disabled.)
         */
        assertFalse(list.rcvLatch.await(3, TimeUnit.SECONDS));
    }

    /**
     *
     */
    private static class MessageListener<UUID, Object> implements P2<UUID, Object> {
        /** */
        final AtomicBoolean error = new AtomicBoolean(false); //to make it modifiable

        /** */
        final CountDownLatch rcvLatch = new CountDownLatch(1);

        /** */
        final Ignite sender;

        /** */
        @LoggerResource
        private transient IgniteLogger log;

        /**
         * @param sender
         */
        private MessageListener(Ignite sender) {
            this.sender = sender;
        }

        /** {@inheritDoc} */
        @Override public boolean apply(UUID nodeId, Object msg) {
            try {
                log.info("Received new message [msg=" + msg + ", senderNodeId=" + nodeId + ']');

                if (!nodeId.equals(sender.cluster().localNode().id())) {
                    log.error("Unexpected sender node: " + nodeId);

                    error.set(true);

                    return false;
                }

                return true;
            }
            finally {
                rcvLatch.countDown();
            }
        }
    }
}
