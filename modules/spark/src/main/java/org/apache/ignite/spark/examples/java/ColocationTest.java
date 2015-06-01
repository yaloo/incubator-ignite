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

package org.apache.ignite.spark.examples.java;

import org.apache.ignite.configuration.*;
import org.apache.ignite.lang.*;
import org.apache.ignite.spark.*;
import org.apache.ignite.spark.examples.*;
import org.apache.spark.*;
import org.apache.spark.api.java.*;
import org.apache.spark.api.java.function.Function;
import scala.*;

import java.util.*;

public class ColocationTest {
    public static void main(String[] args) {
        SparkConf conf = new SparkConf();

        conf.setAppName("Colocation test");

        JavaSparkContext sc = new JavaSparkContext("local[*]", "Colocation test", conf);

        JavaIgniteContext<Integer, Integer> ignite = new JavaIgniteContext<>(sc, new IgniteOutClosure<IgniteConfiguration>() {
            @Override public IgniteConfiguration apply() {
                return ExampleConfiguration.configuration();
            }
        });

        JavaIgniteRDD<Integer, Integer> cache = ignite.fromCache("partitioned");

        List<Integer> seq = new ArrayList<>();

        long sum = 0;

        for (int i = 0; i < 100000; i++) {
            seq.add(i);

            sum += i;
        }

        cache.savePairs(sc.parallelize(seq, 48).map(new Function<Integer, Tuple2<Integer, Integer>>() {
            @Override public Tuple2<Integer, Integer> call(Integer v1) throws Exception {
                return new Tuple2<>(v1, v1);
            }
        }));

        // Execute parallel sum.
        System.out.println("Local sum: " + sum);

        Function1<Tuple2<Integer, Integer>, Integer> f = new Function1<Tuple2<Integer, Integer>, Integer>() {
            @Override public Integer apply(Tuple2<Integer, Integer> t) {
                return t._2();
            }
        };

        //System.out.println("Distributed sum: " + cache.map(f).sum())
    }
}
