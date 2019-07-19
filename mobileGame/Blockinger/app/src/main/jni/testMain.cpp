//
// Created by HCR on 2019-05-23.
//

#include <android/log.h>    // for android log function
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <time.h>           // for clock function
#include <sys/time.h>       // for time function
#include <unistd.h>         // for sleep function
#include "openssl/sha.h"    // for SHA function of openSSL
#include "openssl/hmac.h"   // for HMAC function of openSSL
#include <sys/socket.h>     // for socket
#include <arpa/inet.h>      // for socket

#include "testMain.h"

#define LOGV(...) __android_log_print(ANDROID_LOG_VERBOSE, "[LOGV]", __VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG  , "[LOGD]", __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO   , "[LOGI]", __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN   , "[LOGW]", __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR  , "[LOGE]", __VA_ARGS__)

unsigned char testData[32] = {
    0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08,
    0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08,
    0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08,
    0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08
};

unsigned char testKey[32] = {
    0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01,
    0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01,
    0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01,
    0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01
};

unsigned char testHash[32] = {
    0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08,
    0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08,
    0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08,
    0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08
};

unsigned char resultMsg[32 + 1] = {
    0x9e, 0x85, 0x19, 0x61, 0x3f, 0xe3, 0x67, 0xf8,
    0xf3, 0x85, 0x2a, 0xe8, 0x78, 0x5d, 0x58, 0xa0,
    0x12, 0x02, 0xdf, 0x7a, 0x9d, 0x83, 0x74, 0x3a,
    0x9a, 0xe8, 0x85, 0xf4, 0x6e, 0x81, 0x93, 0xf4,
    0x00
};

static void tzmon_itoa(unsigned char *src, int srcLen, char *target, int *targetLen)
{
    for (int i = 0; i < srcLen; i++) {
        sprintf(&target[i * 2], "%02x", (unsigned int)src[i]);
    }

    *targetLen = srcLen * 2;
    target[*targetLen] = 0x00;
}

static void tzmon_atoi(char *src, int srcLen, unsigned char *dest, int *destLen)
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

static void printBuf(char *title, unsigned char *data, int dataLen)
{
    char buffer[1024] = { 0x00, };

    if (dataLen == 0 || data == NULL) return;

    LOGD("%s", title);
    for (int i = 0; i < dataLen; i++) {
        sprintf(&buffer[i * 5], "0x%02x ", (unsigned int)data[i]);
    }

    LOGD("%s", buffer);
}

int tzmon_xor(unsigned char *first, int firstLen, unsigned char *second, int secondLen, unsigned char *out, int outLen)
{
    if (first == NULL || second == NULL || out == NULL) {
        LOGD("Bad Parameter");
        return 1;
    }

    if (outLen != firstLen || outLen != secondLen) {
        LOGD("Bad Parameter");
        return 1;
    }

    for (int i = 0; i < outLen; i++) {
        out[i] = first[i] ^ second[i];
    }

    return 0;
}

int tzmon_sha256(unsigned char *data, int dataLen, unsigned char *hash, int *hashLen)
{
    SHA256_CTX sha256;

    if (dataLen == 0 || data == NULL || hash == NULL || hashLen == NULL) {
        LOGD("Bad parameter");
        return 1;
    }

    SHA256_Init(&sha256);
    SHA256_Update(&sha256, data, dataLen);
    SHA256_Final(hash, &sha256);

    *hashLen = SHA256_DIGEST_LENGTH;

    return 0;
}

int tzmon_hmac_sha256(unsigned char *key, int keyLen, unsigned char *data, int dataLen, unsigned char *hmac, int *hmacLen)
{

    HMAC_CTX hctx;
    const EVP_MD *evpmd;

    if (keyLen == 0 || dataLen == 0 || key == NULL || data == NULL || hmac == NULL || hmacLen == NULL) {
        LOGD("Bad parameter");
        return 1;
    }

    OpenSSL_add_all_digests();
    evpmd = EVP_get_digestbyname("sha256");

    HMAC_Init(&hctx, key, keyLen, evpmd);
    HMAC_Update(&hctx, data, dataLen);
    HMAC_Final(&hctx, hmac, (unsigned int *)hmacLen);

    HMAC_CTX_cleanup(&hctx);

    return 0;
}

