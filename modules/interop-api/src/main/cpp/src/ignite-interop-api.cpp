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

#include <stdlib.h>
#include <stdio.h>
#include <string>
#include <iostream>

#include <assert.h>
#include <jni.h>

#include "ignite-interop-api.h"

namespace ignite {
    using namespace std;

    /* --- JNI METHOD DEFINITIONS. */

    struct JniMethod {
        /** Method name. */
        const char* name;

        /** Method signature. */
        const char* sign;

        /** Static method flag. */
        bool isStatic;

        JniMethod(const char* name, const char* sign, bool isStatic) {
            this->name = name;
            this->sign = sign;
            this->isStatic = isStatic;
        }
    };

    const char* C_THROWABLE = "java/lang/Throwable";
    JniMethod M_THROWABLE_TOSTRING = JniMethod("toString", "()Ljava/lang/String;", false);
    JniMethod M_THROWABLE_GET_MESSAGE = JniMethod("getMessage", "()Ljava/lang/String;", false);

    const char* C_CLASS = "java/lang/Class";
    JniMethod M_CLASS_GET_NAME = JniMethod("getName", "()Ljava/lang/String;", false);

    const char* C_ILLEGAL_ARGUMENT_EXCEPTION = "java/lang/IllegalArgumentException";
    const char* C_ILLEGAL_STATE_EXCEPTION = "java/lang/IllegalStateException";
    const char* C_NULL_POINTER_EXCEPTION = "java/lang/NullPointerException";
    const char* C_UNSUPPORTED_OPERATION_EXCEPTION = "java/lang/UnsupportedOperationException";

    const char* C_IGNITION = "org/apache/ignite/Ignition";
    JniMethod M_IGNITION_IGNITE = JniMethod("ignite", "(Ljava/lang/String;)Lorg/apache/ignite/Ignite;", true);

    const char* C_IGNITION_EX = "org/apache/ignite/internal/IgnitionEx";
    JniMethod M_IGNITION_EX_START_WITH_CLO = JniMethod("startWithClosure", "(Ljava/lang/String;Ljava/lang/String;Lorg/apache/ignite/lang/IgniteClosure;)Lorg/apache/ignite/Ignite;", true);

    const char* C_INTEROP_UTILS = "org/apache/ignite/internal/processors/interop/InteropUtils";
    JniMethod M_INTEROP_UTILS_INTEROP = JniMethod("interop", "(Lorg/apache/ignite/Ignite;)Lorg/apache/ignite/internal/processors/interop/InteropProcessor;", true);
    JniMethod M_INTEROP_UTILS_ASYNC_CALLBACK = JniMethod("asyncCallback", "(JJIJ)V", true);

    const char* C_INTEROP_PROCESSOR = "org/apache/ignite/internal/processors/interop/InteropProcessor";
    JniMethod M_INTEROP_PROCESSOR_CACHE = JniMethod("cache", "(Ljava/lang/String;)Lorg/apache/ignite/internal/processors/interop/InteropTarget;", false);

    const char* C_INTEROP_TARGET = "org/apache/ignite/internal/processors/interop/InteropTargetAdapter";
    JniMethod M_INTEROP_TARGET_IN_OP = JniMethod("inOp", "(IJI)I", false);
    JniMethod M_INTEROP_TARGET_IN_OUT_OP = JniMethod("inOutOp", "(IJI)J", false);
    JniMethod M_INTEROP_TARGET_IN_OUT_OP_ASYNC = JniMethod("inOutOpAsync", "(IJIJJ)V", false);

    const char* C_INTEROP_CACHE = "org/apache/ignite/internal/processors/interop/InteropCache";

    const int OP_CACHE_PUT = 0;

    /**
     *
     */
    class IgniteJvm::Context {
    public:
        JavaVM* jvm;

        jclass c_Throwable;
        jmethodID m_Throwable_toString;
        jmethodID m_Throwable_getMessage;

