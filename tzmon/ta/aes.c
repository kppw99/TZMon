#include "aes.h"

#define MIN_KEY_SIZE	(1)
#define MAX_KEY_SIZE	(32)
#define MIN_INPUT_SIZE	(1)
#define MAX_INPUT_SIZE	(64)
#define IV_SIZE			(16)

static TEE_Attribute attr = { 0x00, };
static TEE_ObjectHandle key_handle = TEE_HANDLE_NULL;
static TEE_OperationHandle op_handle = TEE_HANDLE_NULL;

static TEE_Result aes_init(const uint8_t *key, const uint32_t keyLen,
		const uint8_t *iv, const uint32_t ivLen, const uint32_t mode)
{
	TEE_Result retVal;

	if (keyLen < MIN_KEY_SIZE || keyLen > MAX_KEY_SIZE ||
			ivLen != IV_SIZE || iv == NULL || key == NULL) {
		return TEE_ERROR_BAD_PARAMETERS;
	}

	if (mode != TEE_MODE_ENCRYPT && mode != TEE_MODE_DECRYPT) {
		return TEE_ERROR_BAD_PARAMETERS;
	}

	if (op_handle != TEE_HANDLE_NULL) {
		TEE_FreeOperation(op_handle);
	}

	retVal = TEE_AllocateOperation(&op_handle, TEE_ALG_AES_CBC_NOPAD,
			mode, keyLen * 8); 
	CHECK(retVal, "TEE_AllocateOperation", return retVal;);

	if (key_handle != TEE_HANDLE_NULL) {
		TEE_FreeTransientObject(key_handle);
	}

	retVal = TEE_AllocateTransientObject(TEE_TYPE_AES, keyLen * 8,
			&key_handle);
	CHECK(retVal, "TEE_AllocateTransientObject", return retVal;);

	TEE_InitRefAttribute(&attr, TEE_ATTR_SECRET_VALUE, key, keyLen);

	retVal = TEE_PopulateTransientObject(key_handle, &attr, 1);
	CHECK(retVal, "TEE_PopulateTransientObject", return retVal;);

	retVal = TEE_SetOperationKey(op_handle, key_handle);
	CHECK(retVal, "TEE_SetOperationKey", return retVal;);

	TEE_CipherInit(op_handle, iv, ivLen);

	return TEE_SUCCESS;
}

static TEE_Result aes_do(const uint8_t *inData, const uint32_t inDataLen,
		uint8_t *outData, uint32_t *outDataLen)
{
	TEE_Result retVal;

	if (inDataLen < MIN_INPUT_SIZE || inDataLen > MAX_INPUT_SIZE ||
			inData == NULL || outData == NULL || outDataLen == NULL) {
		return TEE_ERROR_BAD_PARAMETERS;
	}

	retVal = TEE_CipherUpdate(op_handle, inData, inDataLen,
			outData, outDataLen);
	CHECK(retVal, "TEE_CipherUpdate", return retVal;);

	return retVal;
}

static void aes_final(void)
{
	if (key_handle != TEE_HANDLE_NULL) {
		TEE_FreeTransientObject(key_handle);
	}

	if (op_handle != TEE_HANDLE_NULL) {
		TEE_FreeOperation(op_handle);
	}
}

TEE_Result TZMON_AES256_ENC(uint8_t *inData, uint32_t inDataLen,
                        uint8_t *key, uint32_t keyLen,
                        uint8_t *iv, uint32_t ivLen,
                        uint8_t *outData, uint32_t *outDataLen)
{
    TEE_Result retVal;

    if (inDataLen == 0 || keyLen == 0 || ivLen == 0 ||
            inData == NULL || key == NULL || iv == NULL ||
            outData == NULL || outDataLen == NULL) {
        return TEE_ERROR_BAD_PARAMETERS;
    }

    retVal = aes_init(key, keyLen, iv, ivLen, TEE_MODE_ENCRYPT);
    CHECK(retVal, "aes_init", return retVal;);

    retVal = aes_do(inData, inDataLen, outData, outDataLen);
    CHECK(retVal, "aes_do", return retVal;);

    aes_final();

    return TEE_SUCCESS;
}

TEE_Result TZMON_AES256_DEC(uint8_t *inData, uint32_t inDataLen,
                        uint8_t *key, uint32_t keyLen,
                        uint8_t *iv, uint32_t ivLen,
                        uint8_t *outData, uint32_t *outDataLen)
{
    TEE_Result retVal;

    if (inDataLen == 0 || keyLen == 0 || ivLen == 0 ||
            inData == NULL || key == NULL || iv == NULL ||
            outData == NULL || outDataLen == NULL) {
        return TEE_ERROR_BAD_PARAMETERS;
    }

    retVal = aes_init(key, keyLen, iv, ivLen, TEE_MODE_DECRYPT);
    CHECK(retVal, "aes_init", return retVal;);

    retVal = aes_do(inData, inDataLen, outData, outDataLen);
    CHECK(retVal, "aes_do", return retVal;);

    aes_final();

    return TEE_SUCCESS;
}

