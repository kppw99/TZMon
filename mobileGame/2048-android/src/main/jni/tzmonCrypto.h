#ifndef BLOCKINGER_TZMONCRYPTO_H
#define BLOCKINGER_TZMONCRYPTO_H

bool tzmon_xor(unsigned char *first, int firstLen, unsigned char *second, int secondLen,
                    unsigned char *out, int outLen);
bool tzmon_sha256(unsigned char *data, int dataLen, unsigned char *hash, int *hashLen);
bool tzmon_hmac_sha256(unsigned char *key, int keyLen, unsigned char *data, int dataLen,
                            unsigned char *hmac, int *hmacLen);
void generate_appHash(const char *path, char *appHash);

#endif //BLOCKINGER_TZMONCRYPTO_H
