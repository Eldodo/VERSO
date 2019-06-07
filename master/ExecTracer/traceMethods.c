#include <sys/time.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "common.h"
#include "io.h"

jvmtiEnv *jvmti_env;

FILE *output;
FILE *errors;
FILE *actions;

int sorties = 1;
int entrees = 1;



jvmtiError getMethodInfo(jmethodID method, jint *access, char **className, char **methodName,
        char **methodSig, jthread thread, jvmtiThreadInfo *info_th) {
    jvmtiError err;
    jclass klass;

    err = JVMTI_CALL(GetMethodDeclaringClass, method, &klass);
    if (err == JVMTI_ERROR_NONE) {
    	err = JVMTI_CALL(GetThreadInfo,thread, info_th);
    	if (err == JVMTI_ERROR_NONE) {

			err = JVMTI_CALL(GetClassSignature, klass, className, NULL);
			if (err == JVMTI_ERROR_NONE) {
				if (access != NULL) {
					err = JVMTI_CALL(GetMethodModifiers, method, access);
					if (err != JVMTI_ERROR_NONE) {
						// Cleanup
						JVMTI_DEALLOC(*className);
						JVMTI_DEALLOC(info_th->name);
					}
				}
				if (err == JVMTI_ERROR_NONE) {
					err = JVMTI_CALL(GetMethodName, method, methodName, methodSig, NULL);
					if (err != JVMTI_ERROR_NONE) {
						// Cleanup
						JVMTI_DEALLOC(*className);
						JVMTI_DEALLOC(info_th->name);
					}
				}
			}
			else{
				JVMTI_DEALLOC(info_th->name);
			}
    	}
    }

    eaf_check_error(err, "Failed to obtain method info");

    return err;
}



void JNICALL onJVMStarted(jvmtiEnv *jvmti, JNIEnv* env) {
    fprintf(errors, "TraceMethods> JVM started\n");
}

void JNICALL onJVMInitialized(jvmtiEnv *jvmti, JNIEnv* env, jthread thread) {
    fprintf(errors, "TraceMethods> JVM initialized\n");
}

void JNICALL onJVMExited(jvmtiEnv *jvmti, JNIEnv* env) {
    fprintf(errors, "TraceMethods> JVM exiting\n");
}

#define STARTS_WITH(s,prefix) (strncmp(s,prefix, strlen(prefix)) == 0)

void JNICALL onMethodEntry(jvmtiEnv *jvmti_env,
        JNIEnv* jni_env,
        jthread thread,
        jmethodID method) {
    jint access;
    char *className = NULL;
    char *methodName = NULL;
    char *methodSig = NULL;
    jvmtiThreadInfo info_th;
    jvmtiError err;

    
    err = getMethodInfo(method, &access, &className, &methodName, &methodSig, thread ,&info_th);
//    err = getMethodInfo(method, &access, &className, &methodName, &methodSig);

    if (err == JVMTI_ERROR_NONE) {

       	int i,j;
    	i = strcspn (className,"/");
    	char str[i+1];
    	for(j = 0; j <= i ; j++) str[j]=className[j];
    	str[i]='\0';
    	className[strlen(className)-1]='\0';

    	if((strcmp(str,"Ljava")==0)||(strcmp(str,"Ljavax")==0)||(strcmp(str,"Lcom")==0)||(strcmp(str,"Lsun")==0) ||(STARTS_WITH(className, "Lorg/eclipse")));
    	else {

    	    entrees = 1;
    		if(sorties == 1)
    		{
    			sorties = 0;
    			fprintf(output,"\n E:");
    		}
    		struct timeval tv;
    		gettimeofday(&tv, NULL);
    		fprintf(output, "%s %s.%s (%ld.%ld) | ", info_th.name, className+1,methodName,tv.tv_sec,tv.tv_usec);
    	}
//    	if((strcmp(str,"Ljava")==0)||(strcmp(str,"Ljavax")==0)||(strcmp(str,"Lcom")==0)||(strcmp(str,"Lsun")==0)||(strcmp(str,"Lorg")==0));
//        else {
//        	struct timeval tv;
//        	gettimeofday(&tv, NULL);
//        	fprintf(output, "%s %s.%s (%ld.%ld) | ", info_th.name, className+1,methodName,tv.tv_sec,tv.tv_usec);
////        	fprintf(stdout, "%s.%s (%ld.%ld) | ", className,methodName,tv.tv_sec,tv.tv_usec);
//        }
    } else {
        fprintf(output, "(n/a) | ");
    }
//    fflush(output);
    JVMTI_DEALLOC(className);
    JVMTI_DEALLOC(methodName);
    JVMTI_DEALLOC(methodSig);
    JVMTI_DEALLOC(info_th.name);


}

