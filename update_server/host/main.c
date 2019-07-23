#include <sys/types.h>
#include <sys/stat.h>
#include <sys/socket.h>
#include <sys/ioctl.h>
#include <sys/utsname.h>
#include <unistd.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <net/if.h>
#include <netdb.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <strings.h>

#include "aes.h"
#include "sha.h"

#define DEBUG_ENABLE	(0)

#if (DEBUG_ENABLE)
#define ANSI_COLOR "\x1b[32m"
#define ANSI_COLOR_RESET "\x1b[0m"
#define DEBUG_PREFIX ANSI_COLOR "[UpdateServer] "
#define DEBUG_NOPREFIX ANSI_COLOR
#define LOGD(msg, ...) fprintf(stderr, DEBUG_PREFIX msg "\n" ANSI_COLOR_RESET, \
##__VA_ARGS__)
#define LOGN(msg, ...) fprintf(stderr, DEBUG_NOPREFIX msg ANSI_COLOR_RESET, \
##__VA_ARGS__)
#else
#define LOGD(...)
#define LOGN(...)
#endif

#define CMD "adb shell /vendor/bin/optee_tzmon"

#if defined(AES256)
static uint8_t iv[]  = {
	0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07,
	0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f
};

static uint8_t key[] = {
	0x60, 0x3d, 0xeb, 0x10, 0x15, 0xca, 0x71, 0xbe,
	0x2b, 0x73, 0xae, 0xf0, 0x85, 0x7d, 0x77, 0x81,
	0x1f, 0x35, 0x2c, 0x07, 0x3b, 0x61, 0x08, 0xd7,
	0x2d, 0x98, 0x10, 0xa3, 0x09, 0x14, 0xdf, 0xf4
};

static uint8_t plain[]  = {
	0x6b, 0xc1, 0xbe, 0xe2, 0x2e, 0x40, 0x9f, 0x96,
	0xe9, 0x3d, 0x7e, 0x11, 0x73, 0x93, 0x17, 0x2a,
    0xae, 0x2d, 0x8a, 0x57, 0x1e, 0x03, 0xac, 0x9c,
	0x9e, 0xb7, 0x6f, 0xac, 0x45, 0xaf, 0x8e, 0x51,
	0x30, 0xc8, 0x1c, 0x46, 0xa3, 0x5c, 0xe4, 0x11,
	0xe5, 0xfb, 0xc1, 0x19, 0x1a, 0x0a, 0x52, 0xef,
    0xf6, 0x9f, 0x24, 0x45, 0xdf, 0x4f, 0x9b, 0x17,
	0xad, 0x2b, 0x41, 0x7b, 0xe6, 0x6c, 0x37, 0x10
};

static uint8_t cipher[] = {
	0xf5, 0x8c, 0x4c, 0x04, 0xd6, 0xe5, 0xf1, 0xba,
	0x77, 0x9e, 0xab, 0xfb, 0x5f, 0x7b, 0xfb, 0xd6,
	0x9c, 0xfc, 0x4e, 0x96, 0x7e, 0xdb, 0x80, 0x8d,
	0x67, 0x9f, 0x77, 0x7b, 0xc6, 0x70, 0x2c, 0x7d,
	0x39, 0xf2, 0x33, 0x69, 0xa9, 0xd9, 0xba, 0xcf,
	0xa5, 0x30, 0xe2, 0x63, 0x04, 0x23, 0x14, 0x61,
	0xb2, 0xeb, 0x05, 0xe2, 0xc3, 0x9b, 0xe9, 0xfc,
	0xda, 0x6c, 0x19, 0x07, 0x8c, 0x6a, 0x9d, 0x1b
};
#endif

static uint8_t file[] = {
	0x54, 0x5A, 0x4D, 0x6F, 0x6E, 0x20, 0x69, 0x73,
	0x20, 0x6F, 0x6E, 0x65, 0x20, 0x6F, 0x66, 0x20,
	0x74, 0x68, 0x65, 0x20, 0x62, 0x65, 0x73, 0x74,
	0x20, 0x73, 0x6F, 0x6C, 0x75, 0x74, 0x69, 0x6F,
	0x6E, 0x73, 0x20, 0x66, 0x6F, 0x72, 0x20, 0x61,
	0x20, 0x6D, 0x6F, 0x62, 0x69, 0x6C, 0x65, 0x20,
	0x67, 0x61, 0x6D, 0x65, 0x20, 0x61, 0x70, 0x70,
	0x6C, 0x69, 0x63, 0x61, 0x74, 0x69, 0x6F, 0x6E
};

static uint32_t fileLen = 64;

static uint8_t uKey[32] = { 0x00, };
static uint8_t uToken[32] = { 0x00, };

static void printBuf(uint8_t *title, uint8_t *buf, uint32_t bufLen)
{
	uint32_t i;

	if (bufLen == 0 || title == NULL || buf == NULL)	return;

	LOGD("%s(%d)", title, bufLen);

	for (i = 0; i < bufLen; i++) {
		if (i != 0 && i % 8 == 0)	LOGN("\n");
		LOGN("%02x ", buf[i]);
	}

	LOGN("\n\n");
}

