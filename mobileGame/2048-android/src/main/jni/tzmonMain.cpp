#include <android/log.h>    // for android log function
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <time.h>           // for clock function
#include <sys/time.h>       // for time function
#include <unistd.h>         // for sleep function
#include "openssl/sha.h"    // for SHA function of openSSL

#include "testInput.h"
#include "tzmonMain.h"
#include "tzmonCrypto.h"
#include "tzmonSocket.h"
#include "tzmonUtil.h"

#ifndef SIM_MODE
#include "optee/tee_client_api.h"
#include "tzmonTEEC.h"
#endif

static unsigned char iToken[32] = { 0x00, };
static unsigned char uToken[32] = { 0x00, };
static unsigned char aToken[32] = { 0x00, };
static unsigned char tToken[32] = { 0x00, };

static int iTokenLen, uTokenLen, aTokenLen, tTokenLen;

static bool tzmonInit()
{
    char cmd[1024] = { 0x00, };
    char out[1024] = { 0x00, };

    unsigned char res[1] = { 0x00, };

    int outLen = sizeof(out), resLen = sizeof(res);

#ifdef SIM_MODE
    strcpy(cmd, "adb shell /vendor/bin/optee_tzmon INIT_FLAG all");
    if (_call_tzmonTA(cmd, out, &outLen) != true) {
        LOGD("_call_tzmonTA error: INIT_FLAG");
        return false;
    }

    tzmon_atoi(out, outLen, res, &resLen);
#else
    if (teec_tzmonTA(TA_TZMON_CMD_INIT_FLAG, (unsigned char *)"all",
                    (unsigned char *)out, (uint32_t *)&outLen) != TEEC_SUCCESS) {
        LOGD("teec_tzmonTA error: INIT_FLAG");
        return false;
    }

    resLen = outLen;
    memcpy(res, out, resLen);
#endif

    if (res[0] != 0x00) {
        return false;
    }

    return true;
}

static bool tzmonCheckAppIntegrity(char *pre_appHash)
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

#ifdef SIM_MODE
    strcpy(cmd, "adb shell /vendor/bin/optee_tzmon IKEY 1");
    if (_call_tzmonTA(cmd, out, &outLen) != true) {
        LOGD("_call_tzmonTA error: IKEY");
        return false;
    }

    // iKey
    tzmon_atoi(out, outLen, iKey, &iKeyLen);
    printBuf("iKey", iKey, iKeyLen);
#else
    if (teec_tzmonTA(TA_TZMON_CMD_IKEY, (unsigned char *)"1",
                    (unsigned char *)out, (uint32_t *)&outLen) != TEEC_SUCCESS) {
        LOGD("teec_tzmonTA error: IKEY");
        return false;
    }

    // iKey
    iKeyLen = outLen;
    memcpy(iKey, out, iKeyLen);
#endif

    // preHash
#ifdef USE_FIXAPPHASH
    preHashLen = 32;
    memcpy(preHash, testHash, preHashLen);
    printBuf("preHash", preHash, preHashLen);
#else
    tzmon_atoi(pre_appHash, strlen(pre_appHash), preHash, &preHashLen);
    printBuf("preHash", preHash, preHashLen);
#endif

    // preHash XOR iKey
    ixorLen = 32;
    if (tzmon_xor(preHash, preHashLen, iKey, iKeyLen, ixor, ixorLen) != true) {
        LOGD("tzmon_xor error");
        return false;
    }
    printBuf("pre_appHash XOR iKey", ixor, ixorLen);

    // appHash
    if (tzmon_sha256(ixor, ixorLen, appHash, &appHashLen) != true) {
        LOGD("tzmon_sha256 error");
        return false;
    }
    printBuf("appHash", appHash, appHashLen);

    // iToken
    outLen = sizeof(out);
    argLen = sizeof(arg);
    memset(out, 0x00, outLen);
    memset(arg, 0x00, argLen);
    tzmon_itoa(appHash, appHashLen, arg, &argLen);

#ifdef SIM_MODE
    strcpy(cmd, "adb shell /vendor/bin/optee_tzmon ITOKEN ");
    strcat(cmd, arg);
    if (_call_tzmonTA(cmd, out, &outLen) != true) {
        LOGD("_call_tzmonTA error: ITOKEN");
        return false;
    }

    tzmon_atoi(out, outLen, iToken, &iTokenLen);
    printBuf("iToken", iToken, iTokenLen);
