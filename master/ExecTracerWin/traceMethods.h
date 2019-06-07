#ifndef __TRACEMETHODS_H__
#define __TRACEMETHODS_H__

#include <windows.h>
#include <string.h>

/*  To use this exported function of dll, include this header
 *  in your project.
 */

#ifdef BUILD_DLL
    #define DLL_EXPORT __declspec(dllexport)
#else
    #define DLL_EXPORT __declspec(dllimport)
#endif


#ifdef __cplusplus
extern "C"
{
#endif

void DLL_EXPORT JNICALL onJVMStarted(jvmtiEnv *jvmti, JNIEnv* env);
void DLL_EXPORT JNICALL onJVMInitialized(jvmtiEnv *jvmti, JNIEnv* env, jthread thread);
void DLL_EXPORT JNICALL onJVMExited(jvmtiEnv *jvmti, JNIEnv* env);
void DLL_EXPORT JNICALL MethodEntry(jvmtiEnv *jvmti_env,JNIEnv* jni_env,jthread thread,jmethodID method);
void DLL_EXPORT JNICALL MethodExit(jvmtiEnv *jvmti_env,JNIEnv* jni_env,jthread thread,jmethodID method,jboolean was_popped_by_exception,jvalue return_value);
JNIEXPORT jint DLL_EXPORT JNICALL Agent_OnLoad(JavaVM *vm, char *options, void *reserved);

#ifdef __cplusplus
}
#endif

#endif // __TRACEMETHODS_H__

