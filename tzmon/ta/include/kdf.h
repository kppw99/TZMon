#ifndef __KDF_H__
#define __KDF_H__

#include "tzmon_common.h"

TEE_Result TZMON_XOR(uint8_t *first, uint32_t firstLen,
					uint8_t *second, uint32_t secondLen,
					uint8_t *outData, uint32_t outDataLen);

TEE_Result TZMON_KDF(uint8_t *pwd, uint32_t pwdLen,
					uint8_t *salt, uint32_t saltLen,
					uint8_t *dKey, uint32_t dKeyLen);

#endif // __KDF_H__
