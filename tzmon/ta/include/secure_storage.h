#ifndef __SECURE_STORAGE_H__
#define __SECURE_STORAGE_H__

#include "tzmon_common.h"

TEE_Result TZMON_DeleteFile(uint8_t *fileName, uint32_t fileNameLen);
TEE_Result TZMON_WriteFile(uint8_t *fileName, uint32_t fileNameLen,
		uint8_t *data, uint32_t dataLen);
TEE_Result TZMON_ReadFile(uint8_t *fileName, uint32_t fileNameLen,
		uint8_t *data, uint32_t *dataLen);

#endif // __SECURE_STORAGE_H__
