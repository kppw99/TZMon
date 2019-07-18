#include "parse_cmd.h"

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

#if (USE_TEST_DATA)
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

TEE_Result TZMON_ParseCMD(uint32_t cmdID, TEE_Param params[4])
{
	TEE_Result retVal;

	SharedMem *sharedMem = params[0].memref.buffer;

	uint8_t *outData = sharedMem->outData;
	uint32_t *outDataLen = &(sharedMem->outDataLen);

	switch (cmdID) {
		case TA_TZMON_CMD_INIT_FLAG:
		{
			uint8_t *inData = sharedMem->inData;
			uint32_t inDataLen = sharedMem->inDataLen;

			retVal = TEE_SUCCESS;
			*outDataLen = 1;
			if (strncmp((const char *)inData, "all", inDataLen) == 0) {
				TZMON_DeleteFile((uint8_t *)IFLAG_NAME, (uint32_t)strlen(IFLAG_NAME));
				TZMON_DeleteFile((uint8_t *)UFLAG_NAME, (uint32_t)strlen(UFLAG_NAME));
			} else if (strncmp((const char *)inData, "iflag", inDataLen) == 0) {
				TZMON_DeleteFile((uint8_t *)IFLAG_NAME, (uint32_t)strlen(IFLAG_NAME));
			} else if (strncmp((const char *)inData, "uflag", inDataLen) == 0) {
				TZMON_DeleteFile((uint8_t *)UFLAG_NAME, (uint32_t)strlen(UFLAG_NAME));
			} else {
				retVal = TEE_ERROR_GENERIC;
			}
			break;
		}
		case TA_TZMON_CMD_IKEY:
		{
			uint8_t salt[32] = { 0x00, };
			uint8_t pwd[32] = { 0x00, };
			uint8_t mKey[32] = { 0x00, };

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
			break;
		}
		case TA_TZMON_CMD_ITOKEN:
		{
			uint8_t appPreHash[32] = { 0x00, };
			uint32_t appPreHashLen = sizeof(appPreHash);

			uint8_t iKey[32] = { 0x00, };
			uint32_t iKeyLen = sizeof(iKey);

			uint8_t tmpIn[32] = { 0x00, };

			uint8_t appHash[32] = { 0x00, };
			uint32_t appHashLen = 32;

			uint8_t *inData = sharedMem->inData;
			uint32_t inDataLen = sharedMem->inDataLen;

			uint8_t salt[32] = { 0x00, };

			retVal = TZMON_ReadFile((uint8_t *)APP_PRE_HASH,
					(uint32_t)strlen(APP_PRE_HASH),
					appPreHash, &appPreHashLen);
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
			break;
		}
		case TA_TZMON_CMD_IVERIFY:
		{
			uint8_t iToken[32] = { 0x00, };
			uint32_t iTokenLen = sizeof(iToken);
			uint32_t resultMsgLen = (uint32_t)strlen((const char*)resultMsg);

			uint8_t cMsg[32] = { 0x00, };
			uint32_t cMsgLen = sizeof(cMsg);

			uint8_t *inData = sharedMem->inData;
			uint32_t inDataLen = sharedMem->inDataLen;

			uint8_t iFlag[2] = { 0x01, 0x00 };
			uint32_t iFlagLen = strlen((const char *)iFlag);

			retVal = TZMON_ReadFile((uint8_t *)ITOKEN_NAME,
					(uint32_t)strlen(ITOKEN_NAME), iToken, &iTokenLen);
			CHECK(retVal, "TZMON_ReadFile", return retVal;);

			retVal = TZMON_HMAC_SHA256(iToken, iTokenLen,
					resultMsg, resultMsgLen, cMsg, &cMsgLen);
			CHECK(retVal, "TZMON_HMAC_SHA256", return retVal;);

			*outDataLen = 32;
			retVal = TZMON_XOR(cMsg, cMsgLen, inData, inDataLen,
					outData, *outDataLen);
			CHECK(retVal, "TZMON_XOR", return retVal;);

			if ((cMsgLen != inDataLen) ||
					(memcmp(cMsg, inData, cMsgLen) != 0)) {
				memcpy(sharedMem->inData, cMsg, cMsgLen);
				retVal = TEE_ERROR_GENERIC;
			} else {
				retVal = TZMON_WriteFile((uint8_t *)IFLAG_NAME,
						(uint32_t)strlen(IFLAG_NAME), iFlag, iFlagLen);
				CHECK(retVal, "TZMON_WriteFile", return retVal;);
			}
			break;
		}
		case TA_TZMON_CMD_UKEY:
		{
			uint8_t *inData = sharedMem->inData;

			uint8_t iToken[32] = { 0x00, };
			uint8_t iFlag[2] = { 0x00 };
			uint8_t salt[32] = { 0x00, };
			uint8_t pwd[32] = { 0x00, };
			uint8_t mKey[32] = { 0x00, };
			uint8_t uKey[32] = { 0x00, };
			uint8_t uFlag[2] = { 0x00, };
			uint8_t sendBuf[64] = { 0x00, };

			uint32_t inDataLen = sharedMem->inDataLen;
			uint32_t iTokenLen = sizeof(iToken);
			uint32_t iFlagLen = sizeof(iFlag);
			uint32_t mKeyLen = sizeof(mKey);
			uint32_t uKeyLen = sizeof(uKey);
			uint32_t uFlagLen = sizeof(uFlag);
			uint32_t sendBufLen = sizeof(sendBuf);

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

			retVal = TZMON_KDF(pwd, sizeof(pwd), salt, sizeof(salt),
					uKey, uKeyLen);
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
			
			retVal = TZMON_Random(salt, sizeof(salt));
			CHECK(retVal, "TZMON_Random", return retVal;);

			*outDataLen = 32;
			retVal = TZMON_KDF(pwd, sizeof(pwd), salt, sizeof(salt),
					outData, *outDataLen);
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
			break;
		}
		case TA_TZMON_CMD_UVERIFY:
		{
			uint8_t *inData = sharedMem->inData;
			uint32_t inDataLen = sharedMem->inDataLen;

			uint8_t *encFile = inData;
			uint32_t encFileLen = 64;
			uint8_t *sMac = inData + 64;
			uint32_t sMacLen = 32;
			uint8_t *lMac = inData + 96;
			uint32_t lMacLen = 32;

			uint8_t mac[32] = { 0x00, };
			uint32_t macLen = sizeof(mac);
			uint8_t uToken[32] = { 0x00, };
			uint32_t uTokenLen = sizeof(uToken);
			uint8_t uKey[32] = { 0x00, };
			uint32_t uKeyLen = sizeof(uKey);
			uint8_t file[64] = { 0x00, };
			uint32_t fileLen = sizeof(file);

			uint8_t uFlag[2] = { 0x02, 0x00 };
			uint32_t uFlagLen = strlen((const char *)uFlag);

			retVal = TZMON_ReadFile((uint8_t *)UTOKEN_NAME,
					(uint32_t)strlen(UTOKEN_NAME), uToken, &uTokenLen);
			CHECK(retVal, "TZMON_ReadFile", return retVal;);

			retVal = TZMON_HMAC_SHA256(uToken, uTokenLen, encFile, encFileLen,
					mac, &macLen);
			CHECK(retVal, "TZMON_HMAC_SHA256", return retVal;);

			if (strncmp((const char *)lMac, (const char *)mac, macLen) != 0) {
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

			break;
		}
		case TA_TZMON_CMD_SHA256:
		{
			uint8_t *inData = sharedMem->inData;
			uint32_t inDataLen = sharedMem->inDataLen;
			*outDataLen = 32;

			retVal = TZMON_SHA256(inData, inDataLen, outData, outDataLen);
			CHECK(retVal, "TZMON_SHA256", return retVal;);
			break;
		}
		case TA_TZMON_CMD_HMAC_SHA256:
		{
			uint8_t *inData = sharedMem->inData;
			uint32_t inDataLen = sharedMem->inDataLen;
			*outDataLen = 32;

			retVal = TZMON_HMAC_SHA256(key2, key2Len, inData, inDataLen,
					outData, outDataLen);
			CHECK(retVal, "TZMON_HMAC_SHA256", return retVal;);
			break;
		}
		case TA_TZMON_CMD_RANDOM:
		{
			retVal = TZMON_Random(outData, *outDataLen);
			CHECK(retVal, "TZMON_Random", return retVal;);
			break;
		}
		case TA_TZMON_CMD_AES256_ENC:
		{
			uint8_t *inData = sharedMem->inData;
			uint32_t inDataLen = sharedMem->inDataLen;
			uint8_t *iv = sharedMem->iv;
			uint32_t ivLen = sharedMem->ivLen;
			*outDataLen = 32;

			retVal = TZMON_AES256_ENC(inData, inDataLen, key, keyLen,
					iv, ivLen, outData, outDataLen);
			CHECK(retVal, "TZMON_AES256_ENC", return retVal;);
			break;
		}
		case TA_TZMON_CMD_AES256_DEC:
		{
			uint8_t *inData = sharedMem->inData;
			uint32_t inDataLen = sharedMem->inDataLen;
			uint8_t *iv = sharedMem->iv;
			uint32_t ivLen = sharedMem->ivLen;
			*outDataLen = 32;

			retVal = TZMON_AES256_DEC(inData, inDataLen, key, keyLen,
					iv, ivLen, outData, outDataLen);
			CHECK(retVal, "TZMON_AES256_DEC", return retVal;);
			break;
		}
		case TA_TZMON_CMD_KDF:
		{
			uint8_t *salt = sharedMem->inData;
			uint32_t saltLen = sharedMem->inDataLen;
			*outDataLen = 64;

			retVal = TZMON_KDF(key, keyLen, salt, saltLen,
					outData, *outDataLen);
			CHECK(retVal, "TZMON_KDF", return retVal;);
			break;
		}
		case TA_TZMON_CMD_FILE_WRITE:
		{
			uint8_t *data = sharedMem->inData;
			uint32_t dataLen = sharedMem->inDataLen;
			uint8_t fileName[8 + 1] = {
				0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08,
				0x00
			};
			uint32_t fileNameLen = strlen((const char*)fileName);

			retVal = TZMON_WriteFile(fileName, fileNameLen, data, dataLen);
			CHECK(retVal, "TZMON_FileWrite", return retVal;);
			break;
		}
		case TA_TZMON_CMD_FILE_READ:
		{
			uint8_t fileName[8 + 1] = {
				0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08,
				0x00
			};
			uint32_t fileNameLen = strlen((const char*)fileName);
			*outDataLen = DATA_SIZE;

			retVal = TZMON_ReadFile(fileName, fileNameLen,
					outData, outDataLen);
			CHECK(retVal, "TZMON_ReadFile", return retVal;);
			break;
		}
		case TA_TZMON_CMD_FILE_DELETE:
		{
			uint8_t fileName[8 + 1] = {
				0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08,
				0x00
			};
			uint32_t fileNameLen = strlen((const char*)fileName);
			
			retVal = TZMON_DeleteFile(fileName, fileNameLen);
			CHECK(retVal, "TZMON_DeleteFile", return retVal;);
			break;
		}
		case TA_TZMON_ADMIN_CMD_ABUSING_WRITE:
		{
			*outDataLen = 1;
			break;
		}
		case TA_TZMON_ADMIN_CMD_ABUSING_DELETE:
		{
			*outDataLen = 1;
			break;
		}
		case TA_TZMON_ADMIN_CMD_APPPREHASH_WRITE:
		{
			uint8_t *appHash = sharedMem->inData;
			uint8_t *pwd = sharedMem->iv;
			uint32_t appHashLen = sharedMem->inDataLen;
			uint32_t pwdLen = sharedMem->ivLen;

			if (pwdLen != strlen(ADMIN_PWD) ||
					memcmp(pwd, ADMIN_PWD, pwdLen) != 0) {
				retVal = TEE_ERROR_ACCESS_DENIED;
			} else {
				retVal = TZMON_WriteFile((uint8_t *)APP_PRE_HASH,
						(uint32_t)strlen(APP_PRE_HASH), appHash, appHashLen);
				CHECK(retVal, "TZMON_WriteFile", return retVal;);
			}
			break;
		}
		case TA_TZMON_ADMIN_CMD_MKEY_WRITE:
		{
			uint8_t *pwd = sharedMem->iv;
			uint32_t pwdLen = sharedMem->ivLen;
			uint8_t mKey[32 + 1] = { 0x00, };
			uint32_t mKeyLen = 32;

			if (pwdLen != strlen(ADMIN_PWD) ||
					memcmp(pwd, ADMIN_PWD, pwdLen) != 0) {
				retVal = TEE_ERROR_ACCESS_DENIED;
			} else {
				retVal = TZMON_Random(mKey, mKeyLen);
				CHECK(retVal, "TZMON_Random", return retVal;);

				retVal = TZMON_WriteFile((uint8_t *)MKEY_NAME,
						(uint32_t)strlen(MKEY_NAME), mKey, mKeyLen);
				CHECK(retVal, "TZMON_WriteFile", return retVal;);
			}
			break;
		}
		case TA_TZMON_ADMIN_CMD_APPPREHASH_READ:
		{
			uint8_t *pwd = sharedMem->iv;
			uint32_t pwdLen = sharedMem->ivLen;

			if (pwdLen != strlen(ADMIN_PWD) ||
					memcmp(pwd, ADMIN_PWD, pwdLen) != 0) {
				retVal = TEE_ERROR_ACCESS_DENIED;
			} else {
				*outDataLen = DATA_SIZE;
				retVal = TZMON_ReadFile((uint8_t *)APP_PRE_HASH, 
						(uint32_t)strlen(APP_PRE_HASH), outData, outDataLen);
				CHECK(retVal, "TZMON_ReadFile", return retVal;);
			}
			break;
		}
		case TA_TZMON_ADMIN_CMD_MKEY_READ:
		{
			uint8_t *pwd = sharedMem->iv;
			uint32_t pwdLen = sharedMem->ivLen;

			if (pwdLen != strlen(ADMIN_PWD) ||
					memcmp(pwd, ADMIN_PWD, pwdLen) != 0) {
				retVal = TEE_ERROR_ACCESS_DENIED;
			} else {
				*outDataLen = DATA_SIZE;
				retVal = TZMON_ReadFile((uint8_t *)MKEY_NAME, 
						(uint32_t)strlen(MKEY_NAME), outData, outDataLen);
				CHECK(retVal, "TZMON_WriteFile", return retVal;);
			}
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
