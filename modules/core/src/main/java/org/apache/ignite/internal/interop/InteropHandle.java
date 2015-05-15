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

package org.apache.ignite.internal.interop;

import org.apache.ignite.*;

/**
 * Interop handle passed to platform for further cooperation.
 */
public interface InteropHandle {
    /**
     * Get Ignite instance associated with this handle.
     *
     * @return Ignite instance.
     */
    public Ignite ignite();

    /**
     * Get environment pointer associated with this handle.
     *
     * @return Environment pointer.
     */
    public long environmentPointer();
}
