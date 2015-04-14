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

package org.apache.ignite.internal.processors.hadoop.examples;

import org.apache.hadoop.conf.*;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.input.*;
import org.apache.hadoop.mapreduce.lib.output.*;
import org.apache.ignite.internal.processors.hadoop.*;

import java.io.*;

/**
 * Example job for testing hadoop task execution.
 */
public class HadoopWordCount2 {
    /**
     * Entry point to start job.
     *
     * @param args Command line parameters.
     * @throws Exception If fails.
     */
    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.out.println("usage: [input] [output]");
            System.exit(-1);
        }

        Job job = getJob(args[0], args[1]);

        job.submit();

        printCounters(job);

        job.waitForCompletion(true);

        printCounters(job);
    }

    private static void printCounters(Job job) throws IOException {
        Counters counters = job.getCounters();

        for (CounterGroup group : counters) {
            System.out.println("Group: " + group.getDisplayName() + "," + group.getName());
            System.out.println("  number of counters: " + group.size());
            for (Counter counter : group) {
                System.out.println("  - " + counter.getDisplayName() + ": " + counter.getName() + ": "+counter.getValue());
            }
        }
    }

    /**
     * Gets fully configured Job instance.
     *
     * @param input Input file name.
     * @param output Output directory name.
     * @return Job instance.
     * @throws IOException If fails.
     */
    public static Job getJob(String input, String output) throws IOException {
        Configuration cfg = HadoopStartup.configuration();

        Job job = Job.getInstance(cfg);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);

        setTasksClasses(job, true, true, true);

        FileInputFormat.setInputPaths(job, new Path(input));
        FileOutputFormat.setOutputPath(job, new Path(output));

        job.setJarByClass(HadoopWordCount2.class);

        return job;
    }

    /**
     * Sets task classes with related info if needed into configuration object.
     *
     * @param job Configuration to change.
     * @param setMapper Option to set mapper and input format classes.
     * @param setCombiner Option to set combiner class.
     * @param setReducer Option to set reducer and output format classes.
     */
    public static void setTasksClasses(Job job, boolean setMapper, boolean setCombiner, boolean setReducer) {
        if (setMapper) {
            job.setMapperClass(HadoopWordCount2Mapper.class);
            job.setInputFormatClass(TextInputFormat.class);
        }

        if (setCombiner)
            job.setCombinerClass(HadoopWordCount2Reducer.class);

        if (setReducer) {
            job.setReducerClass(HadoopWordCount2Reducer.class);
            job.setOutputFormatClass(TextOutputFormat.class);
        }
    }
}