static int printServerInfo(char *ifrName, int port)
{
    int sockfd, retVal = 0;
    struct ifreq ifr;
    char ipstr[40] = { 0x00, };

    if (port < 1000 || port > 10000) {
        LOGD("[%d]: The port value must be between 1,000 and 10,000.", port);
        return 1;
    }

    strncpy(ifr.ifr_name, ifrName, IFNAMSIZ);
    if ((sockfd = socket(AF_INET, SOCK_STREAM, 0)) < 0) {
        perror("[server]: socket error --> ");
        return 1;
    }

    if (ioctl(sockfd, SIOCGIFADDR, &ifr) < 0) {
        perror("[server]: ioctl error --> ");
        return 1;
    }

    inet_ntop(AF_INET, ifr.ifr_addr.sa_data + 2, ipstr, sizeof(struct sockaddr));
    close(sockfd);

    LOGD("=========================");
    LOGD("Server information");
    LOGD("[ip]    : %s", ipstr);
    LOGD("[port]  : %d", port);
    LOGD("=========================\n");

    return retVal;
}

static int _encAES256(uint8_t *iv, uint32_t ivLen, uint8_t *key, uint32_t keyLen,
		uint8_t *in, uint32_t inLen, uint8_t *out, uint32_t *outLen)
{
	int res = 0x00;
	uint8_t tempIn[128] = { 0x00, };

	if (ivLen == 0 || keyLen == 0 || inLen == 0)	return 1;
	if (iv == NULL || key == NULL || in == NULL || out == NULL || outLen == NULL)	return 1;
	if (ivLen != 16 || keyLen != 32)	return 1;
	if (inLen > sizeof(tempIn))		return 1;

	struct AES_ctx ctx;

	memcpy(tempIn, in, inLen);
	AES_init_ctx_iv(&ctx, (const unsigned char *)key, (const unsigned char *)iv);
	AES_CBC_encrypt_buffer(&ctx, tempIn, inLen);

	memcpy(out, tempIn, inLen);
	*outLen = inLen;

	return res;
}

static int _decAES256(uint8_t *iv, uint32_t ivLen, uint8_t *key, uint32_t keyLen,
		uint8_t *in, uint32_t inLen, uint8_t *out, uint32_t *outLen)
{
	int res = 0x00;
	uint8_t tempIn[128] = { 0x00, };

	if (ivLen == 0 || keyLen == 0 || inLen == 0)	return 1;
	if (iv == NULL || key == NULL || in == NULL || out == NULL || outLen == NULL)	return 1;
	if (ivLen != 16 || keyLen != 32)	return 1;
	if (inLen > sizeof(tempIn))		return 1;

	struct AES_ctx ctx;

	memcpy(tempIn, in, inLen);
	AES_init_ctx_iv(&ctx, (const unsigned char *)key, (const unsigned char *)iv);
	AES_CBC_decrypt_buffer(&ctx, tempIn, inLen);

	memcpy(out, tempIn, inLen);
	*outLen = inLen;

	return res;
}

static int _hmacSHA256(uint8_t *key, uint32_t keyLen, uint8_t *in, uint32_t inLen,
		uint8_t *out, uint32_t *outLen)
{
	if (keyLen == 0 || inLen == 0)	return 1;
	if (key == NULL || in == NULL || out == NULL || outLen == NULL)		return 1;

	*outLen = 32;
	return hmac(SHA256, in, inLen, key, keyLen, out);
}

