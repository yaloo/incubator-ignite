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
import org.apache.ignite.internal.util.typedef.*;

/**
 *
 */
public abstract class InteropTargetAdapter implements InteropTarget {
    /** */
    private InteropProcessor proc;

    /** */
    protected IgniteLogger log;

    /**
     * @param proc Processor.
     */
    public InteropTargetAdapter(InteropProcessor proc) {
        this.proc = proc;

        log = proc.context().log(getClass());
    }

    /** {@inheritDoc} */
    @Override public int inOp(int type, long ptr, int len) throws IgniteCheckedException {
        InteropOffheapInputStream in = new InteropOffheapInputStream(ptr, len);

        InteropMarshaller marsh = proc.marshaller();

        return inOp(type, in, marsh);
    }

    /**
     * @param type Type.
     * @param in Input.
     * @param marsh Marshaller.
     * @return Operation result.
     * @throws org.apache.ignite.IgniteCheckedException If failed.
     */
    protected abstract int inOp(int type,
        InteropInputStream in,
        InteropMarshaller marsh)
        throws IgniteCheckedException;

    /** {@inheritDoc} */
    @Override public long inOutOp(int type, long ptr, int len) throws IgniteCheckedException {
        InteropOffheapInputStream in = new InteropOffheapInputStream(ptr, len);

        InteropMarshaller marsh = proc.marshaller();

        InteropOffheapOutputStream out = new InteropOffheapOutputStream(1024);

        inOutOp(type, in, out, marsh);

        return 0;
    }

    /**
     * @param type Type.
     * @param in Input.
     * @param out Output.
     * @param marsh Marshaller.
     * @throws org.apache.ignite.IgniteCheckedException If failed.
     */
    protected abstract void inOutOp(int type,
        InteropInputStream in,
        InteropOutputStream out,
        InteropMarshaller marsh)
        throws IgniteCheckedException;

    /** {@inheritDoc} */
    @Override public void inOutOpAsync(int type,
        long ptr,
        int len,
        final long cb,
        final long cbData)
        throws IgniteCheckedException
    {
        InteropOffheapInputStream in = new InteropOffheapInputStream(ptr, len);

        InteropMarshaller marsh = proc.marshaller();

        InteropOffheapOutputStream out = new InteropOffheapOutputStream(1024);

        IgniteInternalFuture<Void> fut = inOutAsyncOp(type, in, out, marsh);

        fut.listen(new CI1<IgniteInternalFuture>() {
            @Override public void apply(final IgniteInternalFuture fut) {
                proc.context().closure().runLocalSafe(new Runnable() {
                    @Override public void run() {
                        try {
                            Thread.sleep(500);

                            fut.get();

                            InteropUtils.asyncCallback(cb, cbData, 1, 0);
                        }
                        catch (Throwable e) {
                            e.printStackTrace();

                            InteropUtils.asyncCallback(cb, cbData, -1, 0);
                        }
                    }
                });
            }
        });
    }

    /**
     * @param type Type.
     * @param in Input.
     * @param out Output.
     * @param marsh Marshaller.
     * @return Future.
     */
    protected abstract IgniteInternalFuture<Void> inOutAsyncOp(int type,
        InteropInputStream in,
        InteropOutputStream out,
        InteropMarshaller marsh);
}
