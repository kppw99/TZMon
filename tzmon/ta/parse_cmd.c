#include "parse_cmd.h"

#include <stdio.h>
#include <string.h>

#include "tzmon_ta.h"
#include "random.h"
#include "sha.h"
#include "hmac.h"
#include "aes.h"
#include "kdf.h"
#include "secure_storage.h"
#include "update.h"

#define ADMIN_PWD		"wjstkdgns"
#define APP_PRE_HASH	"appPreHash"
#define MKEY_NAME		"mKey"
#define ABUSING			"abusing"

#define IKEY_NAME		"iKey"
#define ITOKEN_NAME		"iToken"
#define IFLAG_NAME		"iFlag"

#define UKEY_NAME		"uKey"
#define UTOKEN_NAME		"uToken"
#define UFLAG_NAME		"uFlag"

#define APREFLAG_NAME	"aPreFlag"
#define ATOKEN_NAME		"aToken"
#define AFLAG_NAME		"aFlag"

#define TPREFLAG_NAME	"tPreFlag"
#define TTOKEN_NAME		"tToken"
#define TFLAG_NAME		"tFlag"

#define DELAY			(125)	// 125 millisec --> 0.125 sec.

#if (USE_TEST_DATA)
static uint8_t fileName[8 + 1] = {
	0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08,
	0x00
};

static uint8_t key[32 + 1] = {      // 32
	0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08,
	0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08,
	0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08,
	0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08,
	0x00
};

static uint8_t key2[17 + 1] = {    // 17
	0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08,
	0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08,
	0x08, 0x00
};

static uint32_t keyLen = 32;
static uint32_t key2Len = 17;
#endif

static uint8_t resultMsg[32 + 1] = {
	0x9e, 0x85, 0x19, 0x61, 0x3f, 0xe3, 0x67, 0xf8,
    0xf3, 0x85, 0x2a, 0xe8, 0x78, 0x5d, 0x58, 0xa0,
    0x12, 0x02, 0xdf, 0x7a, 0x9d, 0x83, 0x74, 0x3a,
    0x9a, 0xe8, 0x85, 0xf4, 0x6e, 0x81, 0x93, 0xf4,
    0x00
};

static uint8_t flagSet[1 + 1] = { 0x01, 0x00 };

static uint8_t mKey[32] = { 0x00, };
static uint8_t iKey[32] = { 0x00, };
static uint8_t uKey[32] = { 0x00, };

static uint8_t pwd[32] = { 0x00, };
static uint8_t salt[32] = { 0x00, };

static uint8_t iToken[32] = { 0x00, };
static uint8_t uToken[32] = { 0x00, };
static uint8_t aToken[32] = { 0x00, };
static uint8_t tToken[32] = { 0x00, };

static TEE_Result _test_sha256(SharedMem *sharedMem, uint8_t *outData, uint32_t *outDataLen)
{
	if (sharedMem == NULL) return TEE_ERROR_BAD_PARAMETERS;

	TEE_Result retVal;

	uint8_t *inData = sharedMem->inData;
	uint32_t inDataLen = sharedMem->inDataLen;
	
	*outDataLen = 32;
	retVal = TZMON_SHA256(inData, inDataLen, outData, outDataLen);
	CHECK(retVal, "TZMON_SHA256", return retVal;);
	
	return retVal;
}

static TEE_Result _test_hmac(SharedMem *sharedMem, uint8_t *outData, uint32_t *outDataLen)
{
	if (sharedMem == NULL) return TEE_ERROR_BAD_PARAMETERS;

	TEE_Result retVal;

	uint8_t *inData = sharedMem->inData;
	uint32_t inDataLen = sharedMem->inDataLen;
	
	*outDataLen = 32;
	retVal = TZMON_HMAC_SHA256(key2, key2Len, inData, inDataLen, outData, outDataLen);
	CHECK(retVal, "TZMON_HMAC_SHA256", return retVal;);

	return retVal;
}

static TEE_Result _test_random(SharedMem *sharedMem, uint8_t *outData, uint32_t *outDataLen)
{
	if (sharedMem == NULL) return TEE_ERROR_BAD_PARAMETERS;

	TEE_Result retVal;

	retVal = TZMON_Random(outData, *outDataLen);
	CHECK(retVal, "TZMON_Random", return retVal;);

	return retVal;
}

static TEE_Result _test_aesEnc(SharedMem *sharedMem, uint8_t *outData, uint32_t *outDataLen)
{
	if (sharedMem == NULL) return TEE_ERROR_BAD_PARAMETERS;

	TEE_Result retVal;

	uint8_t *iv = sharedMem->iv;
	uint8_t *inData = sharedMem->inData;
	
	uint32_t ivLen = sharedMem->ivLen;
	uint32_t inDataLen = sharedMem->inDataLen;

	*outDataLen = 32;
	retVal = TZMON_AES256_ENC(inData, inDataLen, key, keyLen, iv, ivLen,
			outData, outDataLen);
	CHECK(retVal, "TZMON_AES256_ENC", return retVal;);

	return retVal;
}

static TEE_Result _test_aesDec(SharedMem *sharedMem, uint8_t *outData, uint32_t *outDataLen)
{
	if (sharedMem == NULL) return TEE_ERROR_BAD_PARAMETERS;

	TEE_Result retVal;
	
	uint8_t *iv = sharedMem->iv;
	uint8_t *inData = sharedMem->inData;
	
	uint32_t ivLen = sharedMem->ivLen;
	uint32_t inDataLen = sharedMem->inDataLen;
	
	*outDataLen = 32;
	retVal = TZMON_AES256_DEC(inData, inDataLen, key, keyLen, iv, ivLen,
			outData, outDataLen);
	CHECK(retVal, "TZMON_AES256_DEC", return retVal;);

	return retVal;
}

static TEE_Result _test_kdf(SharedMem *sharedMem, uint8_t *outData, uint32_t *outDataLen)
{
	if (sharedMem == NULL) return TEE_ERROR_BAD_PARAMETERS;

	TEE_Result retVal;

	uint8_t *random = sharedMem->inData;
	uint32_t randomLen = sharedMem->inDataLen;

	*outDataLen = 64;

	retVal = TZMON_KDF(key, keyLen, random, randomLen, outData, *outDataLen);
	CHECK(retVal, "TZMON_KDF", return retVal;);

	return retVal;
}

