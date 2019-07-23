#include <android/log.h>    // for android log function
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <unistd.h>         // for socket
#include <sys/socket.h>     // for socket
#include <arpa/inet.h>      // for socket

#include "tzmonUtil.h"
#include "tzmonCrypto.h"
#include "tzmonSocket.h"
#include "tzmonLightMain.h"

static bool tzmonHKey(const char *nativeData, int *retVal)
{
    char cmd[1024] = { 0x00, };
    char out[1024] = { 0x00, };

    unsigned char hKey[32] = { 0x00, };

    int outLen, index, hKeyLen;

    strcpy(cmd, "adb shell /vendor/bin/optee_tzmon HKEY ");
    strcat(cmd, nativeData);
    if (_call_tzmonTA(cmd, out, &outLen) != true) {
        LOGD("_call_tzmonTA error: HKEY");
        return false;
    }

    tzmon_atoi(out, outLen, hKey, &hKeyLen);
    printBuf("hKey", hKey, hKeyLen);

    index = hKey[0] % hKeyLen;
    *retVal = hKey[index];
    LOGD("index: %d, hKey: 0x%x", index, *retVal);

    return true;
}

JNIEXPORT jint JNICALL Java_org_blockinger2_game_components_GameState_tzmonGetHKey
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
