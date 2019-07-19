/*
 * Copyright (c) 2016, Linaro Limited
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

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

/* OP-TEE TEE client API (built by optee_client) */
#include <tee_client_api.h>

/* To the the UUID (found the the TA's h-file(s)) */
#include <tzmon_ta.h>

#include "testInput.h"

#define RESULT	"/vendor/bin/result.txt"

static SharedMem sharedMem;

static int socketWrite(int sockFD, char *msg, int msgLen)
{
    char outBuf[255] = { 0x00, };
    int retVal = -1;

	if (msg == NULL || msgLen == 0) {
		printf("Bad Parameter\n");
		return retVal;
	}

    memcpy(outBuf, msg, msgLen);
    retVal = write(sockFD, outBuf, msgLen);

    return retVal;
}

static int socketRead(int sockFD, char *buf, int *bufLen)
{
    int retVal = -1;

    if (buf == NULL || bufLen == NULL) {
        printf("Bad Parameter\n");
		return retVal;
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

static TEEC_Result _connectServer(uint8_t *in, uint32_t inLen, uint8_t *out, uint32_t *outLen)
{
    int sockFD, port = 9999;
    char *ip = "192.168.0.143";
    struct sockaddr_in sockAddr;
	char inBuf[64] = { 0x00, };
	int inBufLen;

    if (inLen == 0 || in == NULL || out == NULL || outLen == NULL) {
        printf("Bad Parameter\n");
        return TEEC_ERROR_BAD_PARAMETERS;
    }

    sockFD = socket(AF_INET, SOCK_STREAM, 0);
    sockAddr.sin_family = AF_INET;
    sockAddr.sin_addr.s_addr = inet_addr(ip);
    sockAddr.sin_port = htons(port);

    if (connect(sockFD, (struct sockaddr *)&sockAddr, sizeof(sockAddr)) < 0) {
        printf("Connect error: \n");
        return TEEC_ERROR_GENERIC;
    }

	inBuf[0] = 0x03;
	inBuf[1] = inLen;
	memcpy(inBuf + 2, in, inLen);
	inBufLen = inLen + 2;
	if (socketWrite(sockFD, inBuf, inBufLen) <= 0) {
		printf("Write error: \n");
		closeSocket(sockFD);
		return TEEC_ERROR_GENERIC;
	}

	if (socketRead(sockFD, (char *)out, (int *)outLen) <= 0) {
		printf("Read error: \n");
		closeSocket(sockFD);
		return TEEC_ERROR_GENERIC;
	}

	if (out[0] != 0x01) {
		closeSocket(sockFD);
		return TEEC_ERROR_GENERIC;
	}

    closeSocket(sockFD);

    return TEEC_SUCCESS;
}

static void tzmon_atoi(uint8_t *src, uint32_t srcLen,
					uint8_t *dest, uint32_t *destLen)
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

//tzmon_abusing_write((unsigned char *)argv[2], strlen(argv[2]),
//		sharedMem.inData, &sharedMem.inDataLen);

static TEEC_Result tzmon_abusing_write(uint8_t *file, uint8_t *out, uint32_t *outLen)
{
	FILE *fp = NULL;

	if (file == NULL || out == NULL || outLen == NULL) {
		return TEEC_ERROR_BAD_PARAMETERS;
	}

	fp = fopen((const char *)file, "r");
	if (fp == NULL) {
		printf("fopen error: \n");
		return TEEC_ERROR_GENERIC;
	}
	
	fseek(fp, 0, SEEK_END);
    *outLen = ftell(fp);
    fseek(fp, 0, SEEK_SET);

    if (fread(out, *outLen, 1, fp) == 0x00) {
		fclose(fp);
		return TEEC_ERROR_GENERIC;
	}

	if (fp != NULL)	{
		fclose(fp);
	}

	return TEEC_SUCCESS;
}

static void printBuf(uint8_t *title, uint8_t *buf, uint32_t bufLen)
{
	uint32_t i;

	if (bufLen == 0 || buf == NULL)	return;

	printf("[%s]:\n", title);

	for (i = 0; i < bufLen; i++) {
		if (i != 0 && i % 8 == 0)	printf("\n");
		printf("%02x ", buf[i]);
	}

	printf("\n");
	
}

static TEEC_Result _tzmonCmd(char *cmd, uint32_t cmdLen,
		uint32_t *tzmonCMD)
{
	if (cmdLen == 0 || cmd == NULL) {
		return TEEC_ERROR_BAD_PARAMETERS;
	}

	if ((memcmp(cmd, "IKEY", cmdLen) == 0) &&
		(cmdLen == strlen("IKEY"))) {
		*tzmonCMD = TA_TZMON_CMD_IKEY;
	} else if (	(memcmp(cmd, "INIT_FLAG", cmdLen) == 0) &&
				(cmdLen == strlen("INIT_FLAG"))) {
		*tzmonCMD = TA_TZMON_CMD_INIT_FLAG;
	} else if (	(memcmp(cmd, "UKEY", cmdLen) == 0) &&
				(cmdLen == strlen("UKEY"))) {
		*tzmonCMD = TA_TZMON_CMD_UKEY;
	} else if (	(memcmp(cmd, "APRETOKEN", cmdLen) == 0) &&
				(cmdLen == strlen("APRETOKEN"))) {
		*tzmonCMD = TA_TZMON_CMD_APRETOKEN;
	} else if (	(memcmp(cmd, "TPRETOKEN", cmdLen) == 0) &&
				(cmdLen == strlen("TPRETOKEN"))) {
		*tzmonCMD = TA_TZMON_CMD_TPRETOKEN;
	} else if (	(memcmp(cmd, "ITOKEN", cmdLen) == 0) &&
				(cmdLen == strlen("ITOKEN"))) {
		*tzmonCMD = TA_TZMON_CMD_ITOKEN;
	} else if (	(memcmp(cmd, "UTOKEN", cmdLen) == 0) &&
				(cmdLen == strlen("UTOKEN"))) {
		*tzmonCMD = TA_TZMON_CMD_UTOKEN;
	} else if (	(memcmp(cmd, "ATOKEN", cmdLen) == 0) &&
				(cmdLen == strlen("ATOKEN"))) {
		*tzmonCMD = TA_TZMON_CMD_ATOKEN;
	} else if (	(memcmp(cmd, "TTOKEN", cmdLen) == 0) &&
				(cmdLen == strlen("TTOKEN"))) {
		*tzmonCMD = TA_TZMON_CMD_TTOKEN;
	} else if (	(memcmp(cmd, "IVERIFY", cmdLen) == 0) &&
				(cmdLen == strlen("IVERIFY"))) {
		*tzmonCMD = TA_TZMON_CMD_IVERIFY;
	} else if (	(memcmp(cmd, "UVERIFY", cmdLen) == 0) &&
				(cmdLen == strlen("UVERIFY"))) {
		*tzmonCMD = TA_TZMON_CMD_UVERIFY;
	} else if (	(memcmp(cmd, "AVERIFY", cmdLen) == 0) &&
				(cmdLen == strlen("AVERIFY"))) {
		*tzmonCMD = TA_TZMON_CMD_AVERIFY;
	} else if (	(memcmp(cmd, "TVERIFY", cmdLen) == 0) &&
				(cmdLen == strlen("TVERIFY"))) {
		*tzmonCMD = TA_TZMON_CMD_TVERIFY;
	} else if (	(memcmp(cmd, "SHA256", cmdLen) == 0) &&
				(cmdLen == strlen("SHA256"))) {
		*tzmonCMD = TA_TZMON_CMD_SHA256;
	} else if (	(memcmp(cmd, "HMAC_SHA256", cmdLen) == 0) &&
				(cmdLen == strlen("HMAC_SHA256"))) {
		*tzmonCMD = TA_TZMON_CMD_HMAC_SHA256;
	} else if (	(memcmp(cmd, "RANDOM", cmdLen) == 0) &&
				(cmdLen == strlen("RANDOM"))) {
		*tzmonCMD = TA_TZMON_CMD_RANDOM;
	} else if (	(memcmp(cmd, "AES256_ENC", cmdLen) == 0) &&
				(cmdLen == strlen("AES256_ENC"))) {
		*tzmonCMD = TA_TZMON_CMD_AES256_ENC;
	} else if (	(memcmp(cmd, "AES256_DEC", cmdLen) == 0) &&
				(cmdLen == strlen("AES256_DEC"))) {
		*tzmonCMD = TA_TZMON_CMD_AES256_DEC;
	} else if (	(memcmp(cmd, "KDF", cmdLen) == 0) &&
				(cmdLen == strlen("KDF"))) {
		*tzmonCMD = TA_TZMON_CMD_KDF;
	} else if (	(memcmp(cmd, "FILE_WRITE", cmdLen) == 0) &&
				(cmdLen == strlen("FILE_WRITE"))) {
		*tzmonCMD = TA_TZMON_CMD_FILE_WRITE;
	} else if (	(memcmp(cmd, "FILE_READ", cmdLen) == 0) &&
				(cmdLen == strlen("FILE_READ"))) {
		*tzmonCMD = TA_TZMON_CMD_FILE_READ;
	} else if (	(memcmp(cmd, "FILE_DELETE", cmdLen) == 0) &&
				(cmdLen == strlen("FILE_DELETE"))) {
		*tzmonCMD = TA_TZMON_CMD_FILE_DELETE;
	} else if (	(memcmp(cmd, "ADMIN_CMD_MKEY_WRITE", cmdLen) == 0) &&
				(cmdLen == strlen("ADMIN_CMD_MKEY_WRITE"))) {
		*tzmonCMD = TA_TZMON_ADMIN_CMD_MKEY_WRITE;
	} else if (	(memcmp(cmd, "ADMIN_CMD_MKEY_READ", cmdLen) == 0) &&
				(cmdLen == strlen("ADMIN_CMD_MKEY_READ"))) {
		*tzmonCMD = TA_TZMON_ADMIN_CMD_MKEY_READ;
	} else if (	(memcmp(cmd, "ADMIN_CMD_APPPREHASH_WRITE", cmdLen) == 0) &&
				(cmdLen == strlen("ADMIN_CMD_APPPREHASH_WRITE"))) {
		*tzmonCMD = TA_TZMON_ADMIN_CMD_APPPREHASH_WRITE;
	} else if (	(memcmp(cmd, "ADMIN_CMD_APPPREHASH_READ", cmdLen) == 0) &&
				(cmdLen == strlen("ADMIN_CMD_APPPREHASH_READ"))) {
		*tzmonCMD = TA_TZMON_ADMIN_CMD_APPPREHASH_READ;
	} else if (	(memcmp(cmd, "ADMIN_CMD_ABUSING_WRITE", cmdLen) == 0) &&
				(cmdLen == strlen("ADMIN_CMD_ABUSING_WRITE"))) {
		*tzmonCMD = TA_TZMON_ADMIN_CMD_ABUSING_WRITE;
	} else if (	(memcmp(cmd, "ADMIN_CMD_ABUSING_DELETE", cmdLen) == 0) &&
				(cmdLen == strlen("ADMIN_CMD_ABUSING_DELETE"))) {
		*tzmonCMD = TA_TZMON_ADMIN_CMD_ABUSING_DELETE;
	} else {
		printf("There is no tzmonCMD.\n");
		return TEEC_ERROR_BAD_PARAMETERS;
	}

	printf("tzmonCMD is %s\n", cmd);

	return TEEC_SUCCESS;
}

static TEEC_Result parseCMD(int argc, char **argv, uint32_t *tzmonCMD)
{
	TEEC_Result retVal = TEEC_SUCCESS;

	if (argc < 3) {
		printf("argument count is less.\n");
		return TEEC_ERROR_BAD_PARAMETERS;
	}

	if (argv[1] == NULL || argv[2] == NULL) {
		printf("argument count is less.\n");
		return TEEC_ERROR_BAD_PARAMETERS;
	}

	retVal = _tzmonCmd(argv[1], strlen(argv[1]), tzmonCMD);
	if (retVal != TEEC_SUCCESS) {
		return TEEC_ERROR_BAD_PARAMETERS;
	}
	
	memset(&sharedMem, 0x00, sizeof(sharedMem));
	if (memcmp(argv[2], "test", strlen("test")) == 0) {
		switch (*tzmonCMD) {
			case TA_TZMON_CMD_SHA256:
				sharedMem.inDataLen = (uint32_t)strlen((const char *)inData);
				memcpy(sharedMem.inData, inData, sharedMem.inDataLen);
				break;
			case TA_TZMON_CMD_HMAC_SHA256:
				sharedMem.inDataLen = (uint32_t)strlen((const char *)inData);
				memcpy(sharedMem.inData, inData, sharedMem.inDataLen);
				break;
			case TA_TZMON_CMD_RANDOM:
				sharedMem.outDataLen = 32;
				break;
			case TA_TZMON_CMD_AES256_ENC:
				sharedMem.inDataLen = (uint32_t)strlen((const char *)inData);
				memcpy(sharedMem.inData, inData, sharedMem.inDataLen);
				sharedMem.ivLen = (uint32_t)strlen((const char *)iv);
				memcpy(sharedMem.iv, iv, sharedMem.ivLen);
				break;
			case TA_TZMON_CMD_AES256_DEC:
				sharedMem.inDataLen = (uint32_t)strlen((const char *)encData);
				memcpy(sharedMem.inData, encData, sharedMem.inDataLen);
				sharedMem.ivLen = (uint32_t)strlen((const char *)iv);
				memcpy(sharedMem.iv, iv, sharedMem.ivLen);
				break;
			case TA_TZMON_CMD_KDF:
				sharedMem.inDataLen = (uint32_t)strlen((const char *)inData);
				memcpy(sharedMem.inData, inData, sharedMem.inDataLen);
				break;
			case TA_TZMON_CMD_FILE_WRITE:
				sharedMem.inDataLen = (uint32_t)strlen((const char *)inData);
				memcpy(sharedMem.inData, inData, sharedMem.inDataLen);
				break;
			case TA_TZMON_CMD_FILE_READ:
				break;
			case TA_TZMON_CMD_FILE_DELETE:
				break;
			case TA_TZMON_ADMIN_CMD_APPPREHASH_WRITE:
				sharedMem.inDataLen = (uint32_t)strlen((const char *)inData);
				memcpy(sharedMem.inData, inData, sharedMem.inDataLen);
				sharedMem.ivLen = (uint32_t)strlen((const char *)ADMIN_PWD);
				memcpy(sharedMem.iv, (char *)ADMIN_PWD, sharedMem.ivLen);
				break;
			case TA_TZMON_ADMIN_CMD_APPPREHASH_READ:
				sharedMem.ivLen = (uint32_t)strlen((const char *)ADMIN_PWD);
				memcpy(sharedMem.iv, (char *)ADMIN_PWD, sharedMem.ivLen);
				break;
			case TA_TZMON_ADMIN_CMD_MKEY_WRITE:
				sharedMem.ivLen = (uint32_t)strlen((const char *)ADMIN_PWD);
				memcpy(sharedMem.iv, (char *)ADMIN_PWD, sharedMem.ivLen);
				break;
			case TA_TZMON_ADMIN_CMD_MKEY_READ:
				sharedMem.ivLen = (uint32_t)strlen((const char *)ADMIN_PWD);
				memcpy(sharedMem.iv, (char *)ADMIN_PWD, sharedMem.ivLen);
				break;
			default:
				retVal = TEEC_ERROR_BAD_PARAMETERS;
				break;
		}
	} else {
		switch (*tzmonCMD) {
			case TA_TZMON_CMD_INIT_FLAG:
				sharedMem.inDataLen = strlen(argv[2]);
				memcpy(sharedMem.inData, argv[2], sharedMem.inDataLen);
				printf("%s(%d)\n", sharedMem.inData, sharedMem.inDataLen);
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
			case TA_TZMON_ADMIN_CMD_ABUSING_WRITE:
				tzmon_abusing_write((unsigned char *)argv[2],
						sharedMem.inData, &sharedMem.inDataLen);
				break;
			case TA_TZMON_ADMIN_CMD_ABUSING_DELETE:
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
				break;
			case TA_TZMON_CMD_TVERIFY:
				break;
			default:
				retVal = TEEC_ERROR_BAD_PARAMETERS;
				break;
		}
	}

	return retVal;
}

int main(int argc, char **argv)
{
	TEEC_Result res;
	TEEC_Context ctx;
	TEEC_Session sess;
	TEEC_Operation op;
	TEEC_UUID uuid = TA_TZMON_UUID;
	
	FILE *fp = NULL;

	char result[1024] = { 0x00, };
	uint32_t err_origin, tzmonCMD, resultLen, ii;

	res = parseCMD(argc, argv, &tzmonCMD);
	if (res != TEEC_SUCCESS) {
		errx(1, "parseCMD failed with code 0x%x", res);
	}

	/* Initialize a context connecting us to the TEE */
	res = TEEC_InitializeContext(NULL, &ctx);
	if (res != TEEC_SUCCESS)
		errx(1, "TEEC_InitializeContext failed with code 0x%x", res);

	/*
	 * Open a session to the "hello world" TA, the TA will print "hello
	 * world!" in the log when the session is created.
	 */
	res = TEEC_OpenSession(&ctx, &sess, &uuid,
			       TEEC_LOGIN_PUBLIC, NULL, NULL, &err_origin);
	if (res != TEEC_SUCCESS)
		errx(1, "TEEC_Opensession failed with code 0x%x origin 0x%x",
			res, err_origin);

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
	fp = fopen(RESULT, "w+");
	if (fp == NULL) {
		printf("fopen is failed\n");
		goto exit;
	}

	if (tzmonCMD != TA_TZMON_CMD_RANDOM) {
		printBuf((uint8_t *)"InputData", sharedMem.inData, sharedMem.inDataLen);
	}

	if (tzmonCMD != TA_TZMON_CMD_UTOKEN) {
		res = TEEC_InvokeCommand(&sess, tzmonCMD, &op, &err_origin);
	} else {
		res = _connectServer(sharedMem.inData, sharedMem.inDataLen, 
				sharedMem.outData, &sharedMem.outDataLen);
	}

	if (tzmonCMD == TA_TZMON_CMD_TPRETOKEN) {
		printf("delay: %0.4f\n", sharedMem.tGap);
	}

	if (res != TEEC_SUCCESS) {
		printBuf((uint8_t *)"For debug", sharedMem.outData,
				sharedMem.outDataLen);

		fwrite("fail", 1, strlen("fail"), fp);
		fclose(fp);

		errx(1, "TEEC_InvokeCommand failed with code 0x%x origin 0x%x",
			res, err_origin);
	}

	printBuf((uint8_t *)argv[1], sharedMem.outData, sharedMem.outDataLen);
	printBuf((uint8_t *)"For debug", sharedMem.inData, sharedMem.inDataLen);

	memset(result, 0x00, sizeof(result));
	for (ii = 0; ii < sharedMem.outDataLen; ii++) {
		sprintf(&result[ii * 2], "%02x", (unsigned int)sharedMem.outData[ii]);
	}
	resultLen = strlen(result);
	printf("[TZMon_result]:\n");
	printf("%s(%d)\n", result, resultLen);
	fwrite(result, 1, resultLen + 1, fp);

	/*
	 * We're done with the TA, close the session and
	 * destroy the context.
	 *
	 * The TA will print "Goodbye!" in the log when the
	 * session is closed.
	 */
exit:
	fclose(fp);

	TEEC_CloseSession(&sess);

	TEEC_FinalizeContext(&ctx);

	return 0;
}
