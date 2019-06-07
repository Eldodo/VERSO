/*
 * elude_io.c
 *
 *  Created on: Apr 1, 2010
 *      Author: Bruno Dufour (dufour@iro.umontreal.ca)
 */

#include <stdio.h>
#include "io.h"

void eaf_report_failure(const char *message) {
    fprintf(stderr, "ERROR: %s\n", message);
}

void eaf_check_error(jvmtiError err, const char *message) {
    if (err != JVMTI_ERROR_NONE) {
        eaf_report_failure(message);
        fprintf(stderr, "  Error code: %d\n", err);
    }
}