static TEE_Result _test_file_write(SharedMem *sharedMem, uint8_t *outData, uint32_t *outDataLen)
{
	if (sharedMem == NULL) return TEE_ERROR_BAD_PARAMETERS;

	TEE_Result retVal;

	uint8_t *data = sharedMem->inData;
	uint32_t dataLen = sharedMem->inDataLen;
	uint32_t fileNameLen = strlen((const char*)fileName);

	retVal = TZMON_WriteFile(fileName, fileNameLen, data, dataLen);
	CHECK(retVal, "TZMON_FileWrite", return retVal;);

	outData[0] = 0x00;
	*outDataLen = 0x01;

	return retVal;
}

static TEE_Result _test_file_read(SharedMem *sharedMem, uint8_t *outData, uint32_t *outDataLen)
{
	if (sharedMem == NULL) return TEE_ERROR_BAD_PARAMETERS;

	TEE_Result retVal;
	
	uint32_t fileNameLen = strlen((const char*)fileName);
	
	*outDataLen = DATA_SIZE;

	retVal = TZMON_ReadFile(fileName, fileNameLen, outData, outDataLen);
	CHECK(retVal, "TZMON_ReadFile", return retVal;);

	return retVal;
}

static TEE_Result _test_file_delete(SharedMem *sharedMem, uint8_t *outData, uint32_t *outDataLen)
{
	if (sharedMem == NULL) return TEE_ERROR_BAD_PARAMETERS;

	TEE_Result retVal;

	uint32_t fileNameLen = strlen((const char*)fileName);

	retVal = TZMON_DeleteFile(fileName, fileNameLen);
	CHECK(retVal, "TZMON_DeleteFile", return retVal;);

	outData[0] = 0x00;
	*outDataLen = 0x01;

	return retVal;
}

static TEE_Result _admin_mKey_write(SharedMem *sharedMem, uint8_t *outData,
		uint32_t *outDataLen)
{
	if (sharedMem == NULL) return TEE_ERROR_BAD_PARAMETERS;

	TEE_Result retVal;

	uint32_t mKeyLen = 32;

	uint8_t *pPwd = sharedMem->iv;
	uint32_t pwdLen = sharedMem->ivLen;
	
	if (pwdLen != strlen(ADMIN_PWD) || memcmp(pPwd, ADMIN_PWD, pwdLen) != 0) {
		retVal = TEE_ERROR_ACCESS_DENIED;
	} else {
		retVal = TZMON_Random(mKey, mKeyLen);
		CHECK(retVal, "TZMON_Random", return retVal;);

		retVal = TZMON_WriteFile((uint8_t *)MKEY_NAME,
				(uint32_t)strlen(MKEY_NAME), mKey, mKeyLen);
		CHECK(retVal, "TZMON_WriteFile", return retVal;);
	}

	outData[0] = 0x00;
	*outDataLen = 0x01;

	return retVal;
}

static TEE_Result _admin_mKey_read(SharedMem *sharedMem, uint8_t *outData,
		uint32_t *outDataLen)
{
	if (sharedMem == NULL) return TEE_ERROR_BAD_PARAMETERS;

	TEE_Result retVal;

	uint8_t *pPwd = sharedMem->iv;
	uint32_t pwdLen = sharedMem->ivLen;

	if (pwdLen != strlen(ADMIN_PWD) || memcmp(pPwd, ADMIN_PWD, pwdLen) != 0) {
		retVal = TEE_ERROR_ACCESS_DENIED;
	} else {
		*outDataLen = DATA_SIZE;
		retVal = TZMON_ReadFile((uint8_t *)MKEY_NAME, 
				(uint32_t)strlen(MKEY_NAME), outData, outDataLen);
		CHECK(retVal, "TZMON_WriteFile", return retVal;);
	}

	return retVal;
}

static TEE_Result _admin_abusing_write(SharedMem *sharedMem, uint8_t *outData,
		uint32_t *outDataLen)
{
	if (sharedMem == NULL) return TEE_ERROR_BAD_PARAMETERS;

	TEE_Result retVal;

	uint8_t *list = sharedMem->inData;
	uint32_t listLen= sharedMem->inDataLen;

	retVal = TZMON_WriteFile((uint8_t *)ABUSING, (uint32_t)strlen(ABUSING), list, listLen);
	CHECK(retVal, "TZMON_WriteFile", return retVal;);

	outData[0] = 0x00;
	*outDataLen = 0x01;

	return retVal;
}

static TEE_Result _admin_abusing_delete(SharedMem *sharedMem, uint8_t *outData,
		uint32_t *outDataLen)
{
	if (sharedMem == NULL) return TEE_ERROR_BAD_PARAMETERS;

	TEE_Result retVal;

	retVal = TZMON_DeleteFile((uint8_t *)ABUSING, (uint32_t)strlen(ABUSING));
	CHECK(retVal, "TZMON_DeleteFile", return retVal;);

	outData[0] = 0x00;
	*outDataLen = 0x01;

	return retVal;
}

static TEE_Result _admin_appPreHash_write(SharedMem *sharedMem, uint8_t *outData,
		uint32_t *outDataLen)
{
	if (sharedMem == NULL) return TEE_ERROR_BAD_PARAMETERS;

	TEE_Result retVal;

	uint8_t *pPwd = sharedMem->iv;
	uint32_t pwdLen = sharedMem->ivLen;

	uint8_t *appHash = sharedMem->inData;
	uint32_t appHashLen = sharedMem->inDataLen;

	if (pwdLen != strlen(ADMIN_PWD) || memcmp(pPwd, ADMIN_PWD, pwdLen) != 0) {
		retVal = TEE_ERROR_ACCESS_DENIED;
	} else {
		retVal = TZMON_WriteFile((uint8_t *)APP_PRE_HASH,
				(uint32_t)strlen(APP_PRE_HASH), appHash, appHashLen);
		CHECK(retVal, "TZMON_WriteFile", return retVal;);
	}

	outData[0] = 0x00;
	*outDataLen = 0x01;

	return retVal;
}