void JNICALL onMethodExit(jvmtiEnv *jvmti_env,
            JNIEnv* jni_env,
            jthread thread,
            jmethodID method,
            jboolean was_popped_by_exception,
            jvalue return_value) {
	struct timeval tv;
	gettimeofday(&tv, NULL);
    jint access;
    char *className;
    char *methodName;
    char *methodSig;
    jvmtiError err;

    jvmtiThreadInfo info_th;



        err = getMethodInfo(method, &access, &className, &methodName, &methodSig, thread , &info_th);
//        err = getMethodInfo(method, &access, &className, &methodName, &methodSig);

        if (err == JVMTI_ERROR_NONE) {

        	int i,j;
        	i = strcspn (className,"/");
        	char str[i+1];
        	for(j = 0; j <= i ; j++) str[j]=className[j];
        	str[i]='\0';
        	className[strlen(className)-1]='\0';

        	if((strcmp(str,"Ljava")==0)||(strcmp(str,"Ljavax")==0)||(strcmp(str,"Lcom")==0) ||(strcmp(str,"Lsun")==0) ||(strcmp(str,"Lorg")==0));
        	else
    		{
        		 sorties = 1;
        		 if(entrees == 1){
        			 entrees = 0;
        			 fprintf(output,"\n S:");
        		 }
             	fprintf(output, "%s %s.%s (%ld.%ld) | ", info_th.name, className+1,methodName,tv.tv_sec,tv.tv_usec);

    		}

//        	if((strcmp(str,"Ljava")==0)||(strcmp(str,"Ljavax")==0)||(strcmp(str,"Lcom")==0)||(strcmp(str,"Lsun")==0) ||(strcmp(str,"Lorg")==0));
//            else {
////            	fprintf(stdout, "%s.%s (%ld.%ld) | ", className,methodName,tv.tv_sec,tv.tv_usec);
//            	fprintf(output, "%s %s.%s (%ld.%ld) | ", info_th.name, className+1,methodName,tv.tv_sec,tv.tv_usec);
//            }


        } else {
            fprintf(output, "(n/a) | ");
        }

//        fflush(output);
        JVMTI_DEALLOC(className);
        JVMTI_DEALLOC(methodName);
        JVMTI_DEALLOC(methodSig);
        JVMTI_DEALLOC(info_th.name);

}

/* -- JVM Hooks -- */

JNIEXPORT jint JNICALL Agent_OnLoad(JavaVM *vm, char *options, void *reserved) {
    jvmtiCapabilities capabilities;
    jvmtiEventCallbacks callbacks;


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



    if (JVMTI_CALL(AddCapabilities, &capabilities) != JVMTI_ERROR_NONE) {
        eaf_report_failure("JVM does not support required capabilities");
        return JNI_ERR;
    }

    // Register callbacks
    memset(&callbacks, 0, sizeof(callbacks));
    callbacks.VMStart = &onJVMStarted;
    callbacks.VMInit = &onJVMInitialized;
    callbacks.VMDeath = &onJVMExited;
    callbacks.MethodEntry = &onMethodEntry;
    callbacks.MethodExit = &onMethodExit;

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


/*
 * Class:     net_suberic_pooka_Separator
 * Method:    newFile
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_net_suberic_pooka_Separator_newFile
  (JNIEnv* env, jobject jobj, jstring jcomment){
	const char *comment = (*env)->GetStringUTFChars(env, jcomment, 0);

		  char str[80];

		  strcpy (str,comment);
		  strcat (str,"_traces.txt");

		  fprintf(actions,"\n");
		  fprintf(actions,comment);
		  fprintf(actions,":\n");

			fflush(output);
			if (output != stderr && output != stdout) {
			    fclose(output);
			}



		output = fopen(str, "w");
		 if (output == NULL) {
			 fprintf(errors,"Failed to open file");
		 }


		(*env)->ReleaseStringUTFChars(env, jcomment, comment);
}

/*
 * Class:     net_suberic_pooka_Separator
 * Method:    start
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_net_suberic_pooka_Separator_start
  (JNIEnv* env, jobject jobj){

		if (JVMTI_ENABLE_EVENT(JVMTI_EVENT_METHOD_ENTRY) != JVMTI_ERROR_NONE) {
			 fprintf(errors,"Failed to enable JVM method entry event");
	    }

	    if (JVMTI_ENABLE_EVENT(JVMTI_EVENT_METHOD_EXIT) != JVMTI_ERROR_NONE) {
	    	 fprintf(errors,"Failed to enable JVM method exit event");
	    }
}

/*
 * Class:     net_suberic_pooka_Separator
 * Method:    stop
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_net_suberic_pooka_Separator_stop
(JNIEnv* env, jobject jobj){

			if (JVMTI_DISABLE_EVENT(JVMTI_EVENT_METHOD_ENTRY) != JVMTI_ERROR_NONE) {
				 fprintf(errors,"Failed to disable JVM method entry event");
		     }

		    if (JVMTI_DISABLE_EVENT(JVMTI_EVENT_METHOD_EXIT) != JVMTI_ERROR_NONE) {
		    	 fprintf(errors,"Failed to disable JVM method exit event");
		    }
}

/*
 * Class:     net_suberic_pooka_Separator
 * Method:    separate
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_net_suberic_pooka_Separator_separate
	(JNIEnv* env, jobject jobj, jstring jcomment){

		const char *comment = (*env)->GetStringUTFChars(env, jcomment, 0);

		fprintf(actions,"\t-");
		fprintf(actions,comment);
		fprintf(actions,"\n");
		fflush(actions);

		fprintf(output, "\n--->\t%s", comment);
		fflush(output);
	  	(*env)->ReleaseStringUTFChars(env, jcomment, comment);

}

JNIEXPORT void JNICALL Agent_OnUnload(JavaVM *vm) {
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
