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

#include "optee/tee_client_api.h"
#include "tzmonTEEC.h"
#include "tzmonUtil.h"

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

static TEEC_Result _parseCmd(uint32_t tzmonCmd, uint8_t *param)
{
    TEEC_Result retVal = TEEC_SUCCESS;

    if (param == NULL) return TEEC_ERROR_BAD_PARAMETERS;

    switch (tzmonCmd) {
        case TA_TZMON_CMD_INIT_FLAG:
            sharedMem.inDataLen = strlen((const char *)param);
            memcpy(sharedMem.inData, param, sharedMem.inDataLen);
            break;
        case TA_TZMON_CMD_IKEY:
            sharedMem.inDataLen = strlen((const char *)param);
            memcpy(sharedMem.inData, param, sharedMem.inDataLen);
            break;
        case TA_TZMON_CMD_ITOKEN:
            tzmon_atoi(param, strlen((const char *)param), sharedMem.inData, &sharedMem.inDataLen);
            break;
        case TA_TZMON_CMD_IVERIFY:
            tzmon_atoi(param, strlen((const char *)param), sharedMem.inData, &sharedMem.inDataLen);
            break;
        case TA_TZMON_CMD_UKEY:
            tzmon_atoi(param, strlen((const char *)param), sharedMem.inData, &sharedMem.inDataLen);
            break;
        case TA_TZMON_CMD_UTOKEN:
            tzmon_atoi(param, strlen((const char *)param), sharedMem.inData, &sharedMem.inDataLen);
            sharedMem.outDataLen = DATA_SIZE;
            break;
        case TA_TZMON_CMD_UVERIFY:
            tzmon_atoi(param, strlen((const char *)param), sharedMem.inData, &sharedMem.inDataLen);
            break;
        case TA_TZMON_CMD_APRETOKEN:
            sharedMem.inDataLen = strlen((const char *)param);
            memcpy(sharedMem.inData, param, sharedMem.inDataLen);
            break;
        case TA_TZMON_CMD_ATOKEN:
            tzmon_atoi(param, strlen((const char *)param), sharedMem.inData, &sharedMem.inDataLen);
            break;
        case TA_TZMON_CMD_AVERIFY:
            tzmon_atoi(param, strlen((const char *)param), sharedMem.inData, &sharedMem.inDataLen);
            break;
        case TA_TZMON_CMD_TPRETOKEN:
            sharedMem.tGap = atof((const char *)param);
            break;
        case TA_TZMON_CMD_TTOKEN:
            tzmon_atoi(param, strlen((const char *)param), sharedMem.inData, &sharedMem.inDataLen);
            break;
        case TA_TZMON_CMD_TVERIFY:
            tzmon_atoi(param, strlen((const char *)param), sharedMem.inData, &sharedMem.inDataLen);
            break;
        case TA_TZMON_CMD_HPRETOKEN:
            tzmon_atoi(param, strlen((const char *)param), sharedMem.inData, &sharedMem.inDataLen);
            break;
        case TA_TZMON_CMD_HKEY:
            sharedMem.inDataLen = strlen((const char *)param);
            memcpy(sharedMem.inData, param, sharedMem.inDataLen);
            break;
        default:
            retVal = TEEC_ERROR_BAD_PARAMETERS;
            break;
    }

    return retVal;
}

static TEEC_Result teecInit(TEEC_Context *ctx, TEEC_Session *session, TEEC_UUID *uuid)
{
    TEEC_Result retVal = TEEC_SUCCESS;
    uint32_t err;

    if (ctx == NULL || session == NULL || uuid == NULL) {
        LOGD("Parameters Error");
        return TEEC_ERROR_BAD_PARAMETERS;
    }

    retVal = TEEC_InitializeContext(NULL, ctx);
    if (retVal != TEEC_SUCCESS) {
        LOGD("TEEC_InitializeContext failed with code 0x%x", retVal);
        return retVal;
    }

    retVal = TEEC_OpenSession(ctx, session, uuid, TEEC_LOGIN_PUBLIC, NULL, NULL, &err);
    if (retVal != TEEC_SUCCESS) {
        LOGD("TEEC_Opensession failed with code 0x%x origin 0x%x", retVal, err);
        TEEC_FinalizeContext(ctx);
        return retVal;
    }

    return retVal;
}

static void teecFinish(TEEC_Context *ctx, TEEC_Session *session)
{
    if (session != NULL)   TEEC_CloseSession(session);
    if (ctx != NULL)       TEEC_FinalizeContext(ctx);
}

static TEEC_Result teecInvoke(TEEC_Session *session, TEEC_Operation *op, uint32_t tzmonCmd,
    uint8_t *param)
{
    TEEC_Result retVal = TEEC_SUCCESS;
    uint32_t err;

    if (session == NULL || op == NULL) {
        LOGD("Parameters Error");
        return TEEC_ERROR_BAD_PARAMETERS;
    }

    op->paramTypes = TEEC_PARAM_TYPES(TEEC_MEMREF_TEMP_INOUT, TEEC_VALUE_INOUT,
                                      TEEC_NONE, TEEC_NONE);
    op->params[0].tmpref.buffer = &sharedMem;
    op->params[0].tmpref.size = sizeof(sharedMem);

    retVal = _parseCmd(tzmonCmd, param);
    if (retVal != TEEC_SUCCESS) {
        LOGD("parseCMD failed with code 0x%x", retVal);
        return retVal;
    }

    retVal = TEEC_InvokeCommand(session, tzmonCmd, op, &err);
    if (retVal != TEEC_SUCCESS) {
        LOGD("TEEC_InvokeCommand failed with code 0x%x origin 0x%x", retVal, err);
        return retVal;
    }

    return retVal;
}

TEEC_Result teec_tzmonTA(uint32_t tzmonCmd, uint8_t *param, uint8_t *out, uint32_t *outLen)
{
    TEEC_Context ctx;
    TEEC_Session session;
    TEEC_Operation op;
    TEEC_UUID uuid = TA_TZMON_UUID;
    TEEC_Result retVal = TEEC_SUCCESS;

    if (out == NULL || outLen == NULL) {
        LOGD("Parameters Error");
        return TEEC_ERROR_BAD_PARAMETERS;
    }

    retVal = teecInit(&ctx, &session, &uuid);
    if (retVal != TEEC_SUCCESS) {
        LOGD("tzmonInit failed with code 0x%x", retVal);
        return retVal;
    }

    memset(&op, 0x00, sizeof(op));
    memset(&sharedMem, 0x00, sizeof(sharedMem));

    retVal = teecInvoke(&session, &op, tzmonCmd, param);
    if (retVal != TEEC_SUCCESS) {
        LOGD("tzmonInvoke failed with code 0x%x", retVal);
        return retVal;
    }

    *outLen = sharedMem.outDataLen;
    memcpy(out, sharedMem.outData, *outLen);

    teecFinish(&ctx, &session);

    return retVal;
}

