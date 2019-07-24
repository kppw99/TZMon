#ifndef BLOCKINGER_TZMONTEEC_H
#define BLOCKINGER_TZMONTEEC_H

#define TA_TZMON_UUID \
{ 0xaaaaf200, 0x2450, 0x11e4, { 0xab, 0xe2, 0x00, 0x02, 0xa5, 0xd5, 0xc5, 0x10 } }

#define TA_TZMON_CMD_INIT_FLAG              (0x00)

#define TA_TZMON_CMD_IKEY           (0x11)
#define TA_TZMON_CMD_ITOKEN         (0x12)
#define TA_TZMON_CMD_IVERIFY        (0x13)

#define TA_TZMON_CMD_UKEY           (0x41)
#define TA_TZMON_CMD_UTOKEN         (0x42)
#define TA_TZMON_CMD_UVERIFY        (0x43)

#define TA_TZMON_CMD_APRETOKEN      (0x51)
#define TA_TZMON_CMD_ATOKEN         (0x52)
#define TA_TZMON_CMD_AVERIFY        (0x53)

#define TA_TZMON_CMD_TPRETOKEN      (0x61)
#define TA_TZMON_CMD_TTOKEN         (0x62)
#define TA_TZMON_CMD_TVERIFY        (0x63)

#define TA_TZMON_CMD_HPRETOKEN      (0x71)
#define TA_TZMON_CMD_HKEY           (0x72)

#define DATA_SIZE   (512 + 1)
#define KEY_SIZE    (32 + 1)
#define IV_SIZE     (16 + 1)

typedef struct _sharedMem {
    uint8_t     inData[DATA_SIZE];
    uint32_t    inDataLen;
    uint8_t     iv[IV_SIZE];
    uint32_t    ivLen;
    uint8_t     outData[DATA_SIZE];
    uint32_t    outDataLen;
    double     tGap;
}SharedMem;

#endif //BLOCKINGER_TZMONTEEC_H
