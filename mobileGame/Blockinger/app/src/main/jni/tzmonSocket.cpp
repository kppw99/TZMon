#include <android/log.h>    // for android log function
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <unistd.h>         // for socket
#include <sys/socket.h>     // for socket
#include <arpa/inet.h>      // for socket

#include "tzmonSocket.h"

#define LOGV(...) __android_log_print(ANDROID_LOG_VERBOSE, "[LOGV]", __VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG  , "[LOGD]", __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO   , "[LOGI]", __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN   , "[LOGW]", __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR  , "[LOGE]", __VA_ARGS__)

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

bool _call_tzmonTA(char *cmd, char *out, int *outLen)
{
    int sockFD, port = 9999;
    char *ip = "163.152.127.108";
    struct sockaddr_in sockAddr;

    if (cmd == NULL || out == NULL || outLen == NULL) {
        LOGD("Bad Parameter");
        return false;
    }

    sockFD = socket(AF_INET, SOCK_STREAM, 0);
    sockAddr.sin_family = AF_INET;
    sockAddr.sin_addr.s_addr = inet_addr(ip);
    sockAddr.sin_port = htons(port);

    if (connect(sockFD, (struct sockaddr *)&sockAddr, sizeof(sockAddr)) < 0) {
        LOGD("Connect error: ");
        return false;
    }

    if (socketWrite(sockFD, cmd, (int)strlen(cmd)) <= 0) {
        LOGD("Write error: ");
        return false;
    }

    if (socketRead(sockFD, out, outLen) <= 0) {
        LOGD("Read error: ");
        return false;
    }

    closeSocket(sockFD);

    LOGD("[Command] %s", cmd);
    LOGD("[Result] %s(%d)", out, *outLen);
    if (strcmp(out, "fail") == 0) {
        return false;
    } else {
        return true;
    }
}

bool _call_tzmonAbusingDetection(const char *appName)
{
    int sockFD, port = 9999;
    char *ip = "163.152.127.108";
    struct sockaddr_in sockAddr;

    char cmd[1024] = { 0x00, };
    char out[1024] = { 0x00, };
    int outLen = sizeof(out);

    if (appName == NULL) {
        LOGD("Bad Parameter");
        return false;
    }

    sockFD = socket(AF_INET, SOCK_STREAM, 0);
    sockAddr.sin_family = AF_INET;
    sockAddr.sin_addr.s_addr = inet_addr(ip);
    sockAddr.sin_port = htons(port);

    if (connect(sockFD, (struct sockaddr *)&sockAddr, sizeof(sockAddr)) < 0) {
        LOGD("Connect error: ");
        return false;
    }

    strcpy(cmd, "adb shell /vendor/bin/optee_tzmon APRETOKEN ");
    strcat(cmd, appName);
    if (socketWrite(sockFD, cmd, (int)strlen(cmd)) <= 0) {
        LOGD("Write error: ");
        return false;
    }

    if (socketRead(sockFD, out, &outLen) <= 0) {
        LOGD("Read error: ");
        return false;
    }

    closeSocket(sockFD);

    LOGD("[Command] %s", cmd);
    LOGD("[Result] %s(%d)", out, outLen);
    if (strcmp(out, "fail") == 0) {
        return false;
    } else {
        return true;
    }
}