void generate_appHash(const char *path, char *appHash)
{
    FILE *fp = fopen(path, "r");
    SHA256_CTX sha256;

    char input_data[2048] = { 0x00, };
    unsigned char hash[SHA256_DIGEST_LENGTH] = { 0x00, };
    char hash_string[SHA256_DIGEST_LENGTH * 2 + 1] = { 0x00, };

    SHA256_Init(&sha256);

    while (fgets(input_data, sizeof(input_data), fp) != NULL) {
        SHA256_Update(&sha256, input_data, sizeof(input_data));
        memset(input_data, 0x00, sizeof(input_data));
    }

    SHA256_Final(hash, &sha256);

    for (int i = 0; i < SHA256_DIGEST_LENGTH; i++) {
        sprintf(&hash_string[i*2], "%02x", (unsigned int)hash[i]);
    }

    memcpy(appHash, hash_string, sizeof(hash_string));
    fclose(fp);
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

    aTimeGap[0] = (endVal.tv_sec - startVal.tv_sec) + (( endVal.tv_usec - startVal.tv_usec ) / microsec);
    aTimeGap[1] = (endSpec.tv_sec - startSpec.tv_sec) + (( endSpec.tv_nsec - startSpec.tv_nsec ) / nanosec);
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
    jmethodID getPackageManager = env->GetMethodID(cls, "getPackageManager", "()Landroid/content/pm/PackageManager;");

    jclass packageManagerCls = env->FindClass("android/content/pm/PackageManager");
    jmethodID getPackageInfo = env->GetMethodID(packageManagerCls, "getPackageInfo", "(Ljava/lang/String;I)Landroid/content/pm/PackageInfo;");

    jobject packageManager = env->CallObjectMethod(context, getPackageManager);
    jstring packageName = (jstring) env->CallObjectMethod(context, getPackageName);
    jobject packageInfo = env->CallObjectMethod(packageManager, getPackageInfo, packageName, 0x80);

    jclass packageInfoCls = env->FindClass("android/content/pm/PackageInfo");
    jfieldID applicationInfoID = env->GetFieldID(packageInfoCls, "applicationInfo", "Landroid/content/pm/ApplicationInfo;");
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

    // UsageStatsManager usageStatsManager = (UsageStatsManager)getSystemService(context.USAGE_STATS_SERVICE);
    jclass cls = env->GetObjectClass(context);
    jmethodID systemServiceID = env->GetMethodID(cls, "getSystemService", "(Ljava/lang/String;)Ljava/lang/Object;");
    jobject usageStateMgr = env->CallObjectMethod(context, systemServiceID, env->NewStringUTF("usagestats"));

    // final UsageEvents usageEvents = usageStatsManager.queryEvents(beginTime, endTime);
    jclass usageStatsManagerCls = env->GetObjectClass(usageStateMgr);
    jmethodID usageEventID = env->GetMethodID(usageStatsManagerCls, "queryEvents", "(JJ)Landroid/app/usage/UsageEvents;");
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
        jmethodID nextID = env->GetMethodID(usageEventCls, "getNextEvent", "(Landroid/app/usage/UsageEvents$Event;)Z");
        env->CallBooleanMethod(usageEvents, nextID, newEvent);

        // event.getEventType();
        jmethodID eventTypeID = env->GetMethodID(eventCls, "getEventType", "()I");
        type = env->CallIntMethod(newEvent, eventTypeID);

        if (type == 1) { /* UsageEvents.Event.MOVE_TO_FOREGROUND is 1 */
            // event.getPackageName();
            jmethodID packageNameID = env->GetMethodID(eventCls, "getPackageName", "()Ljava/lang/String;");
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

    // UsageStatsManager usageStatsManager = (UsageStatsManager)getSystemService(context.USAGE_STATS_SERVICE);
    jclass cls = env->GetObjectClass(context);
    jmethodID systemServiceID = env->GetMethodID(cls, "getSystemService", "(Ljava/lang/String;)Ljava/lang/Object;");
    jobject usageStateMgr = env->CallObjectMethod(context, systemServiceID, env->NewStringUTF("usagestats"));

    // final UsageEvents usageEvents = usageStatsManager.queryEvents(beginTime, endTime);
    jclass usageStatsManagerCls = env->GetObjectClass(usageStateMgr);
    jmethodID usageEventID = env->GetMethodID(usageStatsManagerCls, "queryEvents", "(JJ)Landroid/app/usage/UsageEvents;");
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
        jmethodID nextID = env->GetMethodID(usageEventCls, "getNextEvent", "(Landroid/app/usage/UsageEvents$Event;)Z");
        env->CallBooleanMethod(usageEvents, nextID, newEvent);

        // event.getEventType();
        jmethodID eventTypeID = env->GetMethodID(eventCls, "getEventType", "()I");
        type = env->CallIntMethod(newEvent, eventTypeID);

        if (type == 1) { /* UsageEvents.Event.MOVE_TO_FOREGROUND is 1 */
            // event.getPackageName();
            jmethodID packageNameID = env->GetMethodID(eventCls, "getPackageName", "()Ljava/lang/String;");
            packageName = (jstring) env->CallObjectMethod(newEvent, packageNameID);

            appName = env->GetStringUTFChars(packageName, NULL);
            LOGD("%dth Event is %s", ++i, appName);
            env->ReleaseStringUTFChars(packageName, appName);
        }

        hasNextEvent = env->CallBooleanMethod(usageEvents, eventID);
    }
}

