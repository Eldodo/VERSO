#include <sys/time.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "common.h"
#include "io.h"
#include "traceMethods.h"

jvmtiEnv *jvmti_env;

FILE *output;
FILE *errors;
FILE *actions;

char** args;

void DLL_EXPORT JNICALL onJVMStarted(jvmtiEnv *jvmti, JNIEnv* env) {
    fprintf(errors, "TraceMethods> JVM started\n");
}

void DLL_EXPORT JNICALL onJVMInitialized(jvmtiEnv *jvmti, JNIEnv* env, jthread thread) {

    fprintf(errors, "TraceMethods> JVM initialized\n");
}

void DLL_EXPORT JNICALL onJVMExited(jvmtiEnv *jvmti, JNIEnv* env) {
    fprintf(errors, "TraceMethods> JVM exiting\n");
}

void fillArgsArray(char* options){
    int i,j,k,start;
    char* cpy;
    for(i=0,cpy = options;cpy[i];cpy[i]==','? i++:cpy++);

    args = (char**)malloc(sizeof(char*)*i);

    for(j = 0, k = 0, start = 0; options[j]; j++){
        if(options[j]==','){
            args[k] = (char *)malloc(sizeof(char)*(j-start+1));
            strncpy(args[k],options+start,j-start);
            args[k][j-start]='\0';
            start = j+1;
            k++;
        }
    }
}

void getMethodInfo(jvmtiEnv *jvmti_env,
        JNIEnv* jni_env,
        jthread thread,
        jmethodID method,
        char* text){

        char *className = NULL;
    char *methodName = NULL;
    char *methodSig = NULL;
    char subStr[6];
    jclass declaringClass;
    jvmtiError err;

    err = JVMTI_CALL(GetMethodDeclaringClass,method,&declaringClass);
    eaf_check_error(err,"zut");

    if(declaringClass != NULL){
        JVMTI_CALL(GetClassSignature,declaringClass,&className,NULL);
       JVMTI_CALL(GetMethodName,method, &methodName, &methodSig,NULL);
        strncpy(subStr, className,5);
        if(!strcmp(subStr, "Lpobj")){
            fprintf(output, text);
            fprintf(output,className+1);
            fprintf(output,":");
            fprintf(output,methodName);
            fprintf(output,"\n");
        }
    }

    (*jni_env)->DeleteLocalRef(jni_env,declaringClass);
    JVMTI_CALL(Deallocate,(unsigned char*)methodSig);
    JVMTI_CALL(Deallocate,(unsigned char*)className);
    JVMTI_CALL(Deallocate,(unsigned char*)methodName);


}

void DLL_EXPORT JNICALL MethodEntry(jvmtiEnv *jvmti_env,
        JNIEnv* jni_env,
        jthread thread,
        jmethodID method) {

    getMethodInfo(jvmti_env,jni_env,thread,method, "Enter: ");
}

void DLL_EXPORT JNICALL MethodExit(jvmtiEnv *jvmti_env,
            JNIEnv* jni_env,
            jthread thread,
            jmethodID method,
            jboolean was_popped_by_exception,
            jvalue return_value) {
    getMethodInfo(jvmti_env,jni_env,thread,method, "Exit: ");
}

/* -- JVM Hooks -- */

