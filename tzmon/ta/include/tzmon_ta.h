/*
 * Copyright (c) 2016-2017, Linaro Limited
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
#ifndef TA_TZMON_H
#define TA_TZMON_H

/*
 * This UUID is generated with uuidgen
 * the ITU-T UUID generator at http://www.itu.int/ITU-T/asn1/uuid.html
 */
#define TA_TZMON_UUID \
	{ 0xaaaaf200, 0x2450, 0x11e4, \
		{ 0xab, 0xe2, 0x00, 0x02, 0xa5, 0xd5, 0xc5, 0x10} }

/* The function IDs implemented in this TA */
#define TA_TZMON_CMD_IKEY			(0x11)
#define TA_TZMON_CMD_ITOKEN			(0x12)
#define TA_TZMON_CMD_IVERIFY		(0x13)	

#define TA_TZMON_CMD_SHA256			(0x21)
#define TA_TZMON_CMD_HMAC_SHA256	(0x22)
#define TA_TZMON_CMD_RANDOM			(0x23)
#define TA_TZMON_CMD_AES256_ENC		(0x24)
#define TA_TZMON_CMD_AES256_DEC		(0x25)
#define TA_TZMON_CMD_KDF			(0x26)

#define TA_TZMON_CMD_FILE_WRITE		(0x31)
#define TA_TZMON_CMD_FILE_READ		(0x32)
#define TA_TZMON_CMD_FILE_DELETE	(0x33)

#define TA_TZMON_CMD_UKEY			(0x41)
#define TA_TZMON_CMD_UTOKEN			(0x42)
#define TA_TZMON_CMD_UVERIFY		(0x43)

#define TA_TZMON_CMD_APRETOKEN		(0x51)
#define TA_TZMON_CMD_ATOKEN			(0x52)
#define TA_TZMON_CMD_AVERIFY		(0x53)

#define TA_TZMON_CMD_TPRETOKEN		(0x61)
#define TA_TZMON_CMD_TTOKEN			(0x62)
#define TA_TZMON_CMD_TVERIFY		(0x63)

#define TA_TZMON_CMD_INIT_FLAG				(0x00)
#define TA_TZMON_ADMIN_CMD_MKEY_WRITE		(0x01)
#define TA_TZMON_ADMIN_CMD_MKEY_READ		(0x02)
#define TA_TZMON_ADMIN_CMD_APPPREHASH_WRITE	(0x03)
#define TA_TZMON_ADMIN_CMD_APPPREHASH_READ	(0x04)
#define TA_TZMON_ADMIN_CMD_ABUSING_WRITE	(0x05)
#define TA_TZMON_ADMIN_CMD_ABUSING_DELETE	(0x06)

#define DATA_SIZE	(512 + 1)
#define KEY_SIZE	(32 + 1)
#define IV_SIZE		(16 + 1)

typedef struct _sharedMem {
	uint8_t		inData[DATA_SIZE];
	uint32_t	inDataLen;
	uint8_t		iv[IV_SIZE];
	uint32_t	ivLen;
	uint8_t		outData[DATA_SIZE];
	uint32_t	outDataLen;
	double		tGap;
}SharedMem;

#endif /*TA_TZMON_H*/