int isRunningApp(JNIEnv *env, jobject context, const char *appName)
{
    int retVal = false;
    char topName[256] = { 0x00, };

    getTopProcess(env, context, topName);
    LOGD("top process is %s", topName);

    if (strcmp(appName, topName) == 0) {
        retVal = true;
    } else {
        retVal = false;
    }

    return retVal;
}

static int socketWrite(int sockFD, char *msg, int msgLen)
{
    char outBuf[1024] = { 0x00, };
    int outBufLen = sizeof(outBuf);
    int retVal = -1;

    memcpy(outBuf, msg, msgLen);
    retVal = write(sockFD, outBuf, msgLen);

    return retVal;
}

static int socketRead(int sockFD, char *buf, int *bufLen)
{
    int retVal = -1;

    if (buf == NULL || bufLen == NULL) {
        LOGD("Bad Parameter");
        return -1;
    }

    retVal = read(sockFD, buf, *bufLen);
    *bufLen = retVal;

    return retVal;
}

static void closeSocket(int sockFD)
{
    socketWrite(sockFD, "quit", (int)strlen("quit"));
    close(sockFD);
}

int _call_tzmonAbusingDetection(const char *appName)
{
    int sockFD, port = 9999;
    char *ip = "163.152.127.108";
    struct sockaddr_in sockAddr;

    char cmd[1024] = { 0x00, };
    char out[1024] = { 0x00, };
    int outLen = sizeof(out);

    if (appName == NULL) {
        LOGD("Bad Parameter");
        return 1;
    }

    sockFD = socket(AF_INET, SOCK_STREAM, 0);
    sockAddr.sin_family = AF_INET;
    sockAddr.sin_addr.s_addr = inet_addr(ip);
    sockAddr.sin_port = htons(port);

    if (connect(sockFD, (struct sockaddr *)&sockAddr, sizeof(sockAddr)) < 0) {
        LOGD("Connect error: ");
        return 1;
    }

    strcpy(cmd, "adb shell /vendor/bin/optee_tzmon APRETOKEN ");
    strcat(cmd, appName);
    if (socketWrite(sockFD, cmd, (int)strlen(cmd)) <= 0) {
        LOGD("Write error: ");
        return 1;
    }

    if (socketRead(sockFD, out, &outLen) <= 0) {
        LOGD("Read error: ");
        return 1;
    }

    closeSocket(sockFD);

    LOGD("[Command] %s", cmd);
    LOGD("[Result] %s(%d)", out, outLen);
    if (strcmp(out, "fail") == 0) {
        return 1;
    } else {
        return 0;
    }
}