JNIEXPORT jint DLL_EXPORT JNICALL Agent_OnLoad(JavaVM *vm, char *options, void *reserved) {
    jvmtiCapabilities capabilities;
    jvmtiEventCallbacks callbacks;

    if(options!=NULL)
        fillArgsArray(options);

    errors = stderr;


    fprintf(stderr, "Agent initializating\n");

    // Acquire JVMTI environment
    if (JNI_CALL(vm, GetEnv, (void **)&jvmti_env, JVMTI_VERSION) != JNI_OK) {
        eaf_report_failure("Failed to acquire JVMTI environment");
        return JNI_ERR;
    }

    // Register required capabilities
    if (JVMTI_CALL(GetCapabilities, &capabilities) != JVMTI_ERROR_NONE) {
        eaf_report_failure("Failed to obtain JVM capabilities");
        return JNI_ERR;
    }

    capabilities.can_generate_method_entry_events = 1;
    capabilities.can_generate_method_exit_events = 1;
    capabilities.can_get_source_file_name = 1;



    if (JVMTI_CALL(AddCapabilities, &capabilities) != JVMTI_ERROR_NONE) {
        eaf_report_failure("JVM does not support required capabilities");
        return JNI_ERR;
    }

    // Register callbacks
    memset(&callbacks, 0, sizeof(callbacks));
    callbacks.VMStart = &onJVMStarted;
    callbacks.VMInit = &onJVMInitialized;
    callbacks.VMDeath = &onJVMExited;
    callbacks.MethodEntry = &MethodEntry;
    callbacks.MethodExit = &MethodExit;

    if (JVMTI_CALL(SetEventCallbacks, &callbacks, sizeof(callbacks)) != JVMTI_ERROR_NONE) {
        eaf_report_failure("Failed to register agent callbacks");
        return JNI_ERR;
    }

    // Enable events
    if (JVMTI_ENABLE_EVENT(JVMTI_EVENT_VM_START) != JVMTI_ERROR_NONE) {
        eaf_report_failure("Failed to enable JVM start event");
        return JNI_ERR;
    }
    if (JVMTI_ENABLE_EVENT(JVMTI_EVENT_VM_INIT) != JVMTI_ERROR_NONE) {
        eaf_report_failure("Failed to enable JVM initialization event");
        return JNI_ERR;
    }
    if (JVMTI_ENABLE_EVENT(JVMTI_EVENT_VM_DEATH) != JVMTI_ERROR_NONE) {
        eaf_report_failure("Failed to enable JVM exit event");
        return JNI_ERR;
    }
    if (JVMTI_ENABLE_EVENT(JVMTI_EVENT_CLASS_FILE_LOAD_HOOK) != JVMTI_ERROR_NONE) {
        eaf_report_failure("Failed to enable JVM class loading event");
        return JNI_ERR;
    }

    if (JVMTI_ENABLE_EVENT(JVMTI_EVENT_METHOD_ENTRY) != JVMTI_ERROR_NONE) {
        eaf_report_failure("Failed to enable JVM method entry event");
        return JNI_ERR;
    }

    if (JVMTI_ENABLE_EVENT(JVMTI_EVENT_METHOD_EXIT) != JVMTI_ERROR_NONE) {
        eaf_report_failure("Failed to enable JVM method exit event");
        return JNI_ERR;
    }



//    output = stderr;
    errors  = fopen("errors.log","w");
    actions  = fopen("actions.log","w");
    if (errors == NULL || actions == NULL) {
               return JNI_ERR;
     }
    output = fopen("startup_trace.txt", "w");
    if (output == NULL) {
           return JNI_ERR;
       }
    return JNI_OK;
}


JNIEXPORT void DLL_EXPORT JNICALL Agent_OnUnload(JavaVM *vm) {
    fprintf(errors, "TraceMethods> Agent terminating\n");
    fflush(output);
    fflush(errors);
    fflush(actions);
    fclose(errors);
    fclose(actions);
    if (output != stderr && output != stdout) {
        fclose(output);
    }
}

DLL_EXPORT BOOL APIENTRY DllMain(HINSTANCE hinstDLL, DWORD fdwReason, LPVOID lpvReserved)
{
    switch (fdwReason)
    {
        case DLL_PROCESS_ATTACH:
            // attach to process
            // return FALSE to fail DLL load
            break;

        case DLL_PROCESS_DETACH:
            // detach from process
            break;

        case DLL_THREAD_ATTACH:
            // attach to thread
            break;

        case DLL_THREAD_DETACH:
            // detach from thread
            break;
    }
    return TRUE; // succesful
}

