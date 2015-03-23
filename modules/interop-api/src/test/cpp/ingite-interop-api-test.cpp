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

#define CATCH_CONFIG_MAIN
#include "catch.hpp"

#include <iostream>

#include "ignite-interop-api.h"

using namespace ignite;
using namespace std;

class TestError {
};

void testErrorCallback(const string& errMsg, const string& errCls) {
	cout << "Test error callback [err=" << errMsg << "]" << endl;
	
	throw TestError();
}

TEST_CASE("Stream", "[ignite]") {
	IgniteOutputStream out(1);

	REQUIRE(sizeof(out) == 16);

	out.writeByte(1);
	out.writeInt(1024);
	out.writeLong(2048);

	out.writeByte(-1);
	out.writeInt(-1024);
	out.writeLong(-2048);

	IgniteInputStream in(out.dataPointer(), out.position(), out.allocatedSize());

	REQUIRE(in.readByte() == 1);
	REQUIRE(in.readInt() == 1024);
	REQUIRE(in.readLong() == 2048);

	REQUIRE(in.readByte() == -1);
	REQUIRE(in.readInt() == -1024);
	REQUIRE(in.readLong() == -2048);
}

TEST_CASE("StartIgnite", "[ignite]") {
	IgniteJvm* jvm = testIgniteJvmStart(testErrorCallback);

    REQUIRE(jvm);

	REQUIRE_THROWS_AS(jvm->startIgnite("invalid config", "ignite1"), TestError);

    Ignite* ignite = jvm->startIgnite("modules\\interop-api\\src\\test\\config\\test-single-node.xml", "ignite1");
    
    REQUIRE(ignite);

    IgniteCache* cache = ignite->cache("cache1");

    REQUIRE(cache);

    delete cache;

    delete ignite;

    delete jvm;
}
