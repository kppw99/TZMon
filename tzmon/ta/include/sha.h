#ifndef __SHA_H__
#define __SHA_H__

#include "tzmon_common.h"

TEE_Result TZMON_SHA256(uint8_t *inData, uint32_t inDataLen,
						uint8_t *outData, uint32_t *outDataLen);

#endif //__SHA_H__
