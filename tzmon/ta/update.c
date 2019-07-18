#include "update.h"
#include <tee_isocket.h>
#include <tee_tcpsocket.h>
#include <tee_udpsocket.h>
#include <trace.h>

#define UPDATE_SERVER_IP	"192.168.0.143"
#define UPDATE_SERVER_PORT	(9999)
#define QUIT_MESSAGE		"quit"

TEE_Result TZMON_ConnectServer(uint8_t *buffer, uint32_t bufferLen, uint8_t *outData,
		uint32_t *outDataLen)
{
	TEE_Result retVal;

	TEE_tcpSocket_Setup setup;
	TEE_iSocketHandle ctx;
	TEE_iSocket *socket = TEE_tcpSocket;

	uint32_t err, quitBufLen;
	uint8_t quitBuf[10] = { 0x00, };

	if (buffer == NULL || outData == NULL || outDataLen == NULL) {
		return TEE_ERROR_BAD_PARAMETERS;
	}

	if (bufferLen == 0 || *outDataLen == 0) {
		return TEE_ERROR_BAD_PARAMETERS;
	}

	setup.ipVersion = TEE_IP_VERSION_DC;
	setup.server_port = UPDATE_SERVER_PORT;
	setup.server_addr = (char *)UPDATE_SERVER_IP;

	retVal = socket->open(&ctx, &setup, &err);
	CHECK(retVal, "open", return retVal;);

	retVal = socket->send(ctx, buffer, &bufferLen, TEE_TIMEOUT_INFINITE);
	CHECK(retVal, "send", return retVal;);

	strcpy((char *)quitBuf, QUIT_MESSAGE);
	quitBufLen = strlen(QUIT_MESSAGE);
	retVal = socket->send(ctx, quitBuf, &quitBufLen, TEE_TIMEOUT_INFINITE);
	CHECK(retVal, "send", return retVal;);

	retVal = socket->recv(ctx, outData, outDataLen, TEE_TIMEOUT_INFINITE);
	CHECK(retVal, "recv", return retVal;);

	socket->close(ctx);

	return TEE_SUCCESS;
}
