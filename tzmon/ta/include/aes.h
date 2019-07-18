#ifndef __AES_H__
#define __AES_H__

#include "tzmon_common.h"

TEE_Result TZMON_AES256_ENC(uint8_t *inData, uint32_t inDataLen,
                        uint8_t *key, uint32_t keyLen,
                        uint8_t *iv, uint32_t ivLen,
                        uint8_t *outData, uint32_t *outDataLen);

TEE_Result TZMON_AES256_DEC(uint8_t *inData, uint32_t inDataLen,
                        uint8_t *key, uint32_t keyLen,
                        uint8_t *iv, uint32_t ivLen,
                        uint8_t *outData, uint32_t *outDataLen);

#endif // __AES_H__
