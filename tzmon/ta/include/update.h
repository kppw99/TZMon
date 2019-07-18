#ifndef __UPDATE_H__
#define __UPDATE_H__

#include "tzmon_common.h"

TEE_Result TZMON_ConnectServer(uint8_t *buffer, uint32_t bufferLen, uint8_t *outData,
		uint32_t *outDataLen);

#endif // __UPDATE_H__