        jclass c_Class;
        jmethodID m_Class_getName;

        jclass c_Ignition;
        jmethodID m_Ignition_ignite;

        jclass c_IgnitionEx;
        jmethodID m_IgnitionEx_startWithClo;

        jclass c_InteropUtils;
        jmethodID m_InteropUtils_interop;
        jmethodID m_InteropUtils_asyncCallback;

        jclass c_InteropProcessor;
        jmethodID m_InteropProcessor_cache;

        jclass c_InteropTarget;
        jmethodID m_InteropTarget_inOp;
        jmethodID m_InteropTarget_inOutOp;
        jmethodID m_InteropTarget_inOutOpAsync;

        jclass c_InteropCache;
    };

    /**
     *
     */
    class Ignite::Impl {
    public:
        Impl(jobject interopProc) : interopProc(interopProc) {
            // No-op.
        }

        jobject interopProc;
    };

    /**
     * Attach current thread to JVM.
     */
    inline JNIEnv* attach(IgniteJvm::Context* jvmCtx) {
        JNIEnv* env;

        jint res = jvmCtx->jvm->AttachCurrentThread((void**)&env, NULL);

        if (res != JNI_OK)
            return NULL;

        return env;
    }

    /*
    * Clear pending exception and throw it. Callee of this method must first ensure
    * that exception really happened.
    */
    void clearAndThrow(JNIEnv* env, IgniteJvm::Context* jvmCtx) throw(IgniteException) {
        assert (env->ExceptionCheck());

        jthrowable err = env->ExceptionOccurred();

        if (!err)
            throw IgniteException("Exception handler was called, but there is no pending Java exception.");

        env->ExceptionDescribe();

        env->ExceptionClear();

        jstring msg = (jstring)env->CallObjectMethod(err, jvmCtx->m_Throwable_getMessage);

        env->DeleteLocalRef(err);

        if (msg) {
            const char *str= env->GetStringUTFChars(msg, false);

            string msgStr(str);

            env->ReleaseStringUTFChars(msg, str);

            throw IgniteException(msgStr);
        }
        else {
            if (env->ExceptionCheck())
                clearAndThrow(env, jvmCtx);
            else
                throw IgniteException("Unknown ignite exception.");
        }
    }

    inline jobject localToGlobal(JNIEnv* env, jobject localRef, IgniteJvm::Context* jvmCtx) {
        if (localRef) {
            jobject globalRef = env->NewGlobalRef(localRef);

            env->DeleteLocalRef(localRef); // Clear local ref irrespective of result.

            if (!globalRef)
                clearAndThrow(env, jvmCtx);

            return globalRef;
        }
        else
            return NULL;
    }

    /**
     *
     */
    class IgniteInteropTarget {
    public:
        IgniteInteropTarget(IgniteJvm::Context* jvmCtx, jclass cls, jobject obj) : jvmCtx(jvmCtx), cls(cls), obj(obj) {
            // No-op.
        }

        ~IgniteInteropTarget();

        jint inOp(jint type, void* ptr, jint len);

        void* inOutOp(jint type, void* ptr, jint len);

        void inOpAsync(jint type, void* ptr, jint len, IgniteAsyncCallback cb, void* data);

    private:
        /** */
        IgniteJvm::Context* jvmCtx;

        /** Target class for non-virtual invokes. */
        jclass cls;

        /** Target. */
        jobject obj;
    };

    IgniteInteropTarget::~IgniteInteropTarget() {
        JNIEnv* env = attach(jvmCtx);

        env->DeleteGlobalRef(obj);
    }

    jint IgniteInteropTarget::inOp(jint type, void* ptr, jint len) {
        JNIEnv* env = attach(jvmCtx);

        jint res = env->CallNonvirtualIntMethod(this->obj, this->cls, jvmCtx->m_InteropTarget_inOp, type, ptr, len);

        if (env->ExceptionCheck()) {
            clearAndThrow(env, jvmCtx);

            assert(false);

            return 0;
        }

        return res;
    }

