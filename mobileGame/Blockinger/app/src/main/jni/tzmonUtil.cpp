#include <android/log.h>    // for android log function
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <time.h>           // for clock function
#include <sys/time.h>       // for time function
#include <unistd.h>         // for sleep function
#include "openssl/sha.h"    // for SHA function of openSSL

#include "tzmonUtil.h"

#define LOGV(...) __android_log_print(ANDROID_LOG_VERBOSE, "[LOGV]", __VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG  , "[LOGD]", __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO   , "[LOGI]", __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN   , "[LOGW]", __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR  , "[LOGE]", __VA_ARGS__)

void tzmon_itoa(unsigned char *src, int srcLen, char *target, int *targetLen)
{
    for (int i = 0; i < srcLen; i++) {
        sprintf(&target[i * 2], "%02x", (unsigned int)src[i]);
    }

    *targetLen = srcLen * 2;
    target[*targetLen] = 0x00;
}

void tzmon_atoi(char *src, int srcLen, unsigned char *dest, int *destLen)
{
    int hex;
    char tmp[3] = { 0x00, };

    if (src == NULL || dest == NULL || destLen == NULL) return;

    *destLen = srcLen / 2;
    for (int i = 0; i < *destLen; i++) {
        memset(tmp, 0x00, sizeof(tmp));
        memcpy(tmp, src + i * 2, 2);

        if (tmp[0] >= '0' && tmp[0] <= '9') {
            if (tmp[1] >= '0' && tmp[1] <= '9') {
                hex = atoi(tmp);
                dest[i] = ((hex / 10) * 16) + (hex % 10);
            } else {
                dest[i] = atoi(&tmp[0]) * 16;
                dest[i] += tmp[1] - 87;
            }
        } else {
            dest[i] = (tmp[0] - 87) * 16;
            if (tmp[1] >= '0' && tmp[1] <= '9') {
                dest[i] += atoi(&tmp[1]);
            } else {
                dest[i] += tmp[1] - 87;
            }
        }
    }
}

void printBuf(char *title, unsigned char *data, int dataLen)
{
    char buffer[1024] = { 0x00, };

    if (dataLen == 0 || data == NULL) return;

    LOGD("%s", title);
    for (int i = 0; i < dataLen; i++) {
        sprintf(&buffer[i * 5], "0x%02x ", (unsigned int)data[i]);
    }

    LOGD("%s", buffer);
}

void getCurrentTime(JNIEnv *env, double *aTimeGap)
{
    struct timeval startVal, endVal;
    struct timespec startSpec, endSpec;
    jlong startJ, endJ;

    double millisec = 1000.0;
    double microsec = 1000000.0;
    double nanosec = 1000000000.0;

    int delay = 125000; // 125000 usec --> 0.125 sec

    // ================== JAVA Original Source Code ================================================
    // curTime = System.currentTimeMillis();
    // =============================================================================================
    jclass system = env->FindClass("java/lang/System");
    jmethodID curTimeID = env->GetStaticMethodID(system, "currentTimeMillis", "()J");

    gettimeofday(&startVal, NULL);
    clock_gettime(CLOCK_MONOTONIC, &startSpec);
    startJ = env->CallStaticLongMethod(system, curTimeID);

    usleep(delay);

    gettimeofday(&endVal, NULL);
    clock_gettime(CLOCK_MONOTONIC, &endSpec);
    endJ = env->CallStaticLongMethod(system, curTimeID);

    aTimeGap[0] = (endVal.tv_sec - startVal.tv_sec) +
                    (( endVal.tv_usec - startVal.tv_usec ) / microsec);
    aTimeGap[1] = (endSpec.tv_sec - startSpec.tv_sec) +
                    (( endSpec.tv_nsec - startSpec.tv_nsec ) / nanosec);
    aTimeGap[2] = ((double)(endJ) - (double)(startJ)) / millisec;
}

// original app name과 비교하여 appName을 manipulation하는 공격 차단!!!
void getAppName(JNIEnv *env, jobject context, char *appName)
{
    // ================== JAVA Original Source Code ================================================
    // appName = this.getPackageName();
    // =============================================================================================

    const char *name = NULL;

    jclass cls = env->GetObjectClass(context);
    jmethodID getPackageName = env->GetMethodID(cls, "getPackageName", "()Ljava/lang/String;");
    jstring packageName = (jstring) env->CallObjectMethod(context, getPackageName);

    name = env->GetStringUTFChars(packageName, 0x00);
    memcpy(appName, name, strlen(name));

    env->ReleaseStringUTFChars(packageName, name);
    env->DeleteLocalRef(cls);
}

