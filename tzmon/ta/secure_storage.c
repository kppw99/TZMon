#include "secure_storage.h"

#define MIN_FILE_NAME_SIZE		(1)
#define MAX_FILE_NAME_SIZE		(32)
#define MIN_DATA_SIZE			(1)
#define MAX_DATA_SIZE			(256)

TEE_Result TZMON_DeleteFile(uint8_t *fileName, uint32_t fileNameLen)
{
	TEE_Result retVal;
	TEE_ObjectHandle object;

	if (fileNameLen < MIN_FILE_NAME_SIZE ||
			fileNameLen > MAX_FILE_NAME_SIZE ||	fileName == NULL) {
		return TEE_ERROR_BAD_PARAMETERS;
	}

	retVal = TEE_OpenPersistentObject(TEE_STORAGE_PRIVATE,
			fileName, fileNameLen,
			TEE_DATA_FLAG_ACCESS_READ | TEE_DATA_FLAG_ACCESS_WRITE_META,
			&object);
	CHECK(retVal, "TEE_OpenPersistentObject", return retVal;);

	TEE_CloseAndDeletePersistentObject1(object);

	return retVal;
}

TEE_Result TZMON_WriteFile(uint8_t *fileName, uint32_t fileNameLen,
		uint8_t *data, uint32_t dataLen)
{
	TEE_Result retVal;
	TEE_ObjectHandle object;

	uint32_t objFlag = TEE_DATA_FLAG_ACCESS_READ |
		TEE_DATA_FLAG_ACCESS_WRITE |
		TEE_DATA_FLAG_ACCESS_WRITE_META |
		TEE_DATA_FLAG_OVERWRITE;

	if (fileNameLen < MIN_FILE_NAME_SIZE ||
			fileNameLen > MAX_FILE_NAME_SIZE ||	fileName == NULL) {
		return TEE_ERROR_BAD_PARAMETERS;
	}

	if (dataLen < MIN_DATA_SIZE || dataLen > MAX_DATA_SIZE || data == NULL) {
		return TEE_ERROR_BAD_PARAMETERS;
	}

	retVal = TEE_CreatePersistentObject(TEE_STORAGE_PRIVATE,
			fileName, fileNameLen, objFlag, TEE_HANDLE_NULL,
			NULL, 0x00, &object);
	CHECK(retVal, "TEE_CreatePersistentObject", return retVal;);

	retVal = TEE_WriteObjectData(object, data, dataLen);
	CHECK(retVal, "TEE_WriteObjectData",
			TEE_CloseAndDeletePersistentObject1(object);
			return retVal;);

	TEE_CloseObject(object);

	return retVal;
}

TEE_Result TZMON_ReadFile(uint8_t *fileName, uint32_t fileNameLen,
		uint8_t *data, uint32_t *dataLen)
{
	TEE_Result retVal;
	TEE_ObjectHandle object;
	TEE_ObjectInfo objInfo;

	retVal = TEE_OpenPersistentObject(TEE_STORAGE_PRIVATE,
			fileName, fileNameLen,
			TEE_DATA_FLAG_ACCESS_READ | TEE_DATA_FLAG_SHARE_READ,
			&object);
	CHECK(retVal, "TEE_OpenPersistentObject", return retVal;);

	retVal = TEE_GetObjectInfo1(object, &objInfo);
	CHECK(retVal, "TEE_GetObjectInfo1", goto exit;);

	if (objInfo.dataSize > *dataLen) {
		retVal = TEE_ERROR_SHORT_BUFFER;
		goto exit;
	}

	retVal = TEE_ReadObjectData(object, data, objInfo.dataSize, dataLen);
	if (retVal != TEE_SUCCESS || *dataLen != objInfo.dataSize) {
		goto exit;
	}

exit:
	TEE_CloseObject(object);
	
	return retVal;
}
