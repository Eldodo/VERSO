/*
 * io.h
 *
 *  Created on: Apr 1, 2010
 *      Author: Bruno Dufour (dufour@iro.umontreal.ca)
 */

#ifndef IO_H_
#define IO_H_

#include "common.h"

void eaf_report_failure(const char *message);
void eaf_check_error(jvmtiError err, const char *message);

#endif /* IO_H_ */