void getAppPath(JNIEnv *env, jobject context, char *appPath)
{
    // ================== JAVA Original Source Code ================================================
    // packageInfo = this.getPackageManager().getPackageInfo(this.getPackageName(),
    //                                                     PackageManager.GET_META_DATA /* 0x80 */);
    // appPath = packageInfo.applicationInfo.sourceDir;
    // =============================================================================================

    const char *path = NULL;

    jclass cls = env->GetObjectClass(context);
    jmethodID getPackageName = env->GetMethodID(cls, "getPackageName", "()Ljava/lang/String;");
    jmethodID getPackageManager = env->GetMethodID(cls, "getPackageManager",
                                    "()Landroid/content/pm/PackageManager;");

    jclass packageManagerCls = env->FindClass("android/content/pm/PackageManager");
    jmethodID getPackageInfo = env->GetMethodID(packageManagerCls, "getPackageInfo",
                                    "(Ljava/lang/String;I)Landroid/content/pm/PackageInfo;");

    jobject packageManager = env->CallObjectMethod(context, getPackageManager);
    jstring packageName = (jstring) env->CallObjectMethod(context, getPackageName);
    jobject packageInfo = env->CallObjectMethod(packageManager, getPackageInfo, packageName, 0x80);

    jclass packageInfoCls = env->FindClass("android/content/pm/PackageInfo");
    jfieldID applicationInfoID = env->GetFieldID(packageInfoCls, "applicationInfo",
                                    "Landroid/content/pm/ApplicationInfo;");
    jobject applicationInfo = env->GetObjectField(packageInfo, applicationInfoID);

    jclass applicationInfoCls = env->GetObjectClass(applicationInfo);
    jfieldID sourceDirID = env->GetFieldID(applicationInfoCls, "sourceDir", "Ljava/lang/String;");
    jstring sourceDir = (jstring) env->GetObjectField(applicationInfo, sourceDirID);

    path = env->GetStringUTFChars(sourceDir, 0x00);
    memcpy(appPath, path, strlen(path));

    env->ReleaseStringUTFChars(sourceDir, path);
    env->DeleteLocalRef(cls);
}

void getTopProcess(JNIEnv *env, jobject context, char *topName)
{
    int i = 0;
    jint type;
    double millisec = 1000.0;
    jlong endTime, beginTime;
    jstring packageName = NULL;
    const char *appName = NULL;
    jboolean hasNextEvent = JNI_FALSE;

    // curTime = System.currentTimeMillis();
    jclass system = env->FindClass("java/lang/System");
    jmethodID curTimeID = env->GetStaticMethodID(system, "currentTimeMillis", "()J");
    endTime = env->CallStaticLongMethod(system, curTimeID);
    beginTime = endTime - (30 * millisec);

    // UsageStatsManager usageStatsManager =
    //                         (UsageStatsManager)getSystemService(context.USAGE_STATS_SERVICE);
    jclass cls = env->GetObjectClass(context);
    jmethodID systemServiceID = env->GetMethodID(cls, "getSystemService",
                                    "(Ljava/lang/String;)Ljava/lang/Object;");
    jobject usageStateMgr = env->CallObjectMethod(context, systemServiceID,
                                    env->NewStringUTF("usagestats"));

    // final UsageEvents usageEvents = usageStatsManager.queryEvents(beginTime, endTime);
    jclass usageStatsManagerCls = env->GetObjectClass(usageStateMgr);
    jmethodID usageEventID = env->GetMethodID(usageStatsManagerCls, "queryEvents",
                                "(JJ)Landroid/app/usage/UsageEvents;");
    jobject usageEvents = env->CallObjectMethod(usageStateMgr, usageEventID, beginTime, endTime);

    // usageEvents.hasNextEvent();
    jclass usageEventCls = env->GetObjectClass(usageEvents);
    jmethodID eventID = env->GetMethodID(usageEventCls, "hasNextEvent", "()Z");
    hasNextEvent = env->CallBooleanMethod(usageEvents, eventID);

    while (hasNextEvent == JNI_TRUE && i < 100) {
        // UsageEvents.Event event = new UsageEvents.Event();
        jclass eventCls = env->FindClass("android/app/usage/UsageEvents$Event");
        jmethodID newEventID = env->GetMethodID(eventCls, "<init>", "()V");
        jobject newEvent = env->NewObject(eventCls, newEventID);

        // usageEvents.getNextEvent(event);
        jmethodID nextID = env->GetMethodID(usageEventCls, "getNextEvent",
                                "(Landroid/app/usage/UsageEvents$Event;)Z");
        env->CallBooleanMethod(usageEvents, nextID, newEvent);

        // event.getEventType();
        jmethodID eventTypeID = env->GetMethodID(eventCls, "getEventType", "()I");
        type = env->CallIntMethod(newEvent, eventTypeID);

        if (type == 1) { /* UsageEvents.Event.MOVE_TO_FOREGROUND is 1 */
            // event.getPackageName();
            jmethodID packageNameID = env->GetMethodID(eventCls, "getPackageName",
                                        "()Ljava/lang/String;");
            packageName = (jstring) env->CallObjectMethod(newEvent, packageNameID);

            appName = env->GetStringUTFChars(packageName, NULL);
            LOGD("%dth Event is %s", i++, appName);
            strcpy(topName, appName);
            env->ReleaseStringUTFChars(packageName, appName);
        }

        hasNextEvent = env->CallBooleanMethod(usageEvents, eventID);
    }
}