    void* IgniteInteropTarget::inOutOp(jint type, void* ptr, jint len) {
        JNIEnv* env = attach(jvmCtx);

        jlong res = env->CallNonvirtualLongMethod(this->obj, this->cls, jvmCtx->m_InteropTarget_inOutOp, type, ptr, len);

        if (env->ExceptionCheck()) {
            clearAndThrow(env, jvmCtx);

            assert(false);

            return NULL;
        }

        return (void*)res;
    }

    void IgniteInteropTarget::inOpAsync(jint type, void* ptr, jint len, IgniteAsyncCallback cb, void* data) {
        JNIEnv* env = attach(jvmCtx);

        env->CallNonvirtualVoidMethod(this->obj, this->cls, jvmCtx->m_InteropTarget_inOutOpAsync, type, ptr, len, cb, data);

        if (env->ExceptionCheck())
            clearAndThrow(env, jvmCtx);
    }

    class IgniteCache::Impl : public IgniteInteropTarget {
    public:
        Impl(IgniteJvm::Context* jvmCtx, jobject obj) : IgniteInteropTarget(jvmCtx, jvmCtx->c_InteropCache, obj) {
            // No-op.
        }
    };

    IgniteJvm::IgniteJvm() {
        ctx = new Context();
    }

    IgniteJvm::~IgniteJvm() {
        delete ctx;
    }

    Ignite* IgniteJvm::startIgnite(char* cfgPath, char* igniteName) throw(IgniteException){
        JNIEnv* env = attach(ctx);

        jstring cfgPath0 = env->NewStringUTF(cfgPath);
        jstring igniteName0 = env->NewStringUTF(igniteName);

        jobject clo = NULL;

        jobject igniteObj = env->CallStaticObjectMethod(ctx->c_Ignition,
            ctx->m_IgnitionEx_startWithClo,
            cfgPath0,
            igniteName0,
            clo);

        env->DeleteLocalRef(cfgPath0);
        env->DeleteLocalRef(igniteName0);
        env->DeleteLocalRef(clo);

        if (env->ExceptionCheck()) {
            assert (!igniteObj);

            clearAndThrow(env, ctx);

            assert (false);
        }
        else if (!igniteObj)
            throw IgniteException("Failed to start Ignite.");

        cout << "Started ignite" << endl;

        jobject interopProc = env->CallStaticObjectMethod(ctx->c_InteropUtils,
            ctx->m_InteropUtils_interop,
            igniteObj);

        env->DeleteLocalRef(igniteObj);

        if (env->ExceptionCheck()) {
            assert (!interopProc);

            clearAndThrow(env, ctx);

            assert (false);
        }
        else if (!interopProc)
            throw IgniteException("Failed to get InteropProcessor.");

        Ignite* ignite0 = new Ignite(this);

        ignite0->impl = new Ignite::Impl(interopProc);

        return ignite0;
    }

    Ignite::Ignite(IgniteJvm* jvm) : jvm(jvm) {
        // No-op.
    }

    Ignite::~Ignite() {
        assert (impl->interopProc);

        JNIEnv* env = attach(jvm->ctx);

        env->DeleteGlobalRef(impl->interopProc);

        delete impl;
    }

    IgniteCache* Ignite::cache(char* cacheName) {
        JNIEnv* env = attach(jvm->ctx);

        jstring name0 = cacheName ? env->NewStringUTF(cacheName) : NULL;

        if (env->ExceptionCheck()) {
            if (name0)
                env->DeleteLocalRef(name0);

            clearAndThrow(env, jvm->ctx);

            assert(false);

            return NULL;
        }

        jobject cache = env->CallObjectMethod(impl->interopProc, jvm->ctx->m_InteropProcessor_cache, name0);

        if (name0)
            env->DeleteLocalRef(name0);

        if (env->ExceptionCheck()) {
            clearAndThrow(env, jvm->ctx);

            assert(false);

            return NULL;
        }
        else if (!cache)
            throw IgniteException("Failed to create cache.");

        cache = localToGlobal(env, cache, jvm->ctx);

        return new IgniteCache(new IgniteCache::Impl(jvm->ctx, cache));
    }

