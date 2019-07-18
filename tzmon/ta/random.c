#include "random.h"

#define MIN_OUTPUT_SIZE	(1)
#define MAX_OUTPUT_SIZE	(64)

static TEE_Result random_generate(uint8_t *outData, uint32_t outDataLen)
{
	if (outDataLen < MIN_OUTPUT_SIZE || outDataLen > MAX_OUTPUT_SIZE || 
			outData == NULL) {
		return TEE_ERROR_BAD_PARAMETERS;
	}

	TEE_GenerateRandom(outData, outDataLen);

	return TEE_SUCCESS;
}

TEE_Result TZMON_Random(uint8_t *random, uint32_t randomLen)
{
	TEE_Result retVal;

	if (random == NULL || randomLen == 0) {
		return TEE_ERROR_BAD_PARAMETERS;
	}

	retVal = random_generate(random, randomLen);
	CHECK(retVal, "random_generate", return retVal;);

	return TEE_SUCCESS;
}

