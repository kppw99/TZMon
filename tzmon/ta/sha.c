#include "sha.h"

#define MIN_INPUT_SIZE	(1)
#define MAX_INPUT_SIZE	(64)

static TEE_OperationHandle digest_op = NULL;

static TEE_Result sha_init(void)
{
	TEE_Result retVal;

	if (digest_op) {
		TEE_FreeOperation(digest_op);
	}

	retVal = TEE_AllocateOperation(&digest_op, TEE_ALG_SHA256,
			TEE_MODE_DIGEST, 0x00);
	CHECK(retVal, "TEE_AllocateOperation", return retVal;);

	return TEE_SUCCESS;
}

static TEE_Result sha_process(uint8_t *inData, uint32_t inDataLen,
					uint8_t *outData, uint32_t *outDataLen)
{
	TEE_Result retVal;

	if (inDataLen < MIN_INPUT_SIZE || inDataLen > MAX_INPUT_SIZE ||
			inData == NULL || outData == NULL || outDataLen == NULL) {
		return TEE_ERROR_BAD_PARAMETERS;
	}

	retVal = TEE_DigestDoFinal(digest_op, inData, inDataLen,
			outData, outDataLen);
	CHECK(retVal, "TEE_DigestDoFinal", return retVal;);

	return TEE_SUCCESS;
}

static void sha_done(void)
{
	if (digest_op) {
		TEE_FreeOperation(digest_op);
	}
}

TEE_Result TZMON_SHA256(uint8_t *inData, uint32_t inDataLen,
						uint8_t *outData, uint32_t *outDataLen)
{
	TEE_Result retVal;

	if (inDataLen == 0 || inData == NULL || outData == NULL ||
			outDataLen == NULL) {
		return TEE_ERROR_BAD_PARAMETERS;
	}

	retVal = sha_init();
	CHECK(retVal, "sha_init", return retVal;);

	retVal = sha_process(inData, inDataLen, outData, outDataLen);
	CHECK(retVal, "sha_process", return retVal;);

	sha_done();

	return TEE_SUCCESS;
}

