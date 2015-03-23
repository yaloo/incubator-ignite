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

#ifndef IGNITE_INTEROP_API
#define IGNITE_INTEROP_API

#if defined(__CYGWIN__) || defined(_WIN32)
# ifdef IGNITE_API_EXPORT
#    define IGNITE_API_IMPORT_EXPORT __declspec(dllexport)
# else
#    define IGNITE_API_IMPORT_EXPORT __declspec(dllimport)
# endif
#else
# define IGNITE_API_IMPORT_EXPORT
#endif

#include <stdint.h>
#include <string>

namespace ignite {
    /**
    * Callback to handle Java errors.
    */
    typedef void(*IgniteErrorCallback)(const std::string& errMsg, const std::string& errCls);


    /**
    * Callback for asynchronous operations.
    */
    typedef void(*IgniteAsyncCallback)(void* data, int resType, void* res);

	class IgniteInputStream {
	public:
		IgniteInputStream(uint64_t dataPtr, int32_t size, int32_t allocSize) : 
			dataPtr(dataPtr), size(size), allocSize(allocSize), readPos(0) {
			assert(dataPtr);
			assert(size > 0);
			assert(allocSize > 0);
		}

		IgniteInputStream(void* dataPtr, int32_t size, int32_t allocSize) : 
			dataPtr(reinterpret_cast<uint64_t>(dataPtr)), size(size), allocSize(allocSize), readPos(0) {
			assert(dataPtr);
			assert(size > 0);
			assert(allocSize > 0);
		}

		int8_t readByte() {
			checkAvailable(1);

			int8_t res = *reinterpret_cast<int8_t*>(dataPtr + readPos);

			readPos++;

			return res;
		}

		int32_t readInt() {
			checkAvailable(4);

			int32_t res = *reinterpret_cast<int32_t*>(dataPtr + readPos);

			readPos += 4;

			return res;
		}

		int64_t readLong() {
			checkAvailable(8);

			int64_t res = *reinterpret_cast<int64_t*>(dataPtr + readPos);

			readPos += 8;

			return res;
		}

	private:
		void checkAvailable(int32_t cnt) {
			assert(readPos + cnt <= size);
		}

		uint64_t dataPtr;

		int32_t size;

		int32_t allocSize;

		int32_t readPos;
	};

	class IgniteOutputStream {
	public:
		/**
		* @param initialSize Initially allocated size.
		*/
		IgniteOutputStream(int initialSize) : allocSize(initialSize), pos(0) {
			assert(initialSize > 0);

			dataPtr = reinterpret_cast<uint64_t>(malloc(initialSize));
		}

		~IgniteOutputStream() {
			free(reinterpret_cast<void*>(dataPtr));
		}

		/**
		* @param val Value to write.
		*/
		void writeBool(bool val) {
			writeByte(val ? 1 : 0);
		}

		/**
		* @param val Value to write.
		*/
		void writeByte(int8_t val) {
			resizeIfNeeded(1);

			*reinterpret_cast<int8_t*>(dataPtr + pos) = val;

			pos++;
		}

		void writeInt(int32_t val) {
			resizeIfNeeded(4);

			*reinterpret_cast<int32_t*>(dataPtr + pos) = val;

			pos += 4;
		}

		void writeLong(int64_t val) {
			resizeIfNeeded(8);

			*reinterpret_cast<int64_t*>(dataPtr + pos) = val;

			pos += 8;
		}

		uint64_t dataPointer() {
			return dataPtr;
		}

		int32_t position() {
			return pos;
		}

		int32_t allocatedSize() {
			return allocSize;
		}

	private:
		/**
		* @param cnt Resize byte count.
		*/
		void resizeIfNeeded(int32_t cnt) {
			int32_t newSize = pos + cnt;

			if (newSize > allocSize) {
				allocSize *= 2;

				if (newSize > allocSize)
					allocSize = newSize;

				dataPtr = reinterpret_cast<uint64_t>(realloc(reinterpret_cast<void*>(dataPtr), allocSize));
			}
		}

		uint64_t dataPtr;

		int32_t pos;

		int32_t allocSize;
	};

	class IgniteJvm;
    class IgniteCache;

    class IGNITE_API_IMPORT_EXPORT Ignite {
    public:
        ~Ignite();

        IgniteCache* cache(char* cacheName);

    private:
        Ignite(IgniteJvm* jvm);

        class Impl;

        Impl* impl;

        IgniteJvm* jvm;

        friend class IgniteJvm;
    };

    class IGNITE_API_IMPORT_EXPORT IgniteCache {
    public:
        void put(void* ptr, int len);

        void putAsync(void* ptr, int len, IgniteAsyncCallback cb, void* data);

        ~IgniteCache();
    private:
        class Impl;

        Impl* impl;

        IgniteCache(Impl* impl);

        friend class Ignite;
    };

    /**
     *
     */
    class IGNITE_API_IMPORT_EXPORT IgniteJvm {
    public:
        ~IgniteJvm();

        Ignite* startIgnite(char* cfgPath, char* igniteName);

        class Context;
    private:
        IgniteJvm(IgniteErrorCallback errCb);

        Context* ctx;

        friend IGNITE_API_IMPORT_EXPORT IgniteJvm* testIgniteJvmStart(IgniteErrorCallback errCb);

        friend class Ignite;

        friend class IgniteCache;
    };

    /**
    * Initialization function for test.
    */
    IGNITE_API_IMPORT_EXPORT extern IgniteJvm* testIgniteJvmStart(IgniteErrorCallback errCb);
}

#endif
