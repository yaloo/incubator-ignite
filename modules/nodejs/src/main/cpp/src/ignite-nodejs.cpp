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

#include "ignite-nodejs.h"

#include <assert.h>
#include <iostream>
#include <stdlib.h>
#include <string>

using namespace std;
using namespace v8;
using namespace ignite;

Persistent<Function> IgniteNodeJs::constructor;

IgniteJvm* IgniteNodeJs::jvm = NULL;

Persistent<Function> IgniteNodeJsCache::constructor;

void igniteErrorCallback(const string& errMsg, const string& errCls) {
	Isolate* isolate = Isolate::GetCurrent();
	
	assert(isolate);

	HandleScope scope(isolate);

	isolate->ThrowException(Exception::Error(String::NewFromUtf8(isolate, errMsg.c_str())));
}

IgniteNodeJs::IgniteNodeJs(Ignite* ignite) : ignite(ignite) {
	// No-op
}

IgniteNodeJs::~IgniteNodeJs() {
	delete ignite;
}

void IgniteNodeJs::IgniteStart(const FunctionCallbackInfo<Value>& args) {
	// TODO 496: parse arguments.

	if (!jvm) {
		jvm = testIgniteJvmStart(igniteErrorCallback);

		if (!jvm)
			return;
	}

	Ignite* ignite = jvm->startIgnite("modules\\interop-api\\src\\test\\config\\test-single-node.xml", "ignite1");

	if (!ignite)
		return;

	Isolate* isolate = Isolate::GetCurrent();

	Local<Function> cons = Local<Function>::New(isolate, constructor);
	Local<Object> instance = cons->NewInstance();

	IgniteNodeJs* obj = new IgniteNodeJs(ignite);

	obj->Wrap(instance);

	args.GetReturnValue().Set(instance);
}

void IgniteNodeJs::New(const FunctionCallbackInfo<Value>& args) {
}

void IgniteNodeJs::Init(Handle<Object> exports) {
	NODE_SET_METHOD(exports, "start", IgniteStart);
	
	Isolate* isolate = Isolate::GetCurrent();

	Local<FunctionTemplate> tpl = FunctionTemplate::New(isolate, New);
	tpl->SetClassName(String::NewFromUtf8(isolate, "Ignite"));
	tpl->InstanceTemplate()->SetInternalFieldCount(1);

	NODE_SET_PROTOTYPE_METHOD(tpl, "cache", Cache);

	constructor.Reset(isolate, tpl->GetFunction());
}

IgniteCache* IgniteNodeJs::cache(char* cacheName) {
	return ignite->cache(cacheName);
}

void IgniteNodeJs::Cache(const FunctionCallbackInfo<Value>& args) {
	Isolate* isolate = Isolate::GetCurrent();
	HandleScope scope(isolate);

	IgniteNodeJs* ignite = ObjectWrap::Unwrap<IgniteNodeJs>(args.Holder());

	IgniteCache* cache = 0;
		
	if (!args[0]->IsUndefined()) {
		Local<String> name = args[0]->ToString();

		v8::String::Utf8Value utf8(name);
			
		cache = ignite->cache(*utf8);
	}
	else
		cache = ignite->cache(NULL);

	if (!cache)
		return;

	Local<Function> cons = Local<Function>::New(isolate, IgniteNodeJsCache::constructor);
	Local<Object> instance = cons->NewInstance();

	IgniteNodeJsCache* obj = new IgniteNodeJsCache(cache);

	obj->Wrap(instance);

	args.GetReturnValue().Set(instance);

}

IgniteNodeJsCache::IgniteNodeJsCache(IgniteCache* cache) : cache(cache) {
	// No-op
}

IgniteNodeJsCache::~IgniteNodeJsCache() {
	delete cache;
}