static TEE_Result _admin_appPreHash_read(SharedMem *sharedMem, uint8_t *outData,
		uint32_t *outDataLen)
{
	if (sharedMem == NULL) return TEE_ERROR_BAD_PARAMETERS;

	TEE_Result retVal;

	uint8_t *pPwd = sharedMem->iv;
	uint32_t pwdLen = sharedMem->ivLen;

	if (pwdLen != strlen(ADMIN_PWD) || memcmp(pPwd, ADMIN_PWD, pwdLen) != 0) {
		retVal = TEE_ERROR_ACCESS_DENIED;
	} else {
		*outDataLen = DATA_SIZE;
		retVal = TZMON_ReadFile((uint8_t *)APP_PRE_HASH, 
				(uint32_t)strlen(APP_PRE_HASH), outData, outDataLen);
		CHECK(retVal, "TZMON_ReadFile", return retVal;);
	}

	return retVal;
}

static TEE_Result _initFlag(SharedMem *sharedMem, uint8_t *outData, uint32_t *outDataLen)
{
	if (sharedMem == NULL) return TEE_ERROR_BAD_PARAMETERS;

	uint8_t *inData = sharedMem->inData;
	uint32_t inDataLen = sharedMem->inDataLen;

	outData[0] = 0x00;
	*outDataLen = 0x01;

	if (strncmp((const char *)inData, "all", inDataLen) == 0) {
		TZMON_DeleteFile((uint8_t *)IFLAG_NAME, (uint32_t)strlen(IFLAG_NAME));
		TZMON_DeleteFile((uint8_t *)UFLAG_NAME, (uint32_t)strlen(UFLAG_NAME));
		TZMON_DeleteFile((uint8_t *)AFLAG_NAME, (uint32_t)strlen(AFLAG_NAME));
		TZMON_DeleteFile((uint8_t *)TFLAG_NAME, (uint32_t)strlen(TFLAG_NAME));
		TZMON_DeleteFile((uint8_t *)APREFLAG_NAME, (uint32_t)strlen(APREFLAG_NAME));
		TZMON_DeleteFile((uint8_t *)TPREFLAG_NAME, (uint32_t)strlen(TPREFLAG_NAME));
	} else if (strncmp((const char *)inData, "iFlag", inDataLen) == 0) {
		TZMON_DeleteFile((uint8_t *)IFLAG_NAME, (uint32_t)strlen(IFLAG_NAME));
	} else if (strncmp((const char *)inData, "uFlag", inDataLen) == 0) {
		TZMON_DeleteFile((uint8_t *)UFLAG_NAME, (uint32_t)strlen(UFLAG_NAME));
	} else if (strncmp((const char *)inData, "aFlag", inDataLen) == 0) {
		TZMON_DeleteFile((uint8_t *)AFLAG_NAME, (uint32_t)strlen(AFLAG_NAME));
	} else if (strncmp((const char *)inData, "tFlag", inDataLen) == 0) {
		TZMON_DeleteFile((uint8_t *)TFLAG_NAME, (uint32_t)strlen(TFLAG_NAME));
	} else if (strncmp((const char *)inData, "aPreFlag", inDataLen) == 0) {
		TZMON_DeleteFile((uint8_t *)APREFLAG_NAME, (uint32_t)strlen(APREFLAG_NAME));
	} else if (strncmp((const char *)inData, "tPreFlag", inDataLen) == 0) {
		TZMON_DeleteFile((uint8_t *)TPREFLAG_NAME, (uint32_t)strlen(TPREFLAG_NAME));
	} else {
		outData[0] = 0x01;
		return TEE_ERROR_GENERIC;
	}

	return TEE_SUCCESS;
}

static TEE_Result _iKey(SharedMem *sharedMem, uint8_t *outData, uint32_t *outDataLen)
{
	if (sharedMem == NULL) return TEE_ERROR_BAD_PARAMETERS;

	TEE_Result retVal;

	uint8_t *version = sharedMem->inData;
	uint32_t versionLen = 32;
	uint32_t mKeyLen = sizeof(mKey);

	retVal = TZMON_Random(salt, sizeof(salt));
	CHECK(retVal, "TZMON_Random", return retVal;);

	retVal = TZMON_ReadFile((uint8_t *)MKEY_NAME, 
			(uint32_t)strlen(MKEY_NAME), mKey, &mKeyLen);
	CHECK(retVal, "TZMON_ReadFile", return retVal;);

	retVal = TZMON_XOR(mKey, sizeof(mKey), version, versionLen,
			pwd, sizeof(pwd));
	CHECK(retVal, "TZMON_XOR", return retVal;);

	*outDataLen = 32;
	retVal = TZMON_KDF(pwd, sizeof(pwd), salt, sizeof(salt),
			outData, *outDataLen);
	CHECK(retVal, "TZMON_KDF", return retVal;);

	retVal = TZMON_WriteFile((uint8_t *)IKEY_NAME,
			(uint32_t)strlen(IKEY_NAME), outData, *outDataLen);
	CHECK(retVal, "TZMON_WriteFile", return retVal;);

	return retVal;
}

