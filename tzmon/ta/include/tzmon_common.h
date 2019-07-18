#ifndef __TZMON_COMMON_H__
#define __TZMON_COMMON_H__

#include <tee_internal_api.h>
#include <tee_ta_api.h>
#include <string.h>

#define CHECK(res, name, action) do {   \
	if ((res) != TEE_SUCCESS) {         \
		DMSG(name ": 0x%08x", (res));   \
		action                          \
	}                                   \
} while(0)

#endif // __TZMON_COMMON_H__
