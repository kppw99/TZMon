//
// Created by HCR on 2019-07-23.
//

#include <err.h>
#include <stdio.h>
#include <string.h>
#include <stdlib.h>

#include <sys/types.h>
#include <sys/stat.h>
#include <sys/socket.h>
#include <sys/ioctl.h>
#include <sys/utsname.h>
#include <unistd.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <net/if.h>

#include "tzmonTEEC.h"
#include "tzmonUtil.h"

/* OP-TEE TEE client API (built by optee_client) */
#include <optee/tee_client_api.h>

static SharedMem sharedMem;

static void tzmon_atoi(uint8_t *src, uint32_t srcLen, uint8_t *dest, uint32_t *destLen)
{
    uint32_t hex, i;
    uint8_t tmp[3] = { 0x00, };

    if (src == NULL || dest == NULL || destLen == NULL) return;

    *destLen = srcLen / 2;
    for (i = 0; i < *destLen; i++) {
        memset(tmp, 0x00, sizeof(tmp));
        memcpy(tmp, src + i * 2, 2);

        if (tmp[0] >= '0' && tmp[0] <= '9') {
            if (tmp[1] >= '0' && tmp[1] <= '9') {
                hex = atoi((const char *)tmp);
                dest[i] = ((hex / 10) * 16) + (hex % 10);
            } else {
                dest[i] = atoi((const char *)&tmp[0]) * 16;
                dest[i] += tmp[1] - 87;
            }
        } else {
            dest[i] = (tmp[0] - 87) * 16;
            if (tmp[1] >= '0' && tmp[1] <= '9') {
                dest[i] += atoi((const char *)&tmp[1]);
            } else {
                dest[i] += tmp[1] - 87;
            }
        }
    }
}

static TEEC_Result tzmon_abusing_write(uint8_t *file, uint8_t *out, uint32_t *outLen)
{
    FILE *fp = NULL;

    if (file == NULL || out == NULL || outLen == NULL) {
        return TEEC_ERROR_BAD_PARAMETERS;
    }

    fp = fopen((const char *)file, "r");
    if (fp == NULL) {
        LOGD("fopen error: \n");
        return TEEC_ERROR_GENERIC;
    }

    fseek(fp, 0, SEEK_END);
    *outLen = ftell(fp);
    fseek(fp, 0, SEEK_SET);

    if (fread(out, *outLen, 1, fp) == 0x00) {
        fclose(fp);
        return TEEC_ERROR_GENERIC;
    }

    if (fp != NULL) {
        fclose(fp);
    }

    return TEEC_SUCCESS;
}

static void printBuf(uint8_t *title, uint8_t *buf, uint32_t bufLen)
{
    uint32_t i;

    if (bufLen == 0 || buf == NULL || title == NULL)    return;

    LOGD("%s", title);
#if 0
    for (i = 0; i < bufLen; i++) {
        if (i != 0 && i % 8 == 0)   LOGN("\n");
        LOGN("%02x ", buf[i]);
    }

    LOGN("\n\n");
#endif
}

