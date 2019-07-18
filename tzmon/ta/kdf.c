#include "kdf.h"
#include "hmac.h"

#define MIN_PWD_SIZE		(1)
#define MAX_PWD_SIZE		(32)
#define MIN_SALT_SIZE		(1)
#define MAX_SALT_SIZE		(32)
#define MIN_ITERATION_SIZE	(1)
#define MAX_ITERATION_SIZE	(20)
#define MAX_KDF_SIZE		(64)

#define HMAC_OUT_DATA_SIZE	(32)
#define KDF_F_OUT_DATA_SIZE	(32)
#define KDF_DATA_SIZE			(32)
#define COUNT_BUF_SIZE		(4)

static TEE_Result write32_be(uint32_t counter, uint8_t *countBuf,
		uint32_t countBufLen)
{
	if (countBuf == NULL || countBufLen != COUNT_BUF_SIZE) {
		return TEE_ERROR_BAD_PARAMETERS;
	}

	countBuf[0] = (counter >> 24)   & 0xff;
	countBuf[1] = (counter >> 16)   & 0xff;
	countBuf[2] = (counter >> 8)    & 0xff;
	countBuf[3] = (counter >> 0)    & 0xff;

	return TEE_SUCCESS;
}

static TEE_Result kdf_memcat(uint8_t *first, uint32_t firstLen,
		uint8_t *second, uint32_t secondLen,
		uint8_t *out, uint32_t *outLen)
{
	if (out == NULL || outLen == NULL || *outLen < (firstLen + secondLen)) {
		return TEE_ERROR_BAD_PARAMETERS;
	}

	if (firstLen != 0 && first != NULL) {
		memcpy(out, first, firstLen);
	}
	if (secondLen != 0 && second != NULL) {
		memcpy(out + firstLen, second, secondLen); 
	}

	*outLen = firstLen + secondLen;

	return TEE_SUCCESS;
}

static TEE_Result kdf_f(uint8_t *pwd, uint32_t pwdLen,
				uint8_t *salt, uint32_t saltLen,
				uint8_t *outData, uint32_t *outDataLen,
				uint32_t counter, uint32_t iteration)
{
	TEE_Result retVal;

	uint8_t initData[KDF_DATA_SIZE + COUNT_BUF_SIZE + 1] = { 0x00, };
	uint8_t countBuf[COUNT_BUF_SIZE] = { 0x00, };
	uint8_t tmp[HMAC_OUT_DATA_SIZE + 1] = { 0x00, };
	uint8_t tmp2[HMAC_OUT_DATA_SIZE + 1] = { 0x00, };

	uint32_t initDataLen, i, j;

	if (pwdLen < MIN_PWD_SIZE || pwdLen > MAX_PWD_SIZE ||
		saltLen < MIN_SALT_SIZE || saltLen > MAX_SALT_SIZE ||
		iteration < MIN_ITERATION_SIZE || iteration > MAX_ITERATION_SIZE) {
		return TEE_ERROR_BAD_PARAMETERS;
	}

	if (pwd == NULL || salt == NULL || outData == NULL ||
			outDataLen == NULL) {
		return TEE_ERROR_BAD_PARAMETERS;
	}

	if (*outDataLen != HMAC_OUT_DATA_SIZE) {
		return TEE_ERROR_BAD_PARAMETERS;
	}

	retVal = write32_be(counter, countBuf, COUNT_BUF_SIZE);
	CHECK(retVal, "write32_be", return retVal;);

	initDataLen = sizeof(initData);
	retVal = kdf_memcat(salt, saltLen, countBuf, sizeof(countBuf),
			initData, &initDataLen);
	CHECK(retVal, "kdf_memcat", return retVal;);

	retVal = TZMON_HMAC_SHA256(pwd, pwdLen, initData, initDataLen,
			tmp, outDataLen);
	CHECK(retVal, "TZMON_HMAC_SHA256", return retVal;);
	memcpy(outData, tmp, *outDataLen);
	
	for (i = 1; i < iteration; i++) {
		retVal = TZMON_HMAC_SHA256(pwd, pwdLen, tmp, HMAC_OUT_DATA_SIZE,
				tmp2, outDataLen);
		CHECK(retVal, "TZMON_HMAC_SHA256", return retVal;);

		for (j = 0; j < HMAC_OUT_DATA_SIZE; j++) {
			outData[j] ^= tmp2[j];
		}

		memcpy(tmp, tmp2, *outDataLen);
	}

	return TEE_SUCCESS;
}

// TZMON_KDF is simple KDF function based on pbkdf2 algorithm.
// This function use HMAC_SHA256 as PRF and support big-endian.
TEE_Result TZMON_KDF(uint8_t *pwd, uint32_t pwdLen,
					uint8_t *salt, uint32_t saltLen,
					uint8_t *dKey, uint32_t dKeyLen)
{
	TEE_Result retVal;

	uint8_t tmp[KDF_F_OUT_DATA_SIZE + 1] = { 0x00, };

	uint32_t n, i;
	uint32_t iteration = 10;
	uint32_t outLen = 0;
	uint32_t tmpLen = KDF_F_OUT_DATA_SIZE;

	if (pwdLen == 0 || saltLen == 0 || dKeyLen == 0) {
		return TEE_ERROR_BAD_PARAMETERS;
	}

	if (pwd == NULL || salt == NULL || dKey == NULL) {
		return TEE_ERROR_BAD_PARAMETERS;
	}

	if (dKeyLen % KDF_F_OUT_DATA_SIZE != 0 ||
			dKeyLen > MAX_KDF_SIZE) {
		return TEE_ERROR_BAD_PARAMETERS;
	}

	n = dKeyLen / KDF_F_OUT_DATA_SIZE;

	for (i = 1; i <= n; i++) {
		retVal = kdf_f(pwd, pwdLen, salt, saltLen, tmp, &tmpLen, i,
				iteration);
		CHECK(retVal, "kdf_f", return retVal;);
		memcpy(dKey + outLen, tmp, tmpLen);
		outLen += tmpLen;
	}

	if (outLen != dKeyLen) {
		return TEE_ERROR_GENERIC;
	}

	return TEE_SUCCESS;
}

TEE_Result TZMON_XOR(uint8_t *first, uint32_t firstLen,
					uint8_t *second, uint32_t secondLen,
					uint8_t *outData, uint32_t outDataLen)
{
	uint32_t i;

	if (firstLen == 0 || secondLen == 0 || outDataLen == 0) {
		return TEE_ERROR_BAD_PARAMETERS;
	}

	if (first == NULL || second == NULL || outData == NULL) {
		return TEE_ERROR_BAD_PARAMETERS;
	}

	if (firstLen != secondLen || firstLen != outDataLen) {
		return TEE_ERROR_BAD_PARAMETERS;
	}

	for (i = 0; i < outDataLen; i++) {
		outData[i] = first[i] ^ second[i];
	}

	return TEE_SUCCESS;
}