int _call_tzmonTA(char *cmd, char *out, int *outLen)
{
    int sockFD, port = 9999;
    char *ip = "163.152.127.108";
    struct sockaddr_in sockAddr;

    if (cmd == NULL || out == NULL || outLen == NULL) {
        LOGD("Bad Parameter");
        return 1;
    }

    sockFD = socket(AF_INET, SOCK_STREAM, 0);
    sockAddr.sin_family = AF_INET;
    sockAddr.sin_addr.s_addr = inet_addr(ip);
    sockAddr.sin_port = htons(port);

    if (connect(sockFD, (struct sockaddr *)&sockAddr, sizeof(sockAddr)) < 0) {
        LOGD("Connect error: ");
        return 1;
    }

    if (socketWrite(sockFD, cmd, (int)strlen(cmd)) <= 0) {
        LOGD("Write error: ");
        return 1;
    }

    if (socketRead(sockFD, out, outLen) <= 0) {
        LOGD("Read error: ");
        return 1;
    }

    closeSocket(sockFD);

    LOGD("[Command] %s", cmd);
    LOGD("[Result] %s(%d)", out, *outLen);
    if (strcmp(out, "fail") == 0) {
        return 1;
    } else {
        return 0;
    }
}

static unsigned char iToken[32] = { 0x00, };
static unsigned char uToken[32] = { 0x00, };
static unsigned char aToken[32] = { 0x00, };
static unsigned char tToken[32] = { 0x00, };

static int iTokenLen, uTokenLen, aTokenLen, tTokenLen;

int _tzmon_aPreToken(JNIEnv *env, jobject context)
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

    // UsageStatsManager usageStatsManager = (UsageStatsManager)getSystemService(context.USAGE_STATS_SERVICE);
    jclass cls = env->GetObjectClass(context);
    jmethodID systemServiceID = env->GetMethodID(cls, "getSystemService", "(Ljava/lang/String;)Ljava/lang/Object;");
    jobject usageStateMgr = env->CallObjectMethod(context, systemServiceID, env->NewStringUTF("usagestats"));

    // final UsageEvents usageEvents = usageStatsManager.queryEvents(beginTime, endTime);
    jclass usageStatsManagerCls = env->GetObjectClass(usageStateMgr);
    jmethodID usageEventID = env->GetMethodID(usageStatsManagerCls, "queryEvents", "(JJ)Landroid/app/usage/UsageEvents;");
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
        jmethodID nextID = env->GetMethodID(usageEventCls, "getNextEvent", "(Landroid/app/usage/UsageEvents$Event;)Z");
        env->CallBooleanMethod(usageEvents, nextID, newEvent);

        // event.getEventType();
        jmethodID eventTypeID = env->GetMethodID(eventCls, "getEventType", "()I");
        type = env->CallIntMethod(newEvent, eventTypeID);

        if (type == 1) { /* UsageEvents.Event.MOVE_TO_FOREGROUND is 1 */
            // event.getPackageName();
            jmethodID packageNameID = env->GetMethodID(eventCls, "getPackageName", "()Ljava/lang/String;");
            packageName = (jstring) env->CallObjectMethod(newEvent, packageNameID);

            appName = env->GetStringUTFChars(packageName, NULL);
            LOGD("%dth Event is %s", ++i, appName);
            if (_call_tzmonAbusingDetection(appName) != 0) {
                LOGD("_call_tzmonAbusingDetection error: ");
                return 1;
            }
            env->ReleaseStringUTFChars(packageName, appName);
        }

        hasNextEvent = env->CallBooleanMethod(usageEvents, eventID);
    }

    return 0;
}