static TEE_Result _iToken(SharedMem *sharedMem, uint8_t *outData, uint32_t *outDataLen)
{
	if (sharedMem == NULL) return TEE_ERROR_BAD_PARAMETERS;

	TEE_Result retVal;

	uint8_t appPreHash[32] = { 0x00, };
	uint8_t appHash[32] = { 0x00, };
	uint8_t tmpIn[32] = { 0x00, };
	
	uint32_t appPreHashLen = sizeof(appPreHash);
	uint32_t appHashLen = sizeof(appHash);
	uint32_t iKeyLen = sizeof(iKey);

	uint8_t *inData = sharedMem->inData;
	uint32_t inDataLen = sharedMem->inDataLen;

	retVal = TZMON_ReadFile((uint8_t *)APP_PRE_HASH,
			(uint32_t)strlen(APP_PRE_HASH), appPreHash, &appPreHashLen);
	CHECK(retVal, "TZMON_ReadFile", return retVal;);

	retVal = TZMON_ReadFile((uint8_t *)IKEY_NAME,
			(uint32_t)strlen(IKEY_NAME), iKey, &iKeyLen);
	CHECK(retVal, "TZMON_ReadFile", return retVal;);

	retVal = TZMON_XOR(appPreHash, appPreHashLen, iKey, iKeyLen,
			tmpIn, sizeof(tmpIn));
	CHECK(retVal, "TZMON_XOR", return retVal;);

	retVal = TZMON_SHA256(tmpIn, sizeof(tmpIn), appHash, &appHashLen);
	CHECK(retVal, "TZMON_SHA256", return retVal;);

	if ((appHashLen != inDataLen) ||
			(memcmp(appHash, inData, appHashLen) != 0)) {
		memcpy(sharedMem->inData, appHash, appHashLen);
		return TEE_ERROR_GENERIC;
	}

	retVal = TZMON_Random(salt, sizeof(salt));
	CHECK(retVal, "TZMON_Random", return retVal;);

	*outDataLen = 32;
	retVal = TZMON_KDF(appHash, appHashLen, salt, sizeof(salt),
			outData, *outDataLen);
	CHECK(retVal, "TZMON_KDF", return retVal;);

	retVal = TZMON_WriteFile((uint8_t *)ITOKEN_NAME,
			(uint32_t)strlen(ITOKEN_NAME), outData, *outDataLen);
	CHECK(retVal, "TZMON_WriteFile", return retVal;);

	return retVal;
}

static TEE_Result _iVerify(SharedMem *sharedMem, uint8_t *outData, uint32_t *outDataLen)
{
	if (sharedMem == NULL) return TEE_ERROR_BAD_PARAMETERS;

	TEE_Result retVal;

	uint8_t cMsg[32] = { 0x00, };
	uint8_t iFlag[2] = { 0x01, 0x00 };
	
	uint32_t cMsgLen = sizeof(cMsg);
	uint32_t iTokenLen = sizeof(iToken);
	uint32_t iFlagLen = strlen((const char *)iFlag);
	uint32_t resultMsgLen = (uint32_t)strlen((const char*)resultMsg);

	uint8_t *inData = sharedMem->inData;
	uint32_t inDataLen = sharedMem->inDataLen;

	retVal = TZMON_ReadFile((uint8_t *)ITOKEN_NAME,
			(uint32_t)strlen(ITOKEN_NAME), iToken, &iTokenLen);
	CHECK(retVal, "TZMON_ReadFile", return retVal;);

	retVal = TZMON_HMAC_SHA256(iToken, iTokenLen,
			resultMsg, resultMsgLen, cMsg, &cMsgLen);
	CHECK(retVal, "TZMON_HMAC_SHA256", return retVal;);

	*outDataLen = 32;
	retVal = TZMON_XOR(cMsg, cMsgLen, inData, inDataLen, outData, *outDataLen);
	CHECK(retVal, "TZMON_XOR", return retVal;);

	if ((cMsgLen != inDataLen) || (memcmp(cMsg, inData, cMsgLen) != 0)) {
		memcpy(sharedMem->inData, cMsg, cMsgLen);
		retVal = TEE_ERROR_GENERIC;
	} else {
		retVal = TZMON_WriteFile((uint8_t *)IFLAG_NAME,
				(uint32_t)strlen(IFLAG_NAME), iFlag, iFlagLen);
		CHECK(retVal, "TZMON_WriteFile", return retVal;);
	}

	return retVal;
}

