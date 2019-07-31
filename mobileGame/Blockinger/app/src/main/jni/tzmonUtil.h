#ifndef BLOCKINGER_TZMONUTIL_H
#define BLOCKINGER_TZMONUTIL_H

#include <jni.h>
#include <android/log.h>    // for android log function

#define DEBUG_ENABLE   (1)

#if (DEBUG_ENABLE)
#define LOGV(...) __android_log_print(ANDROID_LOG_VERBOSE, "[LOGV]", __VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG  , "[LOGD]", __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO   , "[LOGI]", __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN   , "[LOGW]", __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR  , "[LOGE]", __VA_ARGS__)
#else
#define LOGV(...)
#define LOGD(...)
#define LOGI(...)
#define LOGW(...)
#define LOGE(...)
#endif

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