int TZMON_SecureUpdate()
{
    char cmd[1024] = { 0x00, };
    char out[1024] = { 0x00, };
    char arg[1024] = { 0x00, };

    unsigned char temp[256] = { 0x00, };
    unsigned char encFile[256] = { 0x00, };
    unsigned char mac[256] = { 0x00, };

    int outLen, argLen, tempLen, encFileLen, macLen;

    outLen = sizeof(out);
    memset(out, 0x00, outLen);
    tzmon_itoa(iToken, iTokenLen, arg, &argLen);
    strcpy(cmd, "adb shell /vendor/bin/optee_tzmon UKEY ");
    strcat(cmd, arg);
    if (_call_tzmonTA(cmd, out, &outLen) != 0) {
        LOGD("_call_tzmonTA error: ");
        return 1;
    }

    // uToken
    tzmon_atoi(out, outLen, uToken, &uTokenLen);
    printBuf("uToken", uToken, uTokenLen);

    outLen = sizeof(out);
    memset(out, 0x00, outLen);
    tzmon_itoa(uToken, uTokenLen, arg, &argLen);
    strcpy(cmd, "adb shell /vendor/bin/optee_tzmon UTOKEN ");
    strcat(cmd, arg);
    if (_call_tzmonTA(cmd, out, &outLen) != 0) {
        LOGD("_call_tzmonTA error:");
        return 1;
    }

    // iVerify
    tzmon_atoi(out, outLen, temp, &tempLen);
    memcpy(encFile, temp + 2, tempLen - 2);
    encFileLen = tempLen - 2;
    printBuf("encFile", encFile, encFileLen);

    tzmon_hmac_sha256(uToken, uTokenLen, encFile, encFileLen - 32, mac, &macLen);
    printBuf("mac", mac, macLen);

    memset(temp, 0x00, sizeof(temp));
    memcpy(temp, encFile, encFileLen);
    memcpy(temp + encFileLen, mac, macLen);
    tempLen = encFileLen + macLen;
    tzmon_itoa(temp, tempLen, arg, &argLen);
    strcpy(cmd, "adb shell /vendor/bin/optee_tzmon UVERIFY ");
    strcat(cmd, arg);

    outLen = sizeof(out);
    memset(out, 0x00, outLen);
    if (_call_tzmonTA(cmd, out, &outLen) != 0) {
        LOGD("_call_tzmonTA error: ");
        return 1;
    }

    return 0;
}