void IgniteNodeJsCache::Init(Handle<Object> exports) {
	Isolate* isolate = Isolate::GetCurrent();

	Local<FunctionTemplate> tpl = FunctionTemplate::New(isolate, New);
	tpl->SetClassName(String::NewFromUtf8(isolate, "IgniteCache"));
	tpl->InstanceTemplate()->SetInternalFieldCount(1);

	NODE_SET_PROTOTYPE_METHOD(tpl, "put", Put);
	NODE_SET_PROTOTYPE_METHOD(tpl, "putAsync", PutAsync);
	NODE_SET_PROTOTYPE_METHOD(tpl, "objectInfo", ObjectInfo);

	constructor.Reset(isolate, tpl->GetFunction());
}

void NewInstance(ignite::IgniteCache* cache, const FunctionCallbackInfo<Value>& args) {

}

void IgniteNodeJsCache::New(const FunctionCallbackInfo<Value>& args) {
}

void IgniteNodeJsCache::Put(const FunctionCallbackInfo<Value>& args) {
	Isolate* isolate = Isolate::GetCurrent();
	HandleScope scope(isolate);

	IgniteNodeJsCache* cache = ObjectWrap::Unwrap<IgniteNodeJsCache>(args.Holder());

	if (args.Length() != 2) {
		std::cout << "Two arguments expected.\n";

		return;
	}

	int key = args[0]->Int32Value();
	int val = args[1]->Int32Value();

	std::cout << "Put key=" << key << ", val=" << val << "\n";

	IgniteOutputStream out(8);

	out.writeInt(key);
	out.writeInt(val);

	// cache->cache->put(out.dataPointer(), out.allocatedSize());
}

struct AsyncData
{
	Persistent<Function> cb;

	uv_async_t *async;
};

void QueueWorkNoop(uv_work_t* req)
{
}

void AfterQueueWork(uv_work_t* req)
{
}

void freeCallback(uv_async_t *async) {
	std::cout << "NodeJs free async handle";

	delete async;
}

void AfterExecute(uv_async_t *async)
{
	std::cout << "NodeJs after execute";

	Isolate* isolate = Isolate::GetCurrent();

	HandleScope scope(isolate);

	AsyncData* asyncData = (AsyncData*)async->data;

	uv_close((uv_handle_t*)async, (uv_close_cb)freeCallback);

	Local<Function> cb = Local<Function>::New(isolate, asyncData->cb);

	asyncData->cb.Reset();

	delete asyncData;

	const unsigned argc = 1;

	Local<Value> argv[argc] = { String::NewFromUtf8(isolate, "hello world") };

	cb->Call(isolate->GetCurrentContext()->Global(), argc, argv);

	node::MakeCallback(isolate,
		isolate->GetCurrentContext()->Global(),
		cb,
		argc,
		argv);
}

void igniteAsyncCallback(AsyncData* asyncData, int resType, void* res)
{
	std::cout << "NodeJs async callback, resType=" << resType << "\n";

	uv_async_send(asyncData->async);
}

void IgniteNodeJsCache::PutAsync(const FunctionCallbackInfo<Value>& args) {
	/*
	Isolate* isolate = Isolate::GetCurrent();
	HandleScope scope(isolate);

	IgniteCache* cache = ObjectWrap::Unwrap<IgniteCache>(args.Holder());

	if (args.Length() != 3) {
		std::cout << "Three arguments expected.\n";

		return;
	}

	int key = args[0]->Int32Value();
	int val = args[1]->Int32Value();

	Local<Function> cb = Local<Function>::Cast(args[2]);
	
	AsyncData* asyncData = new AsyncData();

	// asyncData = Persistent<Function>(isolate, cb);
	asyncData->cb.Reset(isolate, cb);

	std::cout << "Put async key=" << key << ", val=" << val << "\n";

	InterupOutputStream out(8);

	out.writeInt32(key);
	out.writeInt32(val);

	uv_async_t* async = new uv_async_t();
	
	async->data = asyncData;

	asyncData->async = async;

	uv_async_init(uv_default_loop(), async, AfterExecute);

	cache->cache->putAsync(out.data(), out.size(), (IgniteAsyncCallback)igniteAsyncCallback, asyncData);

	// uv_queue_work(uv_default_loop(), 0, QueueWorkNoop, (uv_after_work_cb)AfterQueueWork);

	//obj->value_ += 1;
	//args.GetReturnValue().Set(Number::New(isolate, obj->value_));
	*/
}