static TEE_Result _uKey(SharedMem *sharedMem, uint8_t *outData, uint32_t *outDataLen)
{
	if (sharedMem == NULL) return TEE_ERROR_BAD_PARAMETERS;

	TEE_Result retVal;

	uint8_t iFlag[2] = { 0x00 };
	uint8_t uFlag[2] = { 0x00, };
	uint8_t sendBuf[64] = { 0x00, };

	uint32_t pwdLen = sizeof(pwd);
	uint32_t saltLen = sizeof(salt);
	uint32_t mKeyLen = sizeof(mKey);
	uint32_t uKeyLen = sizeof(uKey);
	uint32_t iFlagLen = sizeof(iFlag);
	uint32_t uFlagLen = sizeof(uFlag);
	uint32_t iTokenLen = sizeof(iToken);
	uint32_t sendBufLen = sizeof(sendBuf);

	uint8_t *inData = sharedMem->inData;
	uint32_t inDataLen = sharedMem->inDataLen;

	retVal = TZMON_ReadFile((uint8_t *)ITOKEN_NAME,
			(uint32_t)strlen(ITOKEN_NAME), iToken, &iTokenLen);
	CHECK(retVal, "TZMON_ReadFile", return retVal;);

	if (strncmp((const char *)iToken, (const char *)inData, inDataLen) != 0) {
		*outDataLen = inDataLen;
		memcpy(outData, inData, inDataLen);
		return TEE_ERROR_GENERIC;
	}

	retVal = TZMON_ReadFile((uint8_t *)IFLAG_NAME,
			(uint32_t)strlen(IFLAG_NAME), iFlag, &iFlagLen); 
	CHECK(retVal, "TZMON_ReadFile", return retVal;);

	if (strncmp((const char *)iFlag, (const char *)flagSet, iFlagLen) != 0) {
		*outDataLen = iFlagLen;
		memcpy(outData, iFlag, iFlagLen);
		return TEE_ERROR_GENERIC;
	}

	retVal = TZMON_Random(salt, sizeof(salt));
	CHECK(retVal, "TZMON_Random", return retVal;);

	retVal = TZMON_ReadFile((uint8_t *)MKEY_NAME, 
			(uint32_t)strlen(MKEY_NAME), mKey, &mKeyLen);
	CHECK(retVal, "TZMON_ReadFile", return retVal;);

	retVal = TZMON_XOR(iToken, iTokenLen, mKey, mKeyLen, pwd, pwdLen);
	CHECK(retVal, "TZMON_XOR", return retVal;);

	retVal = TZMON_KDF(pwd, pwdLen, salt, saltLen, uKey, uKeyLen);
	CHECK(retVal, "TZMON_KDF", return retVal;);

	retVal = TZMON_WriteFile((uint8_t *)UKEY_NAME,
			(uint32_t)strlen(UKEY_NAME), uKey, uKeyLen);
	CHECK(retVal, "TZMON_WriteFile", return retVal;);

	sendBuf[0] = 0x01;
	sendBuf[1] = uKeyLen;
	memcpy((char *)sendBuf + 2, (const char *)uKey, uKeyLen);
	sendBufLen = uKeyLen + 2;
	retVal = TZMON_ConnectServer(sendBuf, sendBufLen, uFlag, &uFlagLen);
	CHECK(retVal, "TZMON_ConnectServer", return retVal;);

	if (strncmp((const char *)uFlag, (const char *)flagSet, uFlagLen) != 0) {
		*outDataLen = uFlagLen;
		memcpy(outData, uFlag, *outDataLen);
		return TEE_ERROR_GENERIC;
	}

	retVal = TZMON_Random(salt, saltLen);
	CHECK(retVal, "TZMON_Random", return retVal;);

	*outDataLen = 32;
	retVal = TZMON_KDF(uKey, uKeyLen, salt, saltLen, outData, *outDataLen);
	CHECK(retVal, "TZMON_KDF", return retVal;);

	sendBuf[0] = 0x02;
	sendBuf[1] = *outDataLen;
	memcpy((char *)sendBuf + 2, (const char *)outData, *outDataLen);
	sendBufLen = *outDataLen + 2;
	retVal = TZMON_ConnectServer(sendBuf, sendBufLen, uFlag, &uFlagLen);
	CHECK(retVal, "TZMON_ConnectServer", return retVal;);

	if (strncmp((const char *)uFlag, (const char *)flagSet, uFlagLen) != 0) {
		*outDataLen = uFlagLen;
		memcpy(outData, uFlag, *outDataLen);
		return TEE_ERROR_GENERIC;
	}

	retVal = TZMON_WriteFile((uint8_t *)UTOKEN_NAME,
			(uint32_t)strlen(UTOKEN_NAME), outData, *outDataLen);
	CHECK(retVal, "TZMON_WriteFile", return retVal;);

	return retVal;
}

static TEE_Result _uVerify(SharedMem *sharedMem, uint8_t *outData, uint32_t *outDataLen)
{
	if (sharedMem == NULL) return TEE_ERROR_BAD_PARAMETERS;

	TEE_Result retVal;

	uint8_t *inData = sharedMem->inData;

	uint8_t *encFile = inData;
	uint32_t encFileLen = 64;
	uint8_t *sMac = inData + encFileLen;
	uint32_t sMacLen = 32;
	uint8_t *lMac = inData + encFileLen + sMacLen;
	uint32_t lMacLen = 32;

	uint8_t mac[32] = { 0x00, };
	uint8_t file[64] = { 0x00, };
	uint8_t uFlag[2] = { 0x02, 0x00 };
	
	uint32_t macLen = sizeof(mac);
	uint32_t uKeyLen = sizeof(uKey);
	uint32_t fileLen = sizeof(file);
	uint32_t uTokenLen = sizeof(uToken);
	uint32_t uFlagLen = strlen((const char *)uFlag);

	retVal = TZMON_ReadFile((uint8_t *)UTOKEN_NAME,
			(uint32_t)strlen(UTOKEN_NAME), uToken, &uTokenLen);
	CHECK(retVal, "TZMON_ReadFile", return retVal;);

	retVal = TZMON_HMAC_SHA256(uToken, uTokenLen, encFile, encFileLen,
			mac, &macLen);
	CHECK(retVal, "TZMON_HMAC_SHA256", return retVal;);

	if (macLen != lMacLen || 
			strncmp((const char *)lMac, (const char *)mac, macLen) != 0) {
		return TEE_ERROR_GENERIC;
	}

	retVal = TZMON_ReadFile((uint8_t *)UKEY_NAME,
			(uint32_t)strlen(UKEY_NAME), uKey, &uKeyLen);
	CHECK(retVal, "TZMON_ReadFile", return retVal;);

	retVal = TZMON_AES256_DEC(encFile, encFileLen, uKey, uKeyLen,
			uToken, 16, file, &fileLen);
	CHECK(retVal, "TZMON_AES256_DEC", return retVal;);

	retVal = TZMON_HMAC_SHA256(uKey, uKeyLen, file, fileLen,
			mac, &macLen);
	CHECK(retVal, "TZMON_HMAC_SHA256", return retVal;);

	if (strncmp((const char *)sMac, (const char *)mac, macLen) != 0) {
		return TEE_ERROR_GENERIC;
	}

	retVal = TZMON_WriteFile((uint8_t *)UFLAG_NAME,
			(uint32_t)strlen(UFLAG_NAME), uFlag, uFlagLen);
	CHECK(retVal, "TZMON_WriteFile", return retVal;);

	memcpy(outData, uFlag, uFlagLen);
	*outDataLen = uFlagLen;

	return retVal;
}

static TEE_Result _aPreToken(SharedMem *sharedMem, uint8_t *outData, uint32_t *outDataLen)
{
	if (sharedMem == NULL) return TEE_ERROR_BAD_PARAMETERS;

	TEE_Result retVal;

	uint8_t aPreFlag[2] = { 0x01, 0x00 };
	uint32_t aPreFlagLen = strlen((const char *)aPreFlag);

	uint8_t *inData = sharedMem->inData;
	uint32_t inDataLen = sharedMem->inDataLen;

	*outDataLen = DATA_SIZE;
	retVal = TZMON_ReadFile((uint8_t *)ABUSING,
			(uint32_t)strlen(ABUSING), outData, outDataLen);
	if (retVal == TEE_ERROR_ITEM_NOT_FOUND) {
		memcpy(outData, inData, inDataLen);
		*outDataLen = inDataLen;
		return TEE_SUCCESS;
	} else {
		CHECK(retVal, "TZMON_ReadFile", return retVal;);
	}

	if (strstr((const char *)outData, (const char *)inData) != NULL) {
		retVal = TZMON_WriteFile((uint8_t *)APREFLAG_NAME,
				(uint32_t)strlen(APREFLAG_NAME), aPreFlag, aPreFlagLen);
		CHECK(retVal, "TZMON_WriteFile", return retVal;);
		retVal = TEE_ERROR_GENERIC;
	} else {
		memcpy(outData, inData, inDataLen);
		*outDataLen = inDataLen;
		retVal = TEE_SUCCESS;
	}

	return retVal;
}

