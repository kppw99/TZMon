//
// Created by HCR on 2019-05-15.
//
#include <android/log.h>    // for android log function
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <unistd.h>         // for socket
#include <sys/socket.h>     // for socket
#include <arpa/inet.h>      // for socket

#include "jniMain.h"

#define LOGV(...) __android_log_print(ANDROID_LOG_VERBOSE, "[LOGV]", __VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG  , "[LOGD]", __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO   , "[LOGI]", __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN   , "[LOGW]", __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR  , "[LOGE]", __VA_ARGS__)

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

JNIEXPORT jint JNICALL Java_org_blockinger2_game_components_GameState_jniHidingKey(JNIEnv *env, jobject context, jstring data)
{
    int retVal;

    char cmd[1024] = { 0x00, };
    char out[1024] = { 0x00, };

    unsigned char hKey[32] = { 0x00, };

    const char *nativeData = env->GetStringUTFChars(data, 0x00);

    int outLen, index, hKeyLen;

    strcpy(cmd, "adb shell /vendor/bin/optee_tzmon HKEY ");
    strcat(cmd, nativeData);
    if (_call_tzmonTA(cmd, out, &outLen) != 0) {
        LOGD("_call_tzmonTA error: ");
        return 1;
    }

    tzmon_atoi(out, outLen, hKey, &hKeyLen);
    printBuf("hKey", hKey, hKeyLen);

    index = hKey[0] % hKeyLen;
    retVal = hKey[index];
    LOGD("index: %d, hKey: 0x%x", index, retVal);

    env->ReleaseStringUTFChars(data, nativeData);

    return retVal;
}