    IgniteCache::IgniteCache(Impl* impl) : impl(impl) {
    }

    void IgniteCache::put(void* ptr, int len) {
        impl->inOp(OP_CACHE_PUT, ptr, len);
    }

    void IgniteCache::putAsync(void* ptr, int len, IgniteAsyncCallback cb, void* data) {
        impl->inOpAsync(OP_CACHE_PUT, ptr, len, cb, data);
    }

    IgniteCache::~IgniteCache() {
        delete impl;
    }

    /**
     * Called from Java to run native callback.
     */
    JNIEXPORT void JNICALL JniAsyncCallback(JNIEnv *env, jclass cls, jlong cb, jlong cbData, jint resType, jlong res) {
        IgniteAsyncCallback cbPtr = (IgniteAsyncCallback)cb;

        cbPtr((void*)cbData, resType, (void*)res);
    }

    /**
     * Find class in running JVM.
     */
    jclass FindClass(JNIEnv* env, const char *name) {
        jclass res = env->FindClass(name);

        if (res) {
            jclass res0 = (jclass)env->NewGlobalRef(res);

            env->DeleteLocalRef(res);

            return res0;
        }
        else
            return NULL;
    }

    /**
     * Find method in running JVM.
     */
    jmethodID FindMethod(JNIEnv* env, jclass cls, JniMethod mthd) {
        jmethodID res = mthd.isStatic ? env->GetStaticMethodID(cls, mthd.name, mthd.sign) :
            env->GetMethodID(cls, mthd.name, mthd.sign);

        return res;
    }