#else
    if (teec_tzmonTA(TA_TZMON_CMD_ITOKEN, (unsigned char *)arg,
                    (unsigned char *)out, (uint32_t *)&outLen) != TEEC_SUCCESS) {
        LOGD("teec_tzmonTA error: ITOKEN");
        return false;
    }

    iTokenLen = outLen;
    memcpy(iToken, out, iTokenLen);
#endif

    // cMsg
    tzmon_hmac_sha256(iToken, iTokenLen, resultMsg, 32, cMsg, &cMsgLen);
    printBuf("cMsg", cMsg, cMsgLen);

    outLen = sizeof(out);
    argLen = sizeof(arg);
    memset(out, 0x00, outLen);
    memset(arg, 0x00, argLen);
    tzmon_itoa(cMsg, cMsgLen, arg, &argLen);

#ifdef SIM_MODE
    strcpy(cmd, "adb shell /vendor/bin/optee_tzmon IVERIFY ");
    strcat(cmd, arg);
    if (_call_tzmonTA(cmd, out, &outLen) != true) {
        LOGD("_call_tzmonTA error: IVERIFY");
        return false;
    }
#else
    if (teec_tzmonTA(TA_TZMON_CMD_IVERIFY, (unsigned char *)arg,
                    (unsigned char *)out, (uint32_t *)&outLen) != TEEC_SUCCESS) {
        LOGD("teec_tzmonTA error: IVERIFY");
        return false;
    }
#endif

    return true;
}

static bool tzmonSecureUpdate()
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

#ifdef SIM_MODE
    strcpy(cmd, "adb shell /vendor/bin/optee_tzmon UKEY ");
    strcat(cmd, arg);
    if (_call_tzmonTA(cmd, out, &outLen) != true) {
        LOGD("_call_tzmonTA error: UKEY");
        return false;
    }

    // uToken
    tzmon_atoi(out, outLen, uToken, &uTokenLen);
    printBuf("uToken", uToken, uTokenLen);
#else
    if (teec_tzmonTA(TA_TZMON_CMD_UKEY, (unsigned char *)arg,
                    (unsigned char *)out, (uint32_t *)&outLen) != TEEC_SUCCESS) {
        LOGD("teec_tzmonTA error: UKEY");
        return false;
    }

    uTokenLen = outLen;
    memcpy(uToken, out, uTokenLen);
#endif

    outLen = sizeof(out);
    memset(out, 0x00, outLen);
    tzmon_itoa(uToken, uTokenLen, arg, &argLen);

#ifdef SIM_MODE
    strcpy(cmd, "adb shell /vendor/bin/optee_tzmon UTOKEN ");
    strcat(cmd, arg);
    if (_call_tzmonTA(cmd, out, &outLen) != true) {
        LOGD("_call_tzmonTA error: UTOKEN");
        return false;
    }

    // iVerify
    tzmon_atoi(out, outLen, temp, &tempLen);
#else
    if (teec_tzmonTA(TA_TZMON_CMD_UTOKEN, (unsigned char *)arg,
                    (unsigned char *)out, (uint32_t *)&outLen) != TEEC_SUCCESS) {
        LOGD("teec_tzmonTA error: UTOKEN");
        return false;
    }

    // iVerify
    tempLen = outLen;
    memcpy(temp, out, tempLen);
#endif

    memcpy(encFile, temp + 2, tempLen - 2);
    encFileLen = tempLen - 2;
    printBuf("encFile", encFile, encFileLen);

    tzmon_hmac_sha256(uToken, uTokenLen, encFile, encFileLen - 32, mac, &macLen);
    printBuf("mac", mac, macLen);

    memset(temp, 0x00, sizeof(temp));
    memcpy(temp, encFile, encFileLen);
    memcpy(temp + encFileLen, mac, macLen);
    tempLen = encFileLen + macLen;

    outLen = sizeof(out);
    memset(out, 0x00, outLen);
    tzmon_itoa(temp, tempLen, arg, &argLen);

#ifdef SIM_MODE
    strcpy(cmd, "adb shell /vendor/bin/optee_tzmon UVERIFY ");
    strcat(cmd, arg);
    if (_call_tzmonTA(cmd, out, &outLen) != true) {
        LOGD("_call_tzmonTA error: UVERIFY");
        return false;
    }