void getRunningApp(JNIEnv *env, jobject context)
{
    int i = 0;
    jint type;
    double millisec = 1000.0;
    jlong endTime, beginTime;
    jstring packageName = NULL;
    const char *appName = NULL;
    jboolean hasNextEvent = JNI_FALSE;

    // curTime = System.currentTimeMillis();
    jclass system = env->FindClass("java/lang/System");
    jmethodID curTimeID = env->GetStaticMethodID(system, "currentTimeMillis", "()J");
    endTime = env->CallStaticLongMethod(system, curTimeID);
    beginTime = endTime - (30 * millisec);

    // UsageStatsManager usageStatsManager =
    //                          (UsageStatsManager)getSystemService(context.USAGE_STATS_SERVICE);
    jclass cls = env->GetObjectClass(context);
    jmethodID systemServiceID = env->GetMethodID(cls, "getSystemService",
                                    "(Ljava/lang/String;)Ljava/lang/Object;");
    jobject usageStateMgr = env->CallObjectMethod(context, systemServiceID,
                                env->NewStringUTF("usagestats"));

    // final UsageEvents usageEvents = usageStatsManager.queryEvents(beginTime, endTime);
    jclass usageStatsManagerCls = env->GetObjectClass(usageStateMgr);
    jmethodID usageEventID = env->GetMethodID(usageStatsManagerCls, "queryEvents",
                                "(JJ)Landroid/app/usage/UsageEvents;");
    jobject usageEvents = env->CallObjectMethod(usageStateMgr, usageEventID, beginTime, endTime);

    // usageEvents.hasNextEvent();
    jclass usageEventCls = env->GetObjectClass(usageEvents);
    jmethodID eventID = env->GetMethodID(usageEventCls, "hasNextEvent", "()Z");
    hasNextEvent = env->CallBooleanMethod(usageEvents, eventID);

    while (hasNextEvent == JNI_TRUE && i < 100) {
        // UsageEvents.Event event = new UsageEvents.Event();
        jclass eventCls = env->FindClass("android/app/usage/UsageEvents$Event");
        jmethodID newEventID = env->GetMethodID(eventCls, "<init>", "()V");
        jobject newEvent = env->NewObject(eventCls, newEventID);

        // usageEvents.getNextEvent(event);
        jmethodID nextID = env->GetMethodID(usageEventCls, "getNextEvent",
                                "(Landroid/app/usage/UsageEvents$Event;)Z");
        env->CallBooleanMethod(usageEvents, nextID, newEvent);

        // event.getEventType();
        jmethodID eventTypeID = env->GetMethodID(eventCls, "getEventType", "()I");
        type = env->CallIntMethod(newEvent, eventTypeID);

        if (type == 1) { /* UsageEvents.Event.MOVE_TO_FOREGROUND is 1 */
            // event.getPackageName();
            jmethodID packageNameID = env->GetMethodID(eventCls, "getPackageName",
                                        "()Ljava/lang/String;");
            packageName = (jstring) env->CallObjectMethod(newEvent, packageNameID);

            appName = env->GetStringUTFChars(packageName, NULL);
            LOGD("%dth Event is %s", ++i, appName);
            env->ReleaseStringUTFChars(packageName, appName);
        }

        hasNextEvent = env->CallBooleanMethod(usageEvents, eventID);
    }
}

bool isRunningApp(JNIEnv *env, jobject context, const char *appName)
{
    char topName[256] = { 0x00, };

    getTopProcess(env, context, topName);
    LOGD("top process is %s", topName);

    if (strcmp(appName, topName) == 0) {
        return true;
    } else {
        return false;
    }
}