static TEEC_Result _tzmonCmd(char *cmd, uint32_t cmdLen, uint32_t *tzmonCMD)
{
    if (cmdLen == 0 || cmd == NULL) {
        return TEEC_ERROR_BAD_PARAMETERS;
    }

    if ((memcmp(cmd, "IKEY", cmdLen) == 0) && (cmdLen == strlen("IKEY"))) {
        *tzmonCMD = TA_TZMON_CMD_IKEY;
    } else if ( (memcmp(cmd, "INIT_FLAG", cmdLen) == 0) && (cmdLen == strlen("INIT_FLAG"))) {
        *tzmonCMD = TA_TZMON_CMD_INIT_FLAG;
    } else if ( (memcmp(cmd, "UKEY", cmdLen) == 0) && (cmdLen == strlen("UKEY"))) {
        *tzmonCMD = TA_TZMON_CMD_UKEY;
    } else if ( (memcmp(cmd, "HKEY", cmdLen) == 0) && (cmdLen == strlen("HKEY"))) {
        *tzmonCMD = TA_TZMON_CMD_HKEY;
    } else if ( (memcmp(cmd, "APRETOKEN", cmdLen) == 0) && (cmdLen == strlen("APRETOKEN"))) {
        *tzmonCMD = TA_TZMON_CMD_APRETOKEN;
    } else if ( (memcmp(cmd, "TPRETOKEN", cmdLen) == 0) && (cmdLen == strlen("TPRETOKEN"))) {
        *tzmonCMD = TA_TZMON_CMD_TPRETOKEN;
    } else if ( (memcmp(cmd, "HPRETOKEN", cmdLen) == 0) && (cmdLen == strlen("HPRETOKEN"))) {
        *tzmonCMD = TA_TZMON_CMD_HPRETOKEN;
    } else if ( (memcmp(cmd, "ITOKEN", cmdLen) == 0) && (cmdLen == strlen("ITOKEN"))) {
        *tzmonCMD = TA_TZMON_CMD_ITOKEN;
    } else if ( (memcmp(cmd, "UTOKEN", cmdLen) == 0) && (cmdLen == strlen("UTOKEN"))) {
        *tzmonCMD = TA_TZMON_CMD_UTOKEN;
    } else if ( (memcmp(cmd, "ATOKEN", cmdLen) == 0) && (cmdLen == strlen("ATOKEN"))) {
        *tzmonCMD = TA_TZMON_CMD_ATOKEN;
    } else if ( (memcmp(cmd, "TTOKEN", cmdLen) == 0) && (cmdLen == strlen("TTOKEN"))) {
        *tzmonCMD = TA_TZMON_CMD_TTOKEN;
    } else if ( (memcmp(cmd, "IVERIFY", cmdLen) == 0) && (cmdLen == strlen("IVERIFY"))) {
        *tzmonCMD = TA_TZMON_CMD_IVERIFY;
    } else if ( (memcmp(cmd, "UVERIFY", cmdLen) == 0) && (cmdLen == strlen("UVERIFY"))) {
        *tzmonCMD = TA_TZMON_CMD_UVERIFY;
    } else if ( (memcmp(cmd, "AVERIFY", cmdLen) == 0) && (cmdLen == strlen("AVERIFY"))) {
        *tzmonCMD = TA_TZMON_CMD_AVERIFY;
    } else if ( (memcmp(cmd, "TVERIFY", cmdLen) == 0) && (cmdLen == strlen("TVERIFY"))) {
        *tzmonCMD = TA_TZMON_CMD_TVERIFY;
    } else {
        LOGD("There is no tzmonCMD.");
        return TEEC_ERROR_BAD_PARAMETERS;
    }

    LOGD("tzmonCMD is %s", cmd);

    return TEEC_SUCCESS;
}

static TEEC_Result parseCMD(int argc, char **argv, uint32_t *tzmonCMD)
{
    TEEC_Result retVal = TEEC_SUCCESS;

    if (argc < 3) {
        LOGD("argument count is less.");
        return TEEC_ERROR_BAD_PARAMETERS;
    }

    if (argv[1] == NULL || argv[2] == NULL) {
        LOGD("argument count is less.");
        return TEEC_ERROR_BAD_PARAMETERS;
    }

    retVal = _tzmonCmd(argv[1], strlen(argv[1]), tzmonCMD);
    if (retVal != TEEC_SUCCESS) {
        return TEEC_ERROR_BAD_PARAMETERS;
    }

    memset(&sharedMem, 0x00, sizeof(sharedMem));
    switch (*tzmonCMD) {
        case TA_TZMON_CMD_INIT_FLAG:
            sharedMem.inDataLen = strlen(argv[2]);
            memcpy(sharedMem.inData, argv[2], sharedMem.inDataLen);
            LOGD("%s(%d)\n", sharedMem.inData, sharedMem.inDataLen);
            break;
        case TA_TZMON_CMD_IKEY:
            sharedMem.inDataLen = strlen(argv[2]);
            memcpy(sharedMem.inData, argv[2], sharedMem.inDataLen);
            break;
        case TA_TZMON_CMD_ITOKEN:
            tzmon_atoi((unsigned char *)argv[2], strlen(argv[2]),
                  sharedMem.inData, &sharedMem.inDataLen);
            break;
        case TA_TZMON_CMD_IVERIFY:
            tzmon_atoi((unsigned char *)argv[2], strlen(argv[2]),
                  sharedMem.inData, &sharedMem.inDataLen);
            break;
        case TA_TZMON_CMD_UKEY:
            tzmon_atoi((unsigned char *)argv[2], strlen(argv[2]),
                  sharedMem.inData, &sharedMem.inDataLen);
            break;
        case TA_TZMON_CMD_UTOKEN:
            tzmon_atoi((unsigned char *)argv[2], strlen(argv[2]),
                  sharedMem.inData, &sharedMem.inDataLen);
            sharedMem.outDataLen = DATA_SIZE;
            break;
        case TA_TZMON_CMD_UVERIFY:
            tzmon_atoi((unsigned char *)argv[2], strlen(argv[2]),
                  sharedMem.inData, &sharedMem.inDataLen);
            break;
        case TA_TZMON_CMD_APRETOKEN:
            sharedMem.inDataLen = strlen(argv[2]);
            memcpy(sharedMem.inData, argv[2], sharedMem.inDataLen);
            break;
        case TA_TZMON_CMD_ATOKEN:
            tzmon_atoi((unsigned char *)argv[2], strlen(argv[2]),
                  sharedMem.inData, &sharedMem.inDataLen);
            break;
        case TA_TZMON_CMD_AVERIFY:
            tzmon_atoi((unsigned char *)argv[2], strlen(argv[2]),
                  sharedMem.inData, &sharedMem.inDataLen);
            break;
        case TA_TZMON_CMD_TPRETOKEN:
            sharedMem.tGap = atof(argv[2]);
            break;
        case TA_TZMON_CMD_TTOKEN:
            tzmon_atoi((unsigned char *)argv[2], strlen(argv[2]),
                  sharedMem.inData, &sharedMem.inDataLen);
            break;
        case TA_TZMON_CMD_TVERIFY:
            tzmon_atoi((unsigned char *)argv[2], strlen(argv[2]),
                  sharedMem.inData, &sharedMem.inDataLen);
            break;
        case TA_TZMON_CMD_HPRETOKEN:
            tzmon_atoi((unsigned char *)argv[2], strlen(argv[2]),
                  sharedMem.inData, &sharedMem.inDataLen);
            break;
        case TA_TZMON_CMD_HKEY:
            sharedMem.inDataLen = strlen(argv[2]);
            memcpy(sharedMem.inData, argv[2], sharedMem.inDataLen);
            break;
        default:
            retVal = TEEC_ERROR_BAD_PARAMETERS;
            break;
    }

  return retVal;
}

