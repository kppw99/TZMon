#ifndef __PARSE_CMD_H__
#define __PARSE_CMD_H__

#include "tzmon_common.h"

#define USE_TEST_DATA		(1)

TEE_Result TZMON_ParseCMD(uint32_t cmdID, TEE_Param params[4]);

#endif // __PARSE_CMD_H__