void printPropertis(Local<Value> prop) {
	if (prop->IsObject()) {
		Local<Object> obj = Local<Object>::Cast(prop);

		std::cout << "Constructor: " << *v8::String::Utf8Value(obj->GetConstructorName()) << "\n";

		Local<Array> props = obj->GetPropertyNames();

		std::cout << "Properties (" << props->Length() << "):\n";

		for (int i = 0; i < props->Length(); i++) {
			Local<Value> prop = props->Get(i);

			Local<Value> val = obj->Get(prop);

			if (!val->IsFunction())
				std::cout << *v8::String::Utf8Value(prop->ToString()) << "=" << *v8::String::Utf8Value(val->ToString()) << "\n";

			/*
			std::string name(*v8::String::Utf8Value(prop->ToString()));

			if (name != "global" && name != "EventEmitter")
				printPropertis(val);*/
		}
	}
}
void IgniteNodeJsCache::ObjectInfo(const FunctionCallbackInfo<Value>& args) {
	/*
	Isolate* isolate = Isolate::GetCurrent();
	HandleScope scope(isolate);

	IgniteCache* cache = ObjectWrap::Unwrap<IgniteCache>(args.Holder());

	if (args.Length() != 1 || !args[0]->IsObject()) {
		std::cout << "One object argument is expected.\n";

		return;
	}

	Local<Object> obj = Local<Object>::Cast(args[0]);

	std::cout << "Constructor: " << *v8::String::Utf8Value(obj->GetConstructorName()) << "\n";

	Local<Array> props = obj->GetPropertyNames();

	std::cout << "Properties (" << props->Length() << "):\n";

	for (int i = 0; i < props->Length(); i++) {
		Local<Value> prop = props->Get(i);

		Local<Value> val = obj->Get(prop);

		if (!val->IsFunction()) {
			std::cout << *v8::String::Utf8Value(prop->ToString()) << "=" << *v8::String::Utf8Value(val->ToString()) << "\n";
		}
	}

	Handle<v8::Object> global = isolate->GetCurrentContext()->Global();

	Local<Object> process = Local<Object>::Cast(global->Get(String::NewFromUtf8(isolate, "process")));

	printPropertis(process);
	/*
	Handle<v8::Object> global = isolate->GetCurrentContext()->Global();

	Local<Object> process = Local<Object>::Cast(global->Get(String::NewFromUtf8(isolate, "process")));

	Local<Object> module = Local<Object>::Cast(process->Get(String::NewFromUtf8(isolate, "mainModule")));

	Local<Object> children = Local<Object>::Cast(module->Get(String::NewFromUtf8(isolate, "children")));

	printPropertis(children);
	*/
	/*
	Local<Object> obj = Local<Object>::Cast(args[0]);
	
	std::cout << "Constructor: " << *v8::String::Utf8Value(obj->GetConstructorName()) << "\n";

	Local<Array> props = obj->GetPropertyNames();

	std::cout << "Properties (" << props->Length() << "):\n";

	for (int i = 0; i < props->Length(); i++) {
		Local<Value> prop = props->Get(i);

		Local<Value> val = obj->Get(prop);

		if (!val->IsFunction()) {
			std::cout << *v8::String::Utf8Value(prop->ToString()) << "=" << *v8::String::Utf8Value(val->ToString()) << "\n";
		}
	}

	Local<v8::Context> context = obj->CreationContext();

	Handle<v8::Object> global = context->Global();
	
	props = global->GetPropertyNames();

	std::cout << "Global properties (" << props->Length() << "):\n";

	for (int i = 0; i < props->Length(); i++) {
		Local<Value> prop = props->Get(i);

		Local<Value> val = obj->Get(prop);

		//if (!val->IsFunction()) {
			std::cout << *v8::String::Utf8Value(prop->ToString()) << "=" << *v8::String::Utf8Value(val->ToString()) << "\n";
		//}
	}

	Handle<v8::Value> value = global->Get(String::NewFromUtf8(isolate, "Apple"));

	if (value->IsFunction())  {
		std::cout << "Found function!";
	}
	else
		std::cout << "Function not found!";*/

}