TEEC_Result tzmonMain()
{
    TEEC_Result res;
    TEEC_Context ctx;
    TEEC_Session sess;
    TEEC_Operation op;
    TEEC_UUID uuid = TA_TZMON_UUID;

    FILE *fp = NULL;

    char result[1024] = { 0x00, };
    uint32_t err_origin, tzmonCMD, resultLen, ii;

    /* Initialize a context connecting us to the TEE */
    res = TEEC_InitializeContext(NULL, &ctx);
    if (res != TEEC_SUCCESS) {
        errx(1, "TEEC_InitializeContext failed with code 0x%x", res);
    }

    /*
     * Open a session to the "hello world" TA, the TA will print "hello
     * world!" in the log when the session is created.
     */
    res = TEEC_OpenSession(&ctx, &sess, &uuid,
                   TEEC_LOGIN_PUBLIC, NULL, NULL, &err_origin);
    if (res != TEEC_SUCCESS) {
        errx(1, "TEEC_Opensession failed with code 0x%x origin 0x%x",
            res, err_origin);
    }

    /*
     * Execute a function in the TA by invoking it, in this case
     * we're incrementing a number.
     *
     * The value of command ID part and how the parameters are
     * interpreted is part of the interface provided by the TA.
     */

    /* Clear the TEEC_Operation struct */
    memset(&op, 0, sizeof(op));

   /*
     * Prepare the argument. Pass a value in the first parameter,
     * the remaining three parameters are unused.
     */
    op.paramTypes = TEEC_PARAM_TYPES(TEEC_MEMREF_TEMP_INOUT,
            TEEC_VALUE_INOUT, TEEC_NONE, TEEC_NONE);

    op.params[0].tmpref.buffer = &sharedMem;
    op.params[0].tmpref.size = sizeof(sharedMem);
    //op.params[1].value.a = 42;

    /*
     * TA_TZMON_CMD_IKEY is the actual function in the TA to be
     * called.
     */
    res = TEEC_InvokeCommand(&sess, tzmonCMD, &op, &err_origin);
    if (res != TEEC_SUCCESS) {
        errx(1, "TEEC_InvokeCommand failed with code 0x%x origin 0x%x",
            res, err_origin);
    }

    /*
     * We're done with the TA, close the session and
     * destroy the context.
     *
     * The TA will print "Goodbye!" in the log when the
     * session is closed.
     */
exit:
    TEEC_CloseSession(&sess);
    TEEC_FinalizeContext(&ctx);

    return TEEC_SUCCESS;
}
