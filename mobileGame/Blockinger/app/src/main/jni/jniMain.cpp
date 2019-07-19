//
// Created by HCR on 2019-05-15.
//
#include <android/log.h>
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include "openssl/sha.h"

#include "jniMain.h"

#define LOGV(...) __android_log_print(ANDROID_LOG_VERBOSE, "[LOGV]", __VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG  , "[LOGD]", __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO   , "[LOGI]", __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN   , "[LOGW]", __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR  , "[LOGE]", __VA_ARGS__)

int generate_hash()
{
    int retVal = 2, temp = 0;
    const int bufSize = 1024;

    unsigned char hash[SHA256_DIGEST_LENGTH] = { 0x00, };
    unsigned char input_data[bufSize] = { 0x01, };

    SHA256_CTX sha256;

    SHA256_Init(&sha256);
    SHA256_Update(&sha256, input_data, bufSize);
    SHA256_Final(hash, &sha256);

    retVal = (int)hash % 9;
    LOGD("HASH: %ld", hash);
    LOGD("retVal: %d", retVal);

    if (retVal < 0) {
        retVal *= -1;
    } else if (retVal == 0) {
        retVal = 1;
    }

    LOGD("start level: %d", retVal);

    return retVal;
}

JNIEXPORT jint JNICALL Java_org_blockinger2_game_components_GameState_jnireturnlevel(JNIEnv *env, jobject context)
{
    int hash = 0;
    hash = generate_hash();

#if 0
    // B=byte / C=char / D=double / F=float / I=int / J=long / S=short / V=void / Z=boolean
    jclass clazz = env->GetObjectClass(obj);
    jmethodID mid = env->GetMethodID(clazz, "SampleMethod", "(IZ)V");
    env->CallVoidMethod(obj, mid, n, b);
#endif

    return hash;
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
        sprintf(&hash_string[i*2], "%02X", (unsigned int)hash[i]);
    }

    LOGD("REAL_APP_HASH: %s", hash_string);
    memcpy(appHash, hash_string, sizeof(hash_string));
    fclose(fp);
}

JNIEXPORT jstring JNICALL Java_org_blockinger2_game_components_GameState_jniapphash(JNIEnv *env, jobject context, jstring appPath)
{
    char appHash[SHA256_DIGEST_LENGTH * 2 + 1] = { 0x00, };
    const char *path = env->GetStringUTFChars(appPath, 0x00);

    LOGD("path: %s", path);

    generate_appHash(path, appHash);

    LOGD("hash: %s", appHash);
    env->ReleaseStringUTFChars(appPath, path);

    return env->NewStringUTF(appHash);
}
