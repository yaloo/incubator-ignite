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

#include <iostream>
#include <stdlib.h>
#include <string>

using namespace v8;

using namespace ignite;

Persistent<Function> IgniteNodeJsCache::constructor;

IgniteNodeJsCache::IgniteNodeJsCache(IgniteCache* cache) : cache(cache) {
	// No-op
}

IgniteNodeJsCache::~IgniteNodeJsCache() {
	if (cache)
		delete cache;
}

Ignite* igniteNode;

void IgniteNodeJsCache::Init(Handle<Object> exports) {
	/*
	igniteNode = StartNode();

	if (!igniteNode)
		return;

	Isolate* isolate = Isolate::GetCurrent();

	// Prepare constructor template
	Local<FunctionTemplate> tpl = FunctionTemplate::New(isolate, New);
	tpl->SetClassName(String::NewFromUtf8(isolate, "IgniteCache"));
	tpl->InstanceTemplate()->SetInternalFieldCount(1);

	// Prototype
	NODE_SET_PROTOTYPE_METHOD(tpl, "put", Put);
	NODE_SET_PROTOTYPE_METHOD(tpl, "putAsync", PutAsync);
	NODE_SET_PROTOTYPE_METHOD(tpl, "objectInfo", ObjectInfo);

	constructor.Reset(isolate, tpl->GetFunction());
	
	exports->Set(String::NewFromUtf8(isolate, "IgniteCache"), tpl->GetFunction());
	*/
}

void IgniteNodeJsCache::New(const FunctionCallbackInfo<Value>& args) {
	/*
	Isolate* isolate = Isolate::GetCurrent();
	HandleScope scope(isolate);

	if (args.IsConstructCall()) {
		// Invoked as constructor: `new IgniteCache(...)`
		
		IgniteInteropCache* cache = 0;
		
		if (!args[0]->IsUndefined()) {
			Local<String> name = args[0]->ToString();

			v8::String::Utf8Value utf8(name);
			
			std::cout << "Cache: " << *utf8 << "\n";
			
			cache = igniteNode->getCache(*utf8);
		}
		else
			cache = igniteNode->getCache(0);

		if (!cache)
			return;

		IgniteCache* obj = new IgniteCache(cache);

		obj->Wrap(args.This());

		args.GetReturnValue().Set(args.This());
	}
	else {
		// Invoked as plain function `IgniteCache(...)`, turn into construct call.
		const int argc = 1;

		Local<Value> argv[argc] = { args[0] };
		
		Local<Function> cons = Local<Function>::New(isolate, constructor);
		
		args.GetReturnValue().Set(cons->NewInstance(argc, argv));
	}
	*/
}

void IgniteNodeJsCache::Put(const FunctionCallbackInfo<Value>& args) {
	/*
	Isolate* isolate = Isolate::GetCurrent();
	HandleScope scope(isolate);

	IgniteCache* cache = ObjectWrap::Unwrap<IgniteCache>(args.Holder());

	if (args.Length() != 2) {
		std::cout << "Two arguments expected.\n";

		return;
	}

	int key = args[0]->Int32Value();
	int val = args[1]->Int32Value();

	std::cout << "Put key=" << key << ", val=" << val << "\n";

	InterupOutputStream out(8);

	out.writeInt32(key);
	out.writeInt32(val);

	cache->cache->put(out.data(), out.size());
	//obj->value_ += 1;

	//args.GetReturnValue().Set(Number::New(isolate, obj->value_));
	*/
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
