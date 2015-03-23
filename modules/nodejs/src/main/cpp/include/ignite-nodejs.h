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

#ifndef MYOBJECT_H
#define MYOBJECT_H

#include <node.h>
#include <uv.h>
#include <node_object_wrap.h>
#include "ignite-interop-api.h"

class IgniteNodeJsCache : public node::ObjectWrap {
public:
	static void Init(v8::Handle<v8::Object> exports);

private:
	IgniteNodeJsCache(ignite::IgniteCache* cache);

	~IgniteNodeJsCache();

	static void New(const v8::FunctionCallbackInfo<v8::Value>& args);

	static void Put(const v8::FunctionCallbackInfo<v8::Value>& args);

	static void PutAsync(const v8::FunctionCallbackInfo<v8::Value>& args);

	static void ObjectInfo(const v8::FunctionCallbackInfo<v8::Value>& args);

	static v8::Persistent<v8::Function> constructor;

	ignite::IgniteCache* cache;
};

#endif