int TZMON_CheckAppIntegrity(char *pre_appHash)
{
    char cmd[1024] = { 0x00, };
    char out[1024] = { 0x00, };
    char arg[1024] = { 0x00, };

    unsigned char preHash[32] = { 0x00, };
    unsigned char iKey[32] = { 0x00, };
    unsigned char ixor[32] = { 0x00, };
    unsigned char appHash[32] = { 0x00, };
    unsigned char cMsg[32] = { 0x00, };

    int outLen, iKeyLen, preHashLen, ixorLen, appHashLen, cMsgLen, argLen;

    outLen = sizeof(out);
    memset(out, 0x00, outLen);
    strcpy(cmd, "adb shell /vendor/bin/optee_tzmon INIT_FLAG all");
    if (_call_tzmonTA(cmd, out, &outLen) != 0) {
        LOGD("_call_tzmonTA error: ");
        return 1;
    }

    outLen = sizeof(out);
    memset(out, 0x00, outLen);
    strcpy(cmd, "adb shell /vendor/bin/optee_tzmon IKEY 1");
    if (_call_tzmonTA(cmd, out, &outLen) != 0) {
        LOGD("_call_tzmonTA error: ");
        return 1;
    }

    // iKey
    tzmon_atoi(out, outLen, iKey, &iKeyLen);
    printBuf("iKey", iKey, iKeyLen);

#if 0
    // preHash
    tzmon_atoi(pre_appHash, strlen(pre_appHash), preHash, &preHashLen);
#else
    preHashLen = 32;
    memcpy(preHash, testHash, preHashLen);
#endif
    printBuf("preHash", preHash, preHashLen);

    // preHash XOR iKey
    ixorLen = 32;
    tzmon_xor(preHash, preHashLen, iKey, iKeyLen, ixor, ixorLen);
    printBuf("pre_appHash XOR iKey", ixor, ixorLen);

    // appHash
    tzmon_sha256(ixor, ixorLen, appHash, &appHashLen);
    printBuf("appHash", appHash, appHashLen);

    // iToken
    outLen = sizeof(out);
    argLen = sizeof(arg);
    memset(out, 0x00, outLen);
    memset(arg, 0x00, argLen);
    tzmon_itoa(appHash, appHashLen, arg, &argLen);

    strcpy(cmd, "adb shell /vendor/bin/optee_tzmon ITOKEN ");
    strcat(cmd, arg);
    if (_call_tzmonTA(cmd, out, &outLen) != 0) {
        LOGD("_call_tzmonTA error: ");
        return 1;
    }

    tzmon_atoi(out, outLen, iToken, &iTokenLen);
    printBuf("iToken", iToken, iTokenLen);

    // cMsg
    tzmon_hmac_sha256(iToken, iTokenLen, resultMsg, 32, cMsg, &cMsgLen);
    printBuf("cMsg", cMsg, cMsgLen);

    outLen = sizeof(out);
    argLen = sizeof(arg);
    memset(out, 0x00, outLen);
    memset(arg, 0x00, argLen);
    tzmon_itoa(cMsg, cMsgLen, arg, &argLen);

    strcpy(cmd, "adb shell /vendor/bin/optee_tzmon IVERIFY ");
    strcat(cmd, arg);
    if (_call_tzmonTA(cmd, out, &outLen) != 0) {
        LOGD("_call_tzmonTA error: ");
        return 1;
    }

    return 0;
}

int TZMON_AbusingDetection(JNIEnv *env, jobject context)
{
    char cmd[1024] = { 0x00, };
    char out[1024] = { 0x00, };
    char arg[1024] = { 0x00, };
    char preToken[32] = { 0x00, };

    unsigned char cMsg[32] = { 0x00, };

    int outLen, argLen, preTokenLen, cMsgLen;

    if (_tzmon_aPreToken(env, context) != 0) {
        LOGD("_tzmon_aPreToken error: ");
        return 1;
    }

    // aToken
    preTokenLen = sizeof(preToken);
    memset(preToken, 0x00, preTokenLen);
    tzmon_xor((unsigned char *)iToken, 32, (unsigned char *)uToken, 32, (unsigned char *)preToken, preTokenLen);
    printBuf("aPreToken", (unsigned char *)preToken, preTokenLen);

    outLen = sizeof(out);
    memset(out, 0x00, outLen);
    tzmon_itoa((unsigned char *)preToken, preTokenLen, arg, &argLen);
    strcpy(cmd, "adb shell /vendor/bin/optee_tzmon ATOKEN ");
    strcat(cmd, arg);
    if (_call_tzmonTA(cmd, out, &outLen) != 0) {
        LOGD("_call_tzmonTA error: ");
        return 1;
    }

    tzmon_atoi(out, outLen, aToken, &aTokenLen);
    printBuf("aToken", aToken, aTokenLen);

    // cMsg
    tzmon_hmac_sha256(aToken, aTokenLen, resultMsg, 32, cMsg, &cMsgLen);
    printBuf("cMsg", cMsg, cMsgLen);

    outLen = sizeof(out);
    argLen = sizeof(arg);
    memset(out, 0x00, outLen);
    memset(arg, 0x00, argLen);
    tzmon_itoa(cMsg, cMsgLen, arg, &argLen);

    strcpy(cmd, "adb shell /vendor/bin/optee_tzmon AVERIFY ");
    strcat(cmd, arg);
    if (_call_tzmonTA(cmd, out, &outLen) != 0) {
        LOGD("_call_tzmonTA error: ");
        return 1;
    }

    return 0;
}

