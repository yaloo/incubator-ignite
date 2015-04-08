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

package org.apache.ignite.internal.processors.igfs;

import org.apache.ignite.*;
import org.apache.ignite.igfs.*;
import org.apache.ignite.internal.util.future.*;
import org.apache.ignite.internal.util.typedef.internal.*;

import java.io.*;
import java.util.concurrent.*;

/**
 * Work batch is an abstraction of the logically grouped tasks.
 */
public abstract class IgfsFileWorkerBatch implements Runnable {
    /** Stop marker. */
    private static final byte[] STOP_MARKER = new byte[0];

    /** Cancel marker. */
    private static final byte[] CANCEL_MARKER = new byte[0];

    /** Tasks queue. */
    private final BlockingDeque<byte[]> queue = new LinkedBlockingDeque<>();

    /** Future which completes when batch is finished. */
    private final GridFutureAdapter fut = new GridFutureAdapter();

    /** Path to the file in the primary file system. */
    private final IgfsPath path;

    /** Output stream to the file. */
    private final OutputStream out;

    /** Finishing flag. */
    private volatile boolean finishing;

    /**
     * Constructor.
     *
     * @param path Path to the file in the primary file system.
     * @param out Output stream opened to that file.
     */
    IgfsFileWorkerBatch(IgfsPath path, OutputStream out) {
        assert path != null;
        assert out != null;

        this.path = path;
        this.out = out;
    }

    /**
     * Perform write if batch is not finishing yet.
     *
     * @param data Data to be written.
     * @return {@code True} in case write was enqueued.
     */
    synchronized boolean write(final byte[] data) {
        if (!finishing) {
            queue.add(data);

            return true;
        }
        else
            return false;
    }

    /**
     * Add the last task to that batch which will release all the resources.
     */
    synchronized void finish() {
        if (!finishing) {
            finishing = true;

            queue.add(STOP_MARKER);
        }
    }

    /**
     * Cancel batch processing.
     */
    synchronized void cancel() {
        queue.addFirst(CANCEL_MARKER);
    }

    /**
     * @return {@code True} if finish was called on this batch.
     */
    boolean finishing() {
        return finishing;
    }

    /**
     * Process the batch.
     */
    public void run() {
        try {
            while (!fut.isDone()) {
                try {
                    byte[] data = queue.poll(1000, TimeUnit.MILLISECONDS);

                    if (data == null)
                        continue;
                    else if (data == STOP_MARKER) {
                        assert queue.isEmpty();

                        fut.onDone();
                    }
                    else if (data == CANCEL_MARKER)
                        throw new IgniteCheckedException("Write to file was cancelled due to node stop.");

                    try {
                        out.write(data);
                    }
                    catch (IOException e) {
                        throw new IgniteCheckedException("Failed to write data to the file due to secondary " +
                            "file system exception: " + path, e);
                    }
                }
                catch (InterruptedException e) {
                    Thread.currentThread().interrupt();

                    fut.onDone(e);
                }
                catch (Exception e) {
                    fut.onDone(e);
                }
            }
        }
        finally {
            assert fut.isDone();

            U.closeQuiet(out);

            onDone();
        }
    }

    /**
     * Await for that worker batch to complete.
     *
     * @throws IgniteCheckedException In case any exception has occurred during batch tasks processing.
     */
    void await() throws IgniteCheckedException {
        fut.get();
    }

    /**
     * Get primary file system path.
     *
     * @return Primary file system path.
     */
    IgfsPath path() {
        return path;
    }

    /**
     * Callback invoked when execution finishes.
     */
    protected abstract void onDone();
}