static TEE_Result _aToken(SharedMem *sharedMem, uint8_t *outData, uint32_t *outDataLen)
{
	if (sharedMem == NULL) return TEE_ERROR_BAD_PARAMETERS;

	TEE_Result retVal;

	uint8_t aPreFlag[2] = { 0x00, };
	uint8_t preToken[32] = { 0x00, };
	
	uint32_t saltLen = sizeof(salt);
	uint32_t iTokenLen = sizeof(iToken);
	uint32_t uTokenLen = sizeof(uToken);
	uint32_t preTokenLen = sizeof(preToken);
	uint32_t aPreFlagLen = sizeof(aPreFlag);

	uint8_t *inData = sharedMem->inData;
	uint32_t inDataLen = sharedMem->inDataLen;

	retVal = TZMON_ReadFile((uint8_t *)APREFLAG_NAME,
			(uint32_t)strlen(APREFLAG_NAME), aPreFlag, &aPreFlagLen);
	if (retVal != TEE_ERROR_ITEM_NOT_FOUND) {
		outData[0] = 0x01;
		*outDataLen = 0x01;
		return TEE_ERROR_GENERIC;
	}

	retVal = TZMON_ReadFile((uint8_t *)ITOKEN_NAME,
			(uint32_t)strlen(ITOKEN_NAME), iToken, &iTokenLen);
	CHECK(retVal, "TZMON_ReadFile", return retVal;);

	retVal = TZMON_ReadFile((uint8_t *)UTOKEN_NAME,
			(uint32_t)strlen(UTOKEN_NAME), uToken, &uTokenLen);
	CHECK(retVal, "TZMON_ReadFile", return retVal;);

	retVal = TZMON_XOR(iToken, iTokenLen, uToken, uTokenLen,
			preToken, preTokenLen);
	CHECK(retVal, "TZMON_XOR", return retVal;);

	if (preTokenLen != inDataLen || 
			strncmp((const char *)preToken, (const char *)inData, inDataLen) != 0x00) {
		memcpy(outData, preToken, preTokenLen);
		*outDataLen = preTokenLen;
		return TEE_ERROR_GENERIC;
	}

	retVal = TZMON_Random(salt, saltLen);
	CHECK(retVal, "TZMON_Random", return retVal;);

	*outDataLen = 32;
	retVal = TZMON_KDF(preToken, preTokenLen, salt, saltLen, outData, *outDataLen);
	CHECK(retVal, "TZMON_KDF", return retVal;);

	retVal = TZMON_WriteFile((uint8_t *)ATOKEN_NAME,
			(uint32_t)strlen(ATOKEN_NAME), outData, *outDataLen);
	CHECK(retVal, "TZMON_WriteFile", return retVal;);

	return retVal;
}

static TEE_Result _aVerify(SharedMem *sharedMem, uint8_t *outData, uint32_t *outDataLen)
{
	if (sharedMem == NULL) return TEE_ERROR_BAD_PARAMETERS;

	TEE_Result retVal;

	uint8_t mac[32] = { 0x00, };
	uint8_t aFlag[2] = { 0x04, 0x00 };

	uint32_t macLen = sizeof(mac);
	uint32_t aTokenLen = sizeof(aToken);
	uint32_t resultMsgLen = (uint32_t)strlen((const char*)resultMsg);

	uint8_t *inData = sharedMem->inData;
	uint32_t inDataLen = sharedMem->inDataLen;
	uint32_t aFlagLen = strlen((const char *)aFlag);

	retVal = TZMON_ReadFile((uint8_t *)ATOKEN_NAME,
			(uint32_t)strlen(ATOKEN_NAME), aToken, &aTokenLen);
	CHECK(retVal, "TZMON_ReadFile", return retVal;);

	retVal = TZMON_HMAC_SHA256(aToken, aTokenLen, resultMsg, resultMsgLen,
			mac, &macLen);
	CHECK(retVal, "TZMON_HMAC_SHA256", return retVal;);

	if (inDataLen != macLen || 
			strncmp((const char *)inData, (const char *)mac, macLen) != 0x00) {
		return TEE_ERROR_GENERIC;
	}

	retVal = TZMON_WriteFile((uint8_t *)AFLAG_NAME,
			(uint32_t)strlen(AFLAG_NAME), aFlag, aFlagLen);
	CHECK(retVal, "TZMON_WriteFile", return retVal;);

	memcpy(outData, aFlag, aFlagLen);
	*outDataLen = aFlagLen;

	return retVal;
}

static TEE_Result _tPreToken(SharedMem *sharedMem, uint8_t *outData, uint32_t *outDataLen)
{
	if (sharedMem == NULL) return TEE_ERROR_BAD_PARAMETERS;

	TEE_Result retVal;
	TEE_Time start, end;
	
	uint8_t tPreFlag[2] = { 0x01, 0x00 };
	uint32_t tPreFlagLen = strlen((const char *)tPreFlag);

	double gap, delay, overhead;
	double tGap = sharedMem->tGap;
	double millisec = 1000.0;

	TEE_GetSystemTime(&start);
	TEE_Wait(DELAY);
	TEE_GetSystemTime(&end);

	overhead = 1.0;
	gap = ((double)end.millis - (double)start.millis) / millisec;

	delay = (tGap >= gap) ? (tGap - gap) : (gap - tGap);
	
	if (delay <= overhead) {
		outData[0] = 0x00;
		*outDataLen = 0x01;
		retVal = TEE_SUCCESS;
	} else {
		outData[0] = 0x01;
		*outDataLen = 0x01;

		retVal = TZMON_WriteFile((uint8_t *)TPREFLAG_NAME,
				(uint32_t)strlen(TPREFLAG_NAME), tPreFlag, tPreFlagLen);
		CHECK(retVal, "TZMON_WriteFile", return retVal;);

		retVal = TEE_ERROR_TIME_NEEDS_RESET;
	}

	sharedMem->tGap = delay;

	return retVal;
}

