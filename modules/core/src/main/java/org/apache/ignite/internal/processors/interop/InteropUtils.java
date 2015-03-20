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

package org.apache.ignite.internal.processors.interop;

import org.apache.ignite.*;
import org.apache.ignite.internal.*;

import java.io.*;

/**
 *
 */
public class InteropUtils {
    /**
     * @param ignite Ignite.
     * @return Interop processor.
     */
    public static InteropProcessor interop(Ignite ignite) {
        GridKernalContext ctx = ((IgniteKernal)ignite).context();

        return ctx.interop();
    }

    /**
     * @param cb Callback address.
     * @param cbArg Value passed to callback.
     * @param resType Result type.
     * @param res Result.
     */
    native public static void asyncCallback(long cb, long cbArg, int resType, long res);
}
