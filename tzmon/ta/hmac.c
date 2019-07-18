#include "hmac.h"

#define MIN_KEY_SIZE	(1)
#define MAX_KEY_SIZE	(32)
#define MIN_INPUT_SIZE	(1)
#define MAX_INPUT_SIZE	(64)

static TEE_Attribute attr = { 0x00, };
static TEE_ObjectHandle key_handle = TEE_HANDLE_NULL;
static TEE_OperationHandle op_handle = TEE_HANDLE_NULL;

static TEE_Result hmac_setKey(uint8_t *key, uint32_t keyLen)
{
	TEE_Result retVal;

	if (keyLen < MIN_KEY_SIZE || keyLen > MAX_KEY_SIZE ||
			key == NULL) {
		return TEE_ERROR_BAD_PARAMETERS;
	}

	retVal = TEE_AllocateOperation(&op_handle, TEE_ALG_HMAC_SHA256,
			TEE_MODE_MAC, keyLen * 8);
	CHECK(retVal, "TEE_AllocateOperation", return retVal;);

	retVal = TEE_AllocateTransientObject(TEE_TYPE_HMAC_SHA256,
			keyLen * 8, &key_handle);
	CHECK(retVal, "TEE_AllocateTransientObject", return retVal;);

	TEE_InitRefAttribute(&attr, TEE_ATTR_SECRET_VALUE, key, keyLen);

	retVal = TEE_PopulateTransientObject(key_handle, &attr, 1);
	CHECK(retVal, "TEE_PopulateTransientObject", return retVal;);
	
	retVal = TEE_SetOperationKey(op_handle, key_handle);
	CHECK(retVal, "TEE_SetOperationKey", return retVal;);
	
	return TEE_SUCCESS;
}

static void hmac_releaseKey(void)
{
	if (key_handle != TEE_HANDLE_NULL) {
		TEE_FreeTransientObject(key_handle);
	}

	if (op_handle != TEE_HANDLE_NULL) {
		TEE_FreeOperation(op_handle);
	}
}

static void hmac_init(void)
{
	TEE_MACInit(op_handle, NULL, 0x00);
}

static TEE_Result hmac_update(uint8_t *inData, uint32_t inDataLen)
{
	if (inDataLen < MIN_INPUT_SIZE || inDataLen > MAX_INPUT_SIZE ||
			inData == NULL) {
		return TEE_ERROR_BAD_PARAMETERS;
	}

	TEE_MACUpdate(op_handle, inData, inDataLen);

	return TEE_SUCCESS;
}

static TEE_Result hmac_final(uint8_t *outData, uint32_t *outDataLen)
{
	TEE_Result retVal;

	if (outData == NULL || outDataLen == NULL) {
		return TEE_ERROR_BAD_PARAMETERS;
	}

	retVal = TEE_MACComputeFinal(op_handle, NULL, 0x00, outData, outDataLen);
	CHECK(retVal, "TEE_MACComputeFinal", return retVal;);

	return TEE_SUCCESS;
}

TEE_Result TZMON_HMAC_SHA256(uint8_t *key, uint32_t keyLen,
							uint8_t *inData, uint32_t inDataLen,
							uint8_t *outData, uint32_t *outDataLen)
{
	TEE_Result retVal;

	if (inDataLen == 0 || keyLen == 0 ||
			inData == NULL || key == NULL ||
			outData == NULL || outDataLen == NULL) {
		return TEE_ERROR_BAD_PARAMETERS;
	}

	retVal = hmac_setKey(key, keyLen);
	CHECK(retVal, "hmac_setKey", return retVal;);

	hmac_init();

	retVal = hmac_update(inData, inDataLen);
	CHECK(retVal, "hmac_update", return retVal;);

	retVal = hmac_final(outData, outDataLen);
	CHECK(retVal, "hmac_releaseKey", return retVal;);

	hmac_releaseKey();

	return TEE_SUCCESS;
}