static TEE_Result _tToken(SharedMem *sharedMem, uint8_t *outData, uint32_t *outDataLen)
{
	if (sharedMem == NULL) return TEE_ERROR_BAD_PARAMETERS;

	TEE_Result retVal;

	uint8_t temp[32] = { 0x00, };
	uint8_t preToken[32] = { 0x00, };
	uint8_t tPreFlag[2] = { 0x00, };

	uint32_t saltLen = sizeof(salt);
	uint32_t iTokenLen = sizeof(iToken);
	uint32_t uTokenLen = sizeof(uToken);
	uint32_t aTokenLen = sizeof(aToken);
	uint32_t tPreFlagLen = sizeof(tPreFlag);
	uint32_t preTokenLen = sizeof(preToken);

	uint8_t *inData = sharedMem->inData;
	uint32_t inDataLen = sharedMem->inDataLen;

	retVal = TZMON_ReadFile((uint8_t *)TPREFLAG_NAME,
			(uint32_t)strlen(TPREFLAG_NAME), tPreFlag, &tPreFlagLen);
	if (retVal != TEE_ERROR_ITEM_NOT_FOUND) {
		outData[0] = 0x01;
		*outDataLen = 0x01;
		return TEE_ERROR_GENERIC;
	}

	retVal = TZMON_ReadFile((uint8_t *)ITOKEN_NAME,
			(uint32_t)strlen(ITOKEN_NAME), iToken, &iTokenLen);
	CHECK(retVal, "TZMON_ReadFile", return retVal;);
	
	retVal = TZMON_ReadFile((uint8_t *)UTOKEN_NAME,
			(uint32_t)strlen(UTOKEN_NAME), uToken, &uTokenLen);
	CHECK(retVal, "TZMON_ReadFile", return retVal;);

	retVal = TZMON_ReadFile((uint8_t *)ATOKEN_NAME,
			(uint32_t)strlen(ATOKEN_NAME), aToken, &aTokenLen);
	CHECK(retVal, "TZMON_ReadFile", return retVal;);

	retVal = TZMON_XOR(iToken, iTokenLen, uToken, uTokenLen, temp, 32);
	CHECK(retVal, "TZMON_XOR", return retVal;);

	retVal = TZMON_XOR(temp, 32, aToken, aTokenLen, preToken, preTokenLen);
	CHECK(retVal, "TZMON_XOR", return retVal;);

	if (preTokenLen != inDataLen ||
			strncmp((const char *)inData, (const char *)preToken, preTokenLen) != 0x00) {
		memcpy(outData, preToken, preTokenLen);
		*outDataLen = preTokenLen;
		return TEE_ERROR_GENERIC;
	}

	retVal = TZMON_Random(salt, saltLen);
	CHECK(retVal, "TZMON_Random", return retVal;);

	*outDataLen = 32;
	retVal = TZMON_KDF(preToken, preTokenLen, salt, saltLen, outData, *outDataLen);
	CHECK(retVal, "TZMON_KDF", return retVal;);

	retVal = TZMON_WriteFile((uint8_t *)TTOKEN_NAME,
			(uint32_t)strlen(TTOKEN_NAME), outData, *outDataLen);
	CHECK(retVal, "TZMON_WriteFile", return retVal;);

	return TEE_SUCCESS;
}

static TEE_Result _tVerify(SharedMem *sharedMem, uint8_t *outData, uint32_t *outDataLen)
{
	if (sharedMem == NULL) return TEE_ERROR_BAD_PARAMETERS;

	TEE_Result retVal;

	uint8_t mac[32] = { 0x00, };
	uint8_t tFlag[2] = { 0x08, 0x00 };

	uint32_t macLen = sizeof(mac);
	uint32_t tTokenLen = sizeof(tToken);
	uint32_t resultMsgLen = (uint32_t)strlen((const char*)resultMsg);

	uint8_t *inData = sharedMem->inData;
	uint32_t inDataLen = sharedMem->inDataLen;
	uint32_t tFlagLen = strlen((const char *)tFlag);

	retVal = TZMON_ReadFile((uint8_t *)TTOKEN_NAME,
			(uint32_t)strlen(TTOKEN_NAME), tToken, &tTokenLen);
	CHECK(retVal, "TZMON_ReadFile", return retVal;);

	retVal = TZMON_HMAC_SHA256(tToken, tTokenLen, resultMsg, resultMsgLen,
			mac, &macLen);
	CHECK(retVal, "TZMON_HMAC_SHA256", return retVal;);

	if (inDataLen != macLen || 
			strncmp((const char *)inData, (const char *)mac, macLen) != 0x00) {
		return TEE_ERROR_GENERIC;
	}

	retVal = TZMON_WriteFile((uint8_t *)TFLAG_NAME,
			(uint32_t)strlen(TFLAG_NAME), tFlag, tFlagLen);
	CHECK(retVal, "TZMON_WriteFile", return retVal;);

	memcpy(outData, tFlag, tFlagLen);
	*outDataLen = tFlagLen;

	return retVal;
}

