################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
C_SRCS += \
../io.c \
../traceMethods.c 

OBJS += \
./io.o \
./traceMethods.o 

C_DEPS += \
./io.d \
./traceMethods.d 


# Each subdirectory must supply rules for building sources it contributes
%.o: ../%.c
	@echo 'Building file: $<'
	@echo 'Invoking: GCC C Compiler'
	gcc -fPIC -IC:\Program Files\Java\jdk1.8.0_161\include -IC:\Program Files\Java\jdk1.8.0_161\include\win32 -O0 -g3 -Wall -c -fmessage-length=0 -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@)" -o "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '


