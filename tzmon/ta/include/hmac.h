#ifndef __HMAC_H__
#define __HMAC_H__

#include "tzmon_common.h"

TEE_Result TZMON_HMAC_SHA256(uint8_t *key, uint32_t keyLen,
							uint8_t *inData, uint32_t inDataLen,
							uint8_t *outData, uint32_t *outDataLen);

#endif // __HMAC_H__