int TZMON_TimerSync(JNIEnv *env)
{
    char cmd[1024] = { 0x00, };
    char out[1024] = { 0x00, };
    char arg[1024] = { 0x00, };
    char tGap[10] = { 0x00, };
    char temp[32] = { 0x00, };
    char preToken[32] = { 0x00, };

    unsigned char cMsg[32] = { 0x00, };

    double aTimeGap[3] = { 0.0, 0.0, 0.0 };

    int outLen, argLen, preTokenLen, tempLen, cMsgLen;

    getCurrentTime(env, aTimeGap);
    for (int i = 0; i < 3; i++) {
        memset(tGap, 0x00, sizeof(tGap));
        sprintf(tGap, "%0.4f", aTimeGap[i]);

        outLen = sizeof(out);
        memset(out, 0x00, outLen);
        memset(cmd, 0x00, sizeof(cmd));
        strcpy(cmd, "adb shell /vendor/bin/optee_tzmon TPRETOKEN ");
        strcat(cmd, tGap);
        if (_call_tzmonTA(cmd, out, &outLen) != 0) {
            LOGD("_call_tzmonTA error: ");
            return 1;
        }
    }

    // tToken
    tempLen = sizeof(temp);
    tzmon_xor((unsigned char *)iToken, 32, (unsigned char *)uToken, 32, (unsigned char *)temp, tempLen);
    printBuf("temp", (unsigned char *)temp, tempLen);

    preTokenLen = sizeof(preToken);
    tzmon_xor((unsigned char *)temp, tempLen, (unsigned char *)aToken, 32, (unsigned char *)preToken, preTokenLen);
    printBuf("preToken", (unsigned char *)preToken, preTokenLen);

    outLen = sizeof(out);
    memset(out, 0x00, outLen);
    tzmon_itoa((unsigned char *)preToken, preTokenLen, arg, &argLen);
    strcpy(cmd, "adb shell /vendor/bin/optee_tzmon TTOKEN ");
    strcat(cmd, arg);
    if (_call_tzmonTA(cmd, out, &outLen) != 0) {
        LOGD("_call_tzmonTA error: ");
        return 1;
    }

    tzmon_atoi(out, outLen, tToken, &tTokenLen);
    printBuf("tToken", tToken, tTokenLen);

    // cMsg
    tzmon_hmac_sha256(tToken, tTokenLen, resultMsg, 32, cMsg, &cMsgLen);
    printBuf("cMsg", cMsg, cMsgLen);

    outLen = sizeof(out);
    argLen = sizeof(arg);
    memset(out, 0x00, outLen);
    memset(arg, 0x00, argLen);
    tzmon_itoa(cMsg, cMsgLen, arg, &argLen);

    strcpy(cmd, "adb shell /vendor/bin/optee_tzmon TVERIFY ");
    strcat(cmd, arg);
    if (_call_tzmonTA(cmd, out, &outLen) != 0) {
        LOGD("_call_tzmonTA error: ");
        return 1;
    }

    return 0;
}

