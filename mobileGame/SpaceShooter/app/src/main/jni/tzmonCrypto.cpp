#include <android/log.h>    // for android log function
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include "openssl/sha.h"    // for SHA function of openSSL
#include "openssl/hmac.h"   // for HMAC function of openSSL

#include "tzmonCrypto.h"
#include "tzmonUtil.h"

bool tzmon_xor(unsigned char *first, int firstLen, unsigned char *second, int secondLen,
                    unsigned char *out, int outLen)
{
    if (first == NULL || second == NULL || out == NULL) {
        LOGD("Bad Parameter");
        return false;
    }

    if (outLen != firstLen || outLen != secondLen) {
        LOGD("Bad Parameter");
        return false;
    }

    for (int i = 0; i < outLen; i++) {
        out[i] = first[i] ^ second[i];
    }

    return true;
}

bool tzmon_sha256(unsigned char *data, int dataLen, unsigned char *hash, int *hashLen)
{
    SHA256_CTX sha256;

    if (dataLen == 0 || data == NULL || hash == NULL || hashLen == NULL) {
        LOGD("Bad parameter");
        return false;
    }

    SHA256_Init(&sha256);
    SHA256_Update(&sha256, data, dataLen);
    SHA256_Final(hash, &sha256);

    *hashLen = SHA256_DIGEST_LENGTH;

    return true;
}

bool tzmon_hmac_sha256(unsigned char *key, int keyLen, unsigned char *data, int dataLen,
                            unsigned char *hmac, int *hmacLen)
{

    HMAC_CTX hctx;
    const EVP_MD *evpmd;

    if (keyLen == 0 || dataLen == 0 || key == NULL || data == NULL ||
            hmac == NULL || hmacLen == NULL) {
        LOGD("Bad parameter");
        return false;
    }

    OpenSSL_add_all_digests();
    evpmd = EVP_get_digestbyname("sha256");

    HMAC_Init(&hctx, key, keyLen, evpmd);
    HMAC_Update(&hctx, data, dataLen);
    HMAC_Final(&hctx, hmac, (unsigned int *)hmacLen);

    HMAC_CTX_cleanup(&hctx);

    return true;
}

void generate_appHash(const char *path, char *appHash)
{
    FILE *fp = fopen(path, "r");
    SHA256_CTX sha256;

    char input_data[2048] = { 0x00, };
    unsigned char hash[SHA256_DIGEST_LENGTH] = { 0x00, };
    char hash_string[SHA256_DIGEST_LENGTH * 2 + 1] = { 0x00, };

    SHA256_Init(&sha256);

    while (fgets(input_data, sizeof(input_data), fp) != NULL) {
        SHA256_Update(&sha256, input_data, sizeof(input_data));
        memset(input_data, 0x00, sizeof(input_data));
    }

    SHA256_Final(hash, &sha256);

    for (int i = 0; i < SHA256_DIGEST_LENGTH; i++) {
        sprintf(&hash_string[i*2], "%02x", (unsigned int)hash[i]);
    }

    memcpy(appHash, hash_string, sizeof(hash_string));
    fclose(fp);
}
