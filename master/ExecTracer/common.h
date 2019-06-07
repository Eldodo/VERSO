/*
 * common.h
 *
 *  Created on: Apr 1, 2010
 *      Author: Bruno Dufour (dufour@iro.umontreal.ca)
 */

#ifndef COMMON_H_
#define COMMON_H_

#include <jvmti.h>

typedef unsigned char byte;
typedef enum {false, true} bool;

extern jvmtiEnv *jvmti_env;

#define EAM_CALL(e, f, ...)   (*e)->f(e, ##__VA_ARGS__)
#define JVMTI_CALL(f, ...)    EAM_CALL(jvmti_env, f, ##__VA_ARGS__)
#define JNI_CALL(e, f, ...)   EAM_CALL(e, f, ##__VA_ARGS__)
#define JVMTI_ENABLE_EVENT(e) JVMTI_CALL(SetEventNotificationMode, JVMTI_ENABLE, e, NULL)
#define JVMTI_DISABLE_EVENT(e) JVMTI_CALL(SetEventNotificationMode, JVMTI_DISABLE, e, NULL)
#define JVMTI_DEALLOC(p)      (p != NULL ? JVMTI_CALL(Deallocate, (unsigned char *) p) : JVMTI_ERROR_NONE); p = NULL
#define LOCK(lock)            JVMTI_CALL(RawMonitorEnter, lock)
#define UNLOCK(lock)          JVMTI_CALL(RawMonitorExit, lock)

#endif /* ELUDE_COMMON_H_ */