#else
    if (teec_tzmonTA(TA_TZMON_CMD_UVERIFY, (unsigned char *)arg,
                    (unsigned char *)out, (uint32_t *)&outLen) != TEEC_SUCCESS) {
        LOGD("teec_tzmonTA error: UVERIFY");
        return false;
    }
#endif

    return true;
}

static bool _tzmon_aPreToken(JNIEnv *env, jobject context)
{
    int i = 0;
    jint type;
    double millisec = 1000.0;
    jlong endTime, beginTime;
    jstring packageName = NULL;
    const char *appName = NULL;
    jboolean hasNextEvent = JNI_FALSE;

#ifndef SIM_MODE
    char out[1024] = { 0x00, };
    int outLen;
#endif

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
#ifdef SIM_MODE
            if (_call_tzmonAbusingDetection(appName) != true) {
                LOGD("_call_tzmonAbusingDetection error: ");
                return false;
            }
#else
            outLen = sizeof(out);
            memset(out, 0x00, outLen);
            if (teec_tzmonTA(TA_TZMON_CMD_APRETOKEN, (unsigned char *)appName,
                            (unsigned char *)out, (uint32_t *)&outLen) != TEEC_SUCCESS) {
                LOGD("teec_tzmonTA error: APRETOKEN");
                return false;
            }
#endif
            env->ReleaseStringUTFChars(packageName, appName);
        }

        hasNextEvent = env->CallBooleanMethod(usageEvents, eventID);
    }

    return true;
}

static bool tzmonAbusingDetection(JNIEnv *env, jobject context)
{
    char cmd[1024] = { 0x00, };
    char out[1024] = { 0x00, };
    char arg[1024] = { 0x00, };
    char preToken[32] = { 0x00, };

    unsigned char cMsg[32] = { 0x00, };

    int outLen, argLen, preTokenLen, cMsgLen;

    if (_tzmon_aPreToken(env, context) != true) {
        LOGD("_tzmon_aPreToken error: ");
        return false;
    }

    // aToken
    preTokenLen = sizeof(preToken);
    memset(preToken, 0x00, preTokenLen);
    if (tzmon_xor((unsigned char *)iToken, 32, (unsigned char *)uToken, 32,
                    (unsigned char *)preToken, preTokenLen) != true) {
        LOGD("tzmon_xor error");
        return false;
    }
    printBuf("aPreToken", (unsigned char *)preToken, preTokenLen);

    outLen = sizeof(out);
    memset(out, 0x00, outLen);
    tzmon_itoa((unsigned char *)preToken, preTokenLen, arg, &argLen);

#ifdef SIM_MODE
    strcpy(cmd, "adb shell /vendor/bin/optee_tzmon ATOKEN ");
    strcat(cmd, arg);
    if (_call_tzmonTA(cmd, out, &outLen) != true) {
        LOGD("_call_tzmonTA error: ATOKEN");
        return false;
    }

    tzmon_atoi(out, outLen, aToken, &aTokenLen);
    printBuf("aToken", aToken, aTokenLen);
#else
    if (teec_tzmonTA(TA_TZMON_CMD_ATOKEN, (unsigned char *)arg,
                    (unsigned char *)out, (uint32_t *)&outLen) != TEEC_SUCCESS) {
        LOGD("teec_tzmonTA error: ATOKEN");
        return false;
    }

    aTokenLen = outLen;
    memcpy(aToken, out, aTokenLen);
#endif

    // cMsg
    tzmon_hmac_sha256(aToken, aTokenLen, resultMsg, 32, cMsg, &cMsgLen);
    printBuf("cMsg", cMsg, cMsgLen);

    outLen = sizeof(out);
    argLen = sizeof(arg);
    memset(out, 0x00, outLen);
    memset(arg, 0x00, argLen);
    tzmon_itoa(cMsg, cMsgLen, arg, &argLen);

#ifdef SIM_MODE
    strcpy(cmd, "adb shell /vendor/bin/optee_tzmon AVERIFY ");
    strcat(cmd, arg);
    if (_call_tzmonTA(cmd, out, &outLen) != true) {
        LOGD("_call_tzmonTA error: AVERIFY");
        return false;
    }
#else
    if (teec_tzmonTA(TA_TZMON_CMD_AVERIFY, (unsigned char *)arg,
                    (unsigned char *)out, (uint32_t *)&outLen) != TEEC_SUCCESS) {
        LOGD("teec_tzmonTA error: AVERIFY");
        return false;
    }
#endif

    return true;
}