TEE_Result TZMON_ParseCMD(uint32_t cmdID, TEE_Param params[4])
{
	TEE_Result retVal;

	SharedMem *sharedMem = params[0].memref.buffer;

	uint8_t *outData = sharedMem->outData;
	uint32_t *outDataLen = &(sharedMem->outDataLen);

	switch (cmdID) {
		case TA_TZMON_CMD_INIT_FLAG:
		{
			retVal = _initFlag(sharedMem, outData, outDataLen);
			CHECK(retVal, "_initFlag", return retVal;);
			break;
		}
		case TA_TZMON_CMD_IKEY:
		{
			retVal = _iKey(sharedMem, outData, outDataLen);
			CHECK(retVal, "_iKey", return retVal;);
			break;
		}
		case TA_TZMON_CMD_ITOKEN:
		{
			retVal = _iToken(sharedMem, outData, outDataLen);
			CHECK(retVal, "_iToken", return retVal;);
			break;
		}
		case TA_TZMON_CMD_IVERIFY:
		{
			retVal = _iVerify(sharedMem, outData, outDataLen);
			CHECK(retVal, "_iVerify", return retVal;);
			break;
		}
		case TA_TZMON_CMD_UKEY:
		{
			retVal = _uKey(sharedMem, outData, outDataLen);
			CHECK(retVal, "_uKey", return retVal;);
			break;
		}
		case TA_TZMON_CMD_UVERIFY:
		{
			retVal = _uVerify(sharedMem, outData, outDataLen);
			CHECK(retVal, "_uVerify", return retVal;);
			break;
		}
		case TA_TZMON_CMD_APRETOKEN:
		{
			retVal = _aPreToken(sharedMem, outData, outDataLen);
			CHECK(retVal, "_aPreToken", return retVal;);
			break;
		}
		case TA_TZMON_CMD_ATOKEN:
		{
			retVal = _aToken(sharedMem, outData, outDataLen);
			CHECK(retVal, "_aToken", return retVal;);
			break;
		}
		case TA_TZMON_CMD_AVERIFY:
		{
			retVal = _aVerify(sharedMem, outData, outDataLen);
			CHECK(retVal, "_aVerify", return retVal;);
			break;
		}
		case TA_TZMON_CMD_TPRETOKEN:
		{
			retVal = _tPreToken(sharedMem, outData, outDataLen);
			CHECK(retVal, "_tPreToken", return retVal;);
			break;
		}
		case TA_TZMON_CMD_TTOKEN:
		{
			retVal = _tToken(sharedMem, outData, outDataLen);
			CHECK(retVal, "_aToken", return retVal;);
			break;
		}
		case TA_TZMON_CMD_TVERIFY:
		{
#if 1
			retVal = _tVerify(sharedMem, outData, outDataLen);
			CHECK(retVal, "_aVerify", return retVal;);
#else
			outData[0] = 0x00;
			*outDataLen = 0x01;
			retVal = TEE_SUCCESS;
#endif
			break;
		}
		case TA_TZMON_CMD_SHA256:
		{
			retVal = _test_sha256(sharedMem, outData, outDataLen);
			CHECK(retVal, "_test_sha256", return retVal;);
			break;
		}
		case TA_TZMON_CMD_HMAC_SHA256:
		{
			retVal = _test_hmac(sharedMem, outData, outDataLen);
			CHECK(retVal, "_test_hmac", return retVal;);
			break;
		}
		case TA_TZMON_CMD_RANDOM:
		{
			retVal = _test_random(sharedMem, outData, outDataLen);
			CHECK(retVal, "_test_random", return retVal;);
			break;
		}
		case TA_TZMON_CMD_AES256_ENC:
		{
			retVal = _test_aesEnc(sharedMem, outData, outDataLen);
			CHECK(retVal, "_aesEnc", return retVal;);
			break;
		}
		case TA_TZMON_CMD_AES256_DEC:
		{
			retVal = _test_aesDec(sharedMem, outData, outDataLen);
			CHECK(retVal, "_aesDec", return retVal;);
			break;
		}
		case TA_TZMON_CMD_KDF:
		{
			retVal = _test_kdf(sharedMem, outData, outDataLen);
			CHECK(retVal, "_test_kdf", return retVal;);
			break;
		}
		case TA_TZMON_CMD_FILE_WRITE:
		{
			retVal = _test_file_write(sharedMem, outData, outDataLen);
			CHECK(retVal, "_test_file_write", return retVal;);
			break;
		}
		case TA_TZMON_CMD_FILE_READ:
		{
			retVal = _test_file_read(sharedMem, outData, outDataLen);
			CHECK(retVal, "_test_file_read", return retVal;);
			break;
		}
		case TA_TZMON_CMD_FILE_DELETE:
		{
			retVal = _test_file_delete(sharedMem, outData, outDataLen);
			CHECK(retVal, "_test_file_delete", return retVal;);
			break;
		}
		case TA_TZMON_ADMIN_CMD_MKEY_WRITE:
		{
			retVal = _admin_mKey_write(sharedMem, outData, outDataLen);
			CHECK(retVal, "_admin_mKey_write", return retVal;);
			break;
		}
		case TA_TZMON_ADMIN_CMD_MKEY_READ:
		{
			retVal = _admin_mKey_read(sharedMem, outData, outDataLen);
			CHECK(retVal, "_admin_mKey_read", return retVal;);
			break;
		}
		case TA_TZMON_ADMIN_CMD_ABUSING_WRITE:
		{
			retVal = _admin_abusing_write(sharedMem, outData, outDataLen);
			CHECK(retVal, "_admin_abusing_write", return retVal;);
			break;
		}
		case TA_TZMON_ADMIN_CMD_ABUSING_DELETE:
		{
			retVal = _admin_abusing_delete(sharedMem, outData, outDataLen);
			CHECK(retVal, "_admin_abusing_delete", return retVal;);
			break;
		}
		case TA_TZMON_ADMIN_CMD_APPPREHASH_WRITE:
		{
			retVal = _admin_appPreHash_write(sharedMem, outData, outDataLen);
			CHECK(retVal, "_admin_appPreHash_write", return retVal;);
			break;
		}
		case TA_TZMON_ADMIN_CMD_APPPREHASH_READ:
		{
			retVal = _admin_appPreHash_read(sharedMem, outData, outDataLen);
			CHECK(retVal, "_admin_appPreHash_read", return retVal;);
			break;
		}
		default:
		{
			retVal = TEE_ERROR_BAD_PARAMETERS;
			break;
		}
	}

	return retVal;
}