    /**
     * Internal context initialization routine.
     */
    int ContextInit0(JavaVMInitArgs args, IgniteJvm::Context& jvmCtx, JNIEnv** retEnv) {
        JavaVM *jvm = 0;
        JNIEnv* env = 0;

        jint res = JNI_CreateJavaVM(&jvm, (void**)&env, &args);

        if (res != JNI_OK)
            return res;

        jvmCtx.jvm = jvm;

        *retEnv = env;

        // 3. Initialize members.
        jvmCtx.c_Throwable = FindClass(env, C_THROWABLE);

        if (!jvmCtx.c_Throwable)
            return JNI_ERR;

        jvmCtx.m_Throwable_toString = FindMethod(env, jvmCtx.c_Throwable, M_THROWABLE_TOSTRING);

        if (!jvmCtx.m_Throwable_toString)
            return JNI_ERR;

        jvmCtx.m_Throwable_getMessage = FindMethod(env, jvmCtx.c_Throwable, M_THROWABLE_GET_MESSAGE);

        if (!jvmCtx.m_Throwable_getMessage)
            return JNI_ERR;

        jvmCtx.c_Class = FindClass(env, C_CLASS);

        if (!jvmCtx.c_Class)
            return JNI_ERR;

        jvmCtx.m_Class_getName = FindMethod(env, jvmCtx.c_Class, M_CLASS_GET_NAME);

        if (!jvmCtx.m_Class_getName)
            return JNI_ERR;

        jvmCtx.c_Ignition = FindClass(env, C_IGNITION);

        if (!jvmCtx.c_Ignition)
            return JNI_ERR;

        jvmCtx.m_Ignition_ignite = FindMethod(env, jvmCtx.c_Ignition, M_IGNITION_IGNITE);

        if (!jvmCtx.m_Ignition_ignite)
            return JNI_ERR;

        jvmCtx.c_IgnitionEx = FindClass(env, C_IGNITION_EX);

        if (!jvmCtx.c_IgnitionEx)
            return JNI_ERR;

        jvmCtx.m_IgnitionEx_startWithClo = FindMethod(env, jvmCtx.c_IgnitionEx, M_IGNITION_EX_START_WITH_CLO);

        if (!jvmCtx.m_IgnitionEx_startWithClo)
            return JNI_ERR;

        jvmCtx.c_InteropProcessor = FindClass(env, C_INTEROP_PROCESSOR);

        if (!jvmCtx.c_InteropProcessor)
            return JNI_ERR;

        jvmCtx.m_InteropProcessor_cache = FindMethod(env, jvmCtx.c_InteropProcessor, M_INTEROP_PROCESSOR_CACHE);

        if (!jvmCtx.m_InteropProcessor_cache)
            return JNI_ERR;

        jvmCtx.c_InteropUtils = FindClass(env, C_INTEROP_UTILS);

        if (!jvmCtx.c_InteropUtils)
            return JNI_ERR;

        jvmCtx.m_InteropUtils_interop = FindMethod(env, jvmCtx.c_InteropUtils, M_INTEROP_UTILS_INTEROP);

        if (!jvmCtx.m_InteropUtils_interop)
            return JNI_ERR;

        jvmCtx.m_InteropUtils_asyncCallback = FindMethod(env, jvmCtx.c_InteropUtils, M_INTEROP_UTILS_ASYNC_CALLBACK);

        if (!jvmCtx.m_InteropUtils_asyncCallback)
            return JNI_ERR;

        jvmCtx.c_InteropTarget = FindClass(env, C_INTEROP_TARGET);

        if (!jvmCtx.c_InteropTarget)
            return JNI_ERR;

        jvmCtx.m_InteropTarget_inOp = FindMethod(env, jvmCtx.c_InteropTarget, M_INTEROP_TARGET_IN_OP);

        if (!jvmCtx.m_InteropTarget_inOp)
            return JNI_ERR;

        jvmCtx.m_InteropTarget_inOutOp = FindMethod(env, jvmCtx.c_InteropTarget, M_INTEROP_TARGET_IN_OUT_OP);

        if (!jvmCtx.m_InteropTarget_inOutOp)
            return JNI_ERR;

        jvmCtx.m_InteropTarget_inOutOpAsync = FindMethod(env, jvmCtx.c_InteropTarget, M_INTEROP_TARGET_IN_OUT_OP_ASYNC);

        if (!jvmCtx.m_InteropTarget_inOutOpAsync)
            return JNI_ERR;

        jvmCtx.c_InteropCache = FindClass(env, C_INTEROP_CACHE);

        if (!jvmCtx.c_InteropCache)
            return JNI_ERR;


        // 4. Register natives.
        {
            JNINativeMethod methods[1];

            int idx = 0;

            methods[idx].name = (char*)M_INTEROP_UTILS_ASYNC_CALLBACK.name;
            methods[idx].signature = (char*)M_INTEROP_UTILS_ASYNC_CALLBACK.sign;
            methods[idx++].fnPtr = JniAsyncCallback;

            res = env->RegisterNatives(jvmCtx.c_InteropUtils, methods, idx);

            if (res != JNI_OK)
                return res;
        }

        // JNI Env is only necessary for error handling, so we nullify to keep invariant.
        *retEnv = NULL;

        return JNI_OK;
    }