static bool tzmonTimerSync(JNIEnv *env)
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

#ifdef SIM_MODE
        strcpy(cmd, "adb shell /vendor/bin/optee_tzmon TPRETOKEN ");
        strcat(cmd, tGap);
        if (_call_tzmonTA(cmd, out, &outLen) != true) {
            LOGD("_call_tzmonTA error: TPRETOKEN");
            return false;
        }
#else
        if (teec_tzmonTA(TA_TZMON_CMD_TPRETOKEN, (unsigned char *)tGap,
                        (unsigned char *)out, (uint32_t *)&outLen) != TEEC_SUCCESS) {
            LOGD("teec_tzmonTA error: TPRETOKEN");
            return false;
        }
#endif
    }

    // tToken
    tempLen = sizeof(temp);
    if (tzmon_xor((unsigned char *)iToken, 32, (unsigned char *)uToken, 32,
                    (unsigned char *)temp, tempLen) != true) {
        LOGD("tzmon_xor error");
        return false;
    }
    printBuf("temp", (unsigned char *)temp, tempLen);

    preTokenLen = sizeof(preToken);
    if (tzmon_xor((unsigned char *)temp, tempLen, (unsigned char *)aToken, 32,
                    (unsigned char *)preToken, preTokenLen) != true) {
        LOGD("tzmon_xor error");
        return false;
    }
    printBuf("preToken", (unsigned char *)preToken, preTokenLen);

    outLen = sizeof(out);
    memset(out, 0x00, outLen);
    tzmon_itoa((unsigned char *)preToken, preTokenLen, arg, &argLen);

#ifdef SIM_MODE
    strcpy(cmd, "adb shell /vendor/bin/optee_tzmon TTOKEN ");
    strcat(cmd, arg);
    if (_call_tzmonTA(cmd, out, &outLen) != true) {
        LOGD("_call_tzmonTA error: TTOKEN");
        return false;
    }

    tzmon_atoi(out, outLen, tToken, &tTokenLen);
    printBuf("tToken", tToken, tTokenLen);
#else
    if (teec_tzmonTA(TA_TZMON_CMD_TTOKEN, (unsigned char *)arg,
                    (unsigned char *)out, (uint32_t *)&outLen) != TEEC_SUCCESS) {
        LOGD("teec_tzmonTA error: TTOKEN");
        return false;
    }

    tTokenLen = outLen;
    memcpy(tToken, out, tTokenLen);
#endif

    // cMsg
    tzmon_hmac_sha256(tToken, tTokenLen, resultMsg, 32, cMsg, &cMsgLen);
    printBuf("cMsg", cMsg, cMsgLen);

    outLen = sizeof(out);
    argLen = sizeof(arg);
    memset(out, 0x00, outLen);
    memset(arg, 0x00, argLen);
    tzmon_itoa(cMsg, cMsgLen, arg, &argLen);

#ifdef SIM_MODE
    strcpy(cmd, "adb shell /vendor/bin/optee_tzmon TVERIFY ");
    strcat(cmd, arg);
    if (_call_tzmonTA(cmd, out, &outLen) != true) {
        LOGD("_call_tzmonTA error: TVERIFY");
        return false;
    }
#else
    if (teec_tzmonTA(TA_TZMON_CMD_TVERIFY, (unsigned char *)arg,
                    (unsigned char *)out, (uint32_t *)&outLen) != TEEC_SUCCESS) {
        LOGD("teec_tzmonTA error: TVERIFY");
        return false;
    }
#endif

    return true;
}