int main(int argc, char **argv)
{
    char sendBuf[512], recvBuf[512];
    int server_sockfd, client_sockfd;
    int state = 0, client_len, port;
    int sendBufLen, recvBufLen, readLen;

	uint8_t out[64] = { 0x00, };
	uint8_t mac[64] = { 0x00, };
	uint32_t outLen, macLen;

    struct sockaddr_in clientaddr, serveraddr;

    if (argc != 2) {
        LOGD("Usage   : ./server [port]");
        LOGD("example : ./server 9999");
        exit(0);
    }

	if (strcmp(argv[1], "enc") == 0) {
		printBuf((unsigned char *)"in", plain, 64);

		if (_encAES256(iv, 16, key, 32, plain, 64, out, &outLen) != 0x00) {
			LOGD("_encAES256 error:");
			exit(0);
		}

		printBuf((unsigned char *)"enc_result", out, outLen);

		return 0;
	} else if (strcmp(argv[1], "dec") == 0) {
		printBuf((unsigned char *)"in", cipher, 64);

		if (_decAES256(iv, 16, key, 32, cipher, 64, out, &outLen) != 0x00) {
			LOGD("_encAES256 error:");
			exit(0);
		}

		printBuf((unsigned char *)"dec_result", out, outLen);

		return 0;
	} else if (strcmp(argv[1], "hmac") == 0) {
		printBuf((unsigned char *)"in", plain, 64);
		if (_hmacSHA256(key, 32, plain, 64, out, &outLen) != 0x00) {
			LOGD("_hmacSHA256 error:");
			exit(0);
		}
		printBuf((unsigned char *)"hmac", out, outLen);

		return 0;
	}

    port = atoi(argv[1]);
    if (printServerInfo("wlan0", port) != 0x00) {
		LOGD("printServerInfo error:");
		exit(0);
	}

    // internet 기반의 소켓 생성 (INET)
    if ((server_sockfd = socket(AF_INET, SOCK_STREAM, 0)) < 0) {
        perror("[server]: socket error --> ");
        exit(0);
    }

    bzero(&serveraddr, sizeof(serveraddr));
    serveraddr.sin_family = AF_INET;
    serveraddr.sin_addr.s_addr = htonl(INADDR_ANY);
    serveraddr.sin_port = htons(port);

    state = bind(server_sockfd , (struct sockaddr *)&serveraddr,
			sizeof(serveraddr));
    if (state == -1) {
        perror("[server]: bind error --> ");
        exit(0);
    }

    state = listen(server_sockfd, 5);
    if (state == -1) {
        perror("[server]: listen error --> ");
        exit(0);
    }
    while(1) {
        LOGD("wait for connection");
		client_sockfd = accept(server_sockfd, (struct sockaddr *)&clientaddr,
				(socklen_t *)&client_len);
        if (client_sockfd == -1) {
            perror("[server]: accept error --> ");
            exit(0);
        }
        LOGD("accept %s's request", inet_ntoa(clientaddr.sin_addr));

        while(1) {
			recvBufLen = sizeof(recvBuf);
            memset(recvBuf, 0x00, recvBufLen);
			if ((readLen = read(client_sockfd, recvBuf, recvBufLen)) <= 0) {
                perror("[server]: close connection(Read Error)\n");
                close(client_sockfd);
                break;
            }

            if (strncmp(recvBuf, "quit", strlen("quit")) == 0 ||
                strncmp(recvBuf, "server_quit", strlen("server_quit")) == 0) {
                LOGD("close connection\n");
                close(client_sockfd);
                break;
            } else {
				printBuf((unsigned char *)"cmd", (unsigned char *)recvBuf, readLen);

				if (recvBuf[0] == 0x01) {	// uKey
					memset(uKey, 0x00, sizeof(uKey));
					memcpy(uKey, (unsigned char *)(recvBuf + 2), recvBuf[1]);
					printBuf((unsigned char *)"uKey", (unsigned char *)uKey, recvBuf[1]);
					memset(sendBuf, 0x00, sizeof(sendBuf));
					sendBuf[0] = 0x01;
					sendBufLen = 0x01;
				} else if (recvBuf[0] == 0x02) {	// uToken
					memset(uToken, 0x00, sizeof(uToken));
					memcpy(uToken, (unsigned char *)(recvBuf + 2), recvBuf[1]);
					printBuf((unsigned char *)"uToken", (unsigned char *)uToken, recvBuf[1]);
					memset(sendBuf, 0x00, sizeof(sendBuf));
					sendBuf[0] = 0x01;
					sendBufLen = 0x01;
				} else if (recvBuf[0] == 0x03) {	// request update
					printBuf((unsigned char *)"request update", (unsigned char *)(recvBuf + 2),
							recvBuf[1]);
					if (strncmp(recvBuf + 2, (char *)uToken, recvBuf[1]) != 0) {
						LOGD("Your uToken is wrong.");
						memset(sendBuf, 0x00, sizeof(sendBuf));
						sendBuf[0] = 0x0f;
						sendBufLen = 0x01;
					} else {
						LOGD("Your uToken is right.");
						memset(sendBuf, 0x00, sizeof(sendBuf));
						printBuf((unsigned char *)"aes key", uKey, 32);
						printBuf((unsigned char *)"file for aes", file, fileLen);
						if (_encAES256((uint8_t *)uToken, 16, (uint8_t *)uKey, 32,
									file, fileLen, (uint8_t *)(sendBuf + 2),
									(uint32_t *)&sendBufLen) == 0) {
							printBuf((unsigned char *)"hmac key", uKey, 32);
							printBuf((unsigned char *)"file for hmac", file, fileLen);
							if (_hmacSHA256((uint8_t *)uKey, 32, file, fileLen,
										mac, &macLen) == 0x00) {
								sendBuf[0] = 0x01;
								sendBuf[1] = sendBufLen;
								sendBufLen += 2;
								memcpy(sendBuf + sendBufLen, mac, macLen);
								sendBuf[1] += macLen;
								sendBufLen += macLen;
								printBuf((unsigned char *)"hmac", mac, macLen);
							} else {
								LOGD("_hmacSHA256 error:");
								sendBuf[0] = 0x0f;
								sendBufLen = 0x01;
							}
						} else {
							LOGD("_encAES256 error:");
							sendBuf[0] = 0x0f;
							sendBufLen = 0x01;
						}
					}
				}

				write(client_sockfd, sendBuf, sendBufLen);
            }
        }

        if (strncmp(recvBuf, "server_quit", strlen("server_quit")) == 0) {
            break;
        }
    }

    LOGD("close server socket\n");
    close(server_sockfd);

	return 0;
}

