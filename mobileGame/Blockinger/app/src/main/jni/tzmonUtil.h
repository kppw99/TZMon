#ifndef BLOCKINGER_TZMONUTIL_H
#define BLOCKINGER_TZMONUTIL_H

#include <jni.h>

void tzmon_itoa(unsigned char *src, int srcLen, char *target, int *targetLen);
void tzmon_atoi(char *src, int srcLen, unsigned char *dest, int *destLen);
void printBuf(char *title, unsigned char *data, int dataLen);

void getCurrentTime(JNIEnv *env, double *aTimeGap);
void getAppName(JNIEnv *env, jobject context, char *appName);
void getAppPath(JNIEnv *env, jobject context, char *appPath);
void getTopProcess(JNIEnv *env, jobject context, char *topName);
void getRunningApp(JNIEnv *env, jobject context);

bool isRunningApp(JNIEnv *env, jobject context, const char *appName);

#endif //BLOCKINGER_TZMONUTIL_H