static bool tzmonHPreToken()
{
    char cmd[1024] = { 0x00, };
    char out[1024] = { 0x00, };
    char arg[1024] = { 0x00, };

    unsigned char temp1[32] = { 0x00, };
    unsigned char temp2[32] = { 0x00, };
    unsigned char preToken[32] = { 0x00, };

    int outLen, argLen, preTokenLen;

    if (tzmon_xor(iToken, 32, uToken, 32, temp1, 32) != true) {
        LOGD("tzmon_xor error");
        return false;
    }
    if (tzmon_xor(aToken, 32, tToken, 32, temp2, 32) != true) {
        LOGD("tzmon_xor error");
        return false;
    }
    if (tzmon_xor(temp1, 32, temp2, 32, preToken, 32) != true) {
        LOGD("tzmon_xor error");
        return false;
    }

    outLen = sizeof(out);
    memset(out, 0x00, outLen);
    tzmon_itoa((unsigned char *)preToken, 32, arg, &argLen);

#ifdef SIM_MODE
    strcpy(cmd, "adb shell /vendor/bin/optee_tzmon HPRETOKEN ");
    strcat(cmd, arg);
    if (_call_tzmonTA(cmd, out, &outLen) != true) {
        LOGD("_call_tzmonTA error: HPRETOKEN");
        return false;
    }
#else
    if (teec_tzmonTA(TA_TZMON_CMD_HPRETOKEN, (unsigned char *)arg,
                    (unsigned char *)out, (uint32_t *)&outLen) != TEEC_SUCCESS) {
        LOGD("teec_tzmonTA error: HPRETOKEN");
        return false;
    }
#endif

    return true;
}

static bool tzmonHKey(const char *nativeData, int *retVal)
{
    char cmd[1024] = { 0x00, };
    char out[1024] = { 0x00, };

    unsigned char hKey[32] = { 0x00, };

    int outLen, index, hKeyLen;

#ifdef SIM_MODE
    strcpy(cmd, "adb shell /vendor/bin/optee_tzmon HKEY ");
    strcat(cmd, nativeData);
    if (_call_tzmonTA(cmd, out, &outLen) != true) {
        LOGD("_call_tzmonTA error: HKEY");
        return false;
    }

    tzmon_atoi(out, outLen, hKey, &hKeyLen);
    printBuf("hKey", hKey, hKeyLen);
#else
    if (teec_tzmonTA(TA_TZMON_CMD_HKEY, (unsigned char *)nativeData,
                    (unsigned char *)out, (uint32_t *)&outLen) != TEEC_SUCCESS) {
        LOGD("teec_tzmonTA error: HKEY");
        return false;
    }

    hKeyLen = outLen;
    memcpy(hKey, out, hKeyLen);
#endif

    index = hKey[0] % hKeyLen;
    *retVal = hKey[index];
    LOGD("index: %d, hKey: 0x%x", index, *retVal);

    return true;
}

static bool tzmonHidingData(const char *nativeData, int *retVal)
{
    char cmd[1024] = { 0x00, };
    char out[1024] = { 0x00, };
    char arg[1024] = { 0x00, };

    unsigned char temp1[32] = { 0x00, };
    unsigned char temp2[32] = { 0x00, };
    unsigned char preToken[32] = { 0x00, };
    unsigned char hKey[32] = { 0x00, };

    int outLen, argLen, preTokenLen, index, hKeyLen;

    *retVal = 0x00;

    if (tzmon_xor(iToken, 32, uToken, 32, temp1, 32) != true) {
        LOGD("tzmon_xor error");
        return false;
    }
    if (tzmon_xor(aToken, 32, tToken, 32, temp2, 32) != true) {
        LOGD("tzmon_xor error");
        return false;
    }
    if (tzmon_xor(temp1, 32, temp2, 32, preToken, 32) != true) {
        LOGD("tzmon_xor error");
        return false;
    }

    outLen = sizeof(out);
    memset(out, 0x00, outLen);
    tzmon_itoa((unsigned char *)preToken, 32, arg, &argLen);

#ifdef SIM_MODE
    strcpy(cmd, "adb shell /vendor/bin/optee_tzmon HPRETOKEN ");
    strcat(cmd, arg);
    if (_call_tzmonTA(cmd, out, &outLen) != true) {
        LOGD("_call_tzmonTA error: HPRETOKEN");
        return false;
    }
#else
    if (teec_tzmonTA(TA_TZMON_CMD_HPRETOKEN, (unsigned char *)arg,
                    (unsigned char *)out, (uint32_t *)&outLen) != TEEC_SUCCESS) {
        LOGD("teec_tzmonTA error: HPRETOKEN");
        return false;
    }
#endif

    outLen = sizeof(out);
    memset(out, 0x00, outLen);

#ifdef SIM_MODE
    strcpy(cmd, "adb shell /vendor/bin/optee_tzmon HKEY ");
    strcat(cmd, nativeData);
    if (_call_tzmonTA(cmd, out, &outLen) != true) {
        LOGD("_call_tzmonTA error: HKEY");
        return false;
    }

    tzmon_atoi(out, outLen, hKey, &hKeyLen);
    printBuf("hKey", hKey, hKeyLen);
#else
    if (teec_tzmonTA(TA_TZMON_CMD_HKEY, (unsigned char *)nativeData,
                    (unsigned char *)out, (uint32_t *)&outLen) != TEEC_SUCCESS) {
        LOGD("teec_tzmonTA error: HKEY");
        return false;
    }

    hKeyLen = outLen;
    memcpy(hKey, out, outLen);
#endif

    index = preToken[0] % hKeyLen;
    *retVal = hKey[index];
    LOGD("index: %d, hKey: 0x%x", index, *retVal);

    return true;
}

