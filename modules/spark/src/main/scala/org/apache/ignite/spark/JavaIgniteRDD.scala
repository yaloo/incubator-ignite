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

package org.apache.ignite.spark

import org.apache.ignite.lang.IgniteBiTuple
import org.apache.ignite.spark.impl.IgniteAbstractRDD
import org.apache.spark.api.java.JavaRDD
import org.apache.spark.rdd.RDD
import org.apache.spark.{Partition, TaskContext}

import scala.collection.JavaConversions._
import scala.language.implicitConversions
import scala.reflect.ClassTag


class JavaIgniteRDD[K, V](val rdd: IgniteRDD[K, V])(implicit val ktag: ClassTag[K], implicit val vtag: ClassTag[V])
    extends IgniteAbstractRDD[(K, V), K, V](rdd.ic, rdd.cacheName, rdd.cacheCfg) {

    /**
     * Computes iterator based on given partition.
     *
     * @param part Partition to use.
     * @param context Task context.
     * @return Partition iterator.
     */
    override def compute(part: Partition, context: TaskContext): Iterator[(K, V)] = {
        rdd.compute(part, context)
    }

    /**
     * Gets partitions for the given cache RDD.
     *
     * @return Partitions.
     */
    override protected def getPartitions: Array[Partition] = {
        rdd.getPartitions
    }

    /**
     * Gets preferred locations for the given partition.
     *
     * @param split Split partition.
     * @return
     */
    override protected def getPreferredLocations(split: Partition): Seq[String] = {
        rdd.getPreferredLocations(split)
    }

    def objectSql(typeName: String, sql: String, args: Any*): JavaRDD[IgniteBiTuple[K, V]] =
        JavaRDD.fromRDD(rdd.objectSql(typeName, sql, args)).map(tuple2BiTuple[K, V](_))

    def sql(sql: String, args: Any*): JavaRDD[Seq[Any]] = JavaRDD.fromRDD(rdd.sql(sql, args))

    def saveValues(jrdd: JavaRDD[V]) = rdd.saveValues(JavaRDD.toRDD(jrdd))

    def savePairs(jrdd: JavaRDD[(K, V)]) = rdd.savePairs(JavaRDD.toRDD(jrdd))

    def clear(): Unit = rdd.clear()

    implicit def tuple2BiTuple[A, B](tuple: (A, B)): IgniteBiTuple[A, B] =
        new IgniteBiTuple[A, B](tuple._1, tuple._2)

    implicit def tupleIt2BiTupleIt[A, B](it: Iterator[(A, B)]): java.util.Iterator[IgniteBiTuple[A, B]] =
        new java.util.Iterator[IgniteBiTuple[A, B]] {
            val target: java.util.Iterator[(A, B)] = it

            override def next(): IgniteBiTuple[A, B] = target.next()

            override def remove(): Unit = target.remove()

            override def hasNext: Boolean = target.hasNext
        }
}

object JavaIgniteRDD {
    implicit def fromIgniteRDD[K: ClassTag, V: ClassTag](rdd: IgniteRDD[K, V]): JavaIgniteRDD[K, V] =
        new JavaIgniteRDD[K, V](rdd)

    implicit def toIgniteRDD[K, V](rdd: JavaIgniteRDD[K, V]): IgniteRDD[K, V] = rdd.rdd
}

object JavaRDD {
    implicit def fromRDD[T: ClassTag](rdd: RDD[T]): JavaRDD[T] = new JavaRDD[T](rdd)

    implicit def toRDD[T](rdd: JavaRDD[T]): RDD[T] = rdd.rdd
}