    int ContextInit(JavaVMInitArgs args, IgniteJvm::Context& jvmCtx, char** errClsName, char** errMsg) {
        JNIEnv* env = 0;

        int res = ContextInit0(args, jvmCtx, &env);

        if (res != JNI_OK) {
            if (env) {
                // If env is defined, then JVM has been started. We must get error message and stop it.
                if (env->ExceptionCheck()) {
                    jthrowable err = env->ExceptionOccurred();

                    env->ExceptionClear();

                    jclass errCls = env->GetObjectClass(err);

                    // Try getting error class name.
                    if (jvmCtx.m_Class_getName) {
                        jstring clsName = (jstring)env->CallObjectMethod(errCls, jvmCtx.m_Class_getName);

                        // Could have failed due to OOME.
                        if (clsName) {
                            const char* clsNameChars = env->GetStringUTFChars(clsName, 0);

                            const size_t clsNameCharsLen = strlen(clsNameChars);

                            char* clsNameChars0 = new char[clsNameCharsLen];

                            strcpy(clsNameChars0, clsNameChars);

                            env->ReleaseStringUTFChars(clsName, clsNameChars);

                            *errClsName = clsNameChars0;
                        }

                        // Sanity check for another exception.
                        if (env->ExceptionCheck())
                            env->ExceptionClear();
                    }

                    // Try getting error message.
                    if (jvmCtx.m_Throwable_toString) {
                        jstring msg = (jstring)env->CallObjectMethod(err, jvmCtx.m_Throwable_toString);

                        // Could have failed due to OOME.
                        if (msg) {
                            const char* msgChars = env->GetStringUTFChars(msg, 0);

                            const size_t msgCharsLen = strlen(msgChars);

                            char* msgChars0 = new char[msgCharsLen];

                            strcpy(msgChars0, msgChars);

                            env->ReleaseStringUTFChars(msg, msgChars);

                            // Pass error message backwards.
                            *errMsg = msgChars0;
                        }

                        // Sanity check for another exception.
                        if (env->ExceptionCheck())
                            env->ExceptionClear();
                    }
                }

                // Stop JVM.
                jvmCtx.jvm->DestroyJavaVM();
            }
        }

        return res;
    }

    IGNITE_API_IMPORT_EXPORT IgniteJvm* testIgniteJvmStart() {
        char* igniteHome;

        igniteHome = getenv("IGNITE_HOME");

        if (!igniteHome) {
            cout << "IGNITE_HOME is not set" << endl;

            return NULL;
        }

        string home(igniteHome);

        cout << "IGNITE_HOME:" << home << endl;

        string classpath = "-Djava.class.path=" + home + "\\modules\\core\\target\\classes;";

        classpath += home + "\\modules\\core\\target\\libs\\cache-api-1.0.0.jar;";
        classpath += home + "\\modules\\spring\\target\\classes;";
        classpath += home + "\\modules\\spring\\target\\libs\\spring-core-4.1.0.RELEASE.jar;";
        classpath += home + "\\modules\\spring\\target\\libs\\spring-context-4.1.0.RELEASE.jar;";
        classpath += home + "\\modules\\spring\\target\\libs\\spring-beans-4.1.0.RELEASE.jar;";
        classpath += home + "\\modules\\spring\\target\\libs\\spring-tx-4.1.0.RELEASE.jar;";
        classpath += home + "\\modules\\spring\\target\\libs\\spring-aop-4.1.0.RELEASE.jar;";
        classpath += home + "\\modules\\spring\\target\\libs\\spring-expression-4.1.0.RELEASE.jar;";
        classpath += home + "\\modules\\spring\\target\\libs\\commons-logging-1.1.1.jar;";

        cout << "Classpath:" << classpath << endl;

        JavaVMInitArgs args;

        JavaVMOption* options = new JavaVMOption[2];

        options[0].optionString = const_cast<char*>(classpath.c_str());
        options[1].optionString = "-DIGNITE_QUIET=false";

        args.version = JNI_VERSION_1_6;
        args.nOptions = 2;
        args.options = options;
        args.ignoreUnrecognized = 0;

        char* errClsName;
        char* errMsg;

        IgniteJvm* jvm = new IgniteJvm();

        int res = ContextInit(args, *jvm->ctx, &errClsName, &errMsg);

        delete[] options;

        if (res != JNI_OK) {
            delete jvm;

            cout << "Failed to create JVM: " << errMsg << endl;

            return NULL;
        }

        cout << "Created JVM" << endl;

        return jvm;
    }

}