JNIEXPORT jboolean JNICALL Java_com_uberspot_a2048_MainActivity_tzmonInitKeyNFlag
  (JNIEnv *env, jobject context)
{
    if (tzmonInit() != true) {
        LOGD("tzmonInit Error");
        return (bool)JNI_FALSE;
    }

    return (bool)JNI_TRUE;
}

JNIEXPORT jboolean JNICALL Java_com_uberspot_a2048_MainActivity_tzmonCheckAppHash
  (JNIEnv *env, jobject context)
{
    jboolean retVal = JNI_TRUE;

    char appHash[SHA256_DIGEST_LENGTH * 2 + 1] = { 0x00, };
    char path[1024] = { 0x00, };
    char name[256] = { 0x00, };

    getAppPath(env, context, path);
    LOGD("appPath: %s", path);

    getAppName(env, context, name);
    LOGD("appName: %s", name);

    generate_appHash(path, appHash);
    LOGD("pre_appHash: %s", appHash);

    if (isRunningApp(env, context, name) != true) {
        LOGD("isRunningApp error: this path is wrong!!!");
        retVal = JNI_FALSE;
        return (bool)retVal;
    }

    if (tzmonCheckAppIntegrity(appHash) != true) {
        LOGD("tzmonCheckAppIntegrity error: ");
        retVal = JNI_FALSE;
        return (bool)retVal;
    }

    return (bool)retVal;
}

JNIEXPORT jboolean JNICALL Java_com_uberspot_a2048_MainActivity_tzmonSecureUpdate
    (JNIEnv *env, jobject context)
{
    jboolean retVal = JNI_TRUE;

    if (tzmonSecureUpdate() != true) {
        LOGD("tzmonSecureUpdate error: ");
        retVal = JNI_FALSE;
    }

    return (bool)retVal;
}

JNIEXPORT jboolean JNICALL Java_com_uberspot_a2048_MainActivity_tzmonAbusingDetection
  (JNIEnv *env, jobject context)
{
    jboolean retVal = JNI_TRUE;

    if (tzmonAbusingDetection(env, context) != true) {
        LOGD("tzmonAbusingDetection error: ");
        retVal = JNI_FALSE;
    }

    return (bool)retVal;
}

JNIEXPORT jboolean JNICALL Java_com_uberspot_a2048_MainActivity_tzmonSyncTimer
  (JNIEnv * env, jobject context)
{
    jboolean retVal = JNI_TRUE;

    if (tzmonTimerSync(env) != true) {
        LOGD("tzmonTimerSync error: ");
        retVal = JNI_FALSE;
    }

    return (bool)retVal;
}

JNIEXPORT jboolean JNICALL Java_com_uberspot_a2048_MainActivity_tzmonHidingSetup
  (JNIEnv *env, jobject context)
{
    jboolean retVal = JNI_TRUE;

    if (tzmonHPreToken() != true) {
        LOGD("tzmonHPreToken error: ");
        retVal = JNI_FALSE;
    }

    return (bool)retVal;
}

JNIEXPORT jint JNICALL Java_com_uberspot_a2048_MainActivity_tzmonGetHKey
  (JNIEnv *env, jobject context, jstring data)
{
    int retVal = 0x00;

    const char *nativeData = env->GetStringUTFChars(data, 0x00);

    if (tzmonHKey(nativeData, &retVal) != true) {
        LOGD("tzmonHKey error");
        return retVal;
    }

    env->ReleaseStringUTFChars(data, nativeData);

    return retVal;
}
