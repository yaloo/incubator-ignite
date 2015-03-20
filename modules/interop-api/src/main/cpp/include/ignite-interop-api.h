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

#if defined(__CYGWIN__) || defined(_WINDLL)
# ifdef IGNITE_API_EXPORT
#    define IGNITE_API_IMPORT_EXPORT __declspec(dllexport)
# else
#    define IGNITE_API_IMPORT_EXPORT __declspec(dllimport)
# endif
#else
# define IGNITE_API_IMPORT_EXPORT
#endif

#include <string>

namespace ignite {
    /**
     * Ignite exception.
     */
    class IGNITE_API_IMPORT_EXPORT IgniteException: public std::exception {
    public:
        /**
         * Constructor of exception with message text.
         *
         * @param what Exception text.
         */
        IgniteException(const std::string& what) : what_(what) {
            // No-op.
        }

        virtual const char* what() const throw() {
            return what_.c_str();
        }

    private:
        std::string what_;
    };

    /**
    * Callback for asynchronous operations.
    */
    typedef void(*IgniteAsyncCallback)(void* data, int resType, void* res);

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

        Ignite* startIgnite(char* cfgPath, char* igniteName) throw(IgniteException);

        class Context;
    private:
        IgniteJvm();

        Context* ctx;

        friend IGNITE_API_IMPORT_EXPORT IgniteJvm* testIgniteJvmStart();

        friend class Ignite;

        friend class IgniteCache;
    };

    /**
    * Initialization function for test.
    */
    IGNITE_API_IMPORT_EXPORT extern IgniteJvm* testIgniteJvmStart();
}

#endif
