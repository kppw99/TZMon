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

#define TZMON_IKEY		(0x00)
#define TZMON_ITOKEN	(0x01)
#define TZMON_IFLAG		(0x02)

#define TZMON_UKEY		(0x10)
#define TZMON_UTOKEN	(0x11)
#define TZMON_UFLAG		(0x12)

#define TZMON_APREFLAG	(0x21)
#define TZMON_ATOKEN	(0x22)
#define TZMON_AFLAG		(0x23)

#define TZMON_TPREFLAG	(0x31)
#define TZMON_TTOKEN	(0x32)
#define TZMON_TFLAG		(0x33)

#define TZMON_HPREFLAG	(0x40)

#endif // __TZMON_COMMON_H__