JNIEXPORT void JNICALL Java_org_blockinger2_game_activities_MainActivity_jniSocket(JNIEnv *env, jobject context)
{
#if 0
    int sockFD, outBufLen;
    char outBuf[255] = { 0x00, };
    struct sockaddr_in sockAddr;

    char *ip = "163.152.127.108";
    int port = 9999;

    char appPath[1024] = { 0x00, };
    char appHash[SHA256_DIGEST_LENGTH * 2 + 1] = { 0x00, };

    sockFD = socket(AF_INET, SOCK_STREAM, 0);
    sockAddr.sin_family = AF_INET;
    sockAddr.sin_addr.s_addr = inet_addr(ip);
    sockAddr.sin_port = htons(port);
    outBufLen = sizeof(outBuf);

    if (connect(sockFD, (struct sockaddr *)&sockAddr, sizeof(sockAddr)) < 0) {
        LOGD("Connect error: ");
        return;
    }

    getAppPath(env, context, appPath);
    socketWrite(sockFD, appPath, (int)strlen(appPath));

    generate_appHash(appPath, appHash);
    socketWrite(sockFD, appHash, (int)strlen(appHash));

    closeSocket(sockFD);
#else
    char *cmd = "adb shell /vendor/bin/optee_tzmon SHA256 test";
    char out[1024] = { 0x00, };
    int outLen = sizeof(out);

    _call_tzmonTA(cmd, out, &outLen);
#endif
}

JNIEXPORT jboolean JNICALL Java_org_blockinger2_game_activities_MainActivity_jniapphashtest(JNIEnv *env, jobject context)
{
    jboolean retVal = JNI_TRUE;
    char appHash[SHA256_DIGEST_LENGTH * 2 + 1] = { 0x00, };
    char path[1024] = { 0x00, };
    char name[256] = { 0x00, };
    char *list[256] = { { 0x00, }, };
    int listLen = 0;

    getAppPath(env, context, path);
    LOGD("appPath: %s", path);

    getAppName(env, context, name);
    LOGD("appName: %s", name);

    generate_appHash(path, appHash);
    LOGD("pre_appHash: %s", appHash);

    if (isRunningApp(env, context, name) != true) {
        LOGD("this path is wrong!!!");
        retVal = JNI_FALSE;
        return (bool)retVal;
    }

    if (TZMON_CheckAppIntegrity(appHash) != 0) {
        LOGD("TZMON_CheckAppIntegrity error: ");
        retVal = JNI_FALSE;
    }

    if (TZMON_SecureUpdate() != 0) {
        LOGD("TZMON_SecureUpdate error: ");
        retVal = JNI_FALSE;
    }

    if (TZMON_AbusingDetection(env, context) != 0) {
        LOGD("TZMON_AbusingDetection error: ");
        retVal = JNI_FALSE;
    }

    if (TZMON_TimerSync(env) != 0) {
        LOGD("TZMON_TimerSync error: ");
        retVal = JNI_FALSE;
    }

    return (bool)retVal;

#if 0
    double aTimeGap[3] = { 0.0, 0.0, 0.0 };
    getCurrentTime(env, aTimeGap);
    for (int i = 0; i < 3; i++) {
        LOGD("[%d]: %0.3f sec", i + 1, aTimeGap[i]);
    }

    getRunningApp(env, context);

    unsigned char testHash[32] = { 0x00, };
    int testHashLen;

    printBuf("Test Data for HASH", testData, 32);
    tzmon_sha256(testData, 32, testHash, &testHashLen);
    printBuf("Hashed Data", testHash, testHashLen);

    unsigned char testHMAC[32] = { 0x00, };
    unsigned int testHMACLen;

    printBuf("Test Key for HMAC", testKey, 32);
    printBuf("Test Data for HMAC", testData, 32);
    tzmon_hmac_sha256(testKey, 32, testData, 32, testHMAC, &testHMACLen);
    printBuf("HMAC Data", testHMAC, testHMACLen);

    unsigned char out[32] = { 0x00, };

    printBuf("First Data", testData, 32);
    printBuf("Second Data", testKey, 32);
    tzmon_xor(testData, 32, testKey, 32, out, 32);
    printBuf("XOR Data", out, 32);

    return env->NewStringUTF(appHash);
#endif
}

