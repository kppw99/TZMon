
#define ADMIN_PWD	"wjstkdgns"

static uint8_t key[32 + 1] = {     // 32
	0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08,
	0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08,
	0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08,
	0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08,
	0x00
};

static uint8_t key2[32 + 1] = {    // 25
	0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08,
	0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08,
	0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08,
	0x08, 0x00
};

static uint8_t iv[16 + 1] = {
	0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01,
	0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01
};

static uint8_t inData[32 + 1] = {
	0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08,
	0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08,
	0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08,
	0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08,
	0x00
};

static uint8_t encData[32 + 1] = {
	0x47, 0x5b, 0xd8, 0x0e, 0x8a, 0x56, 0xc0, 0x96,
	0xd4, 0x74, 0x47, 0xd7, 0x67, 0xfb, 0xb5, 0x75,
	0x6d, 0xd3, 0xae, 0xbd, 0x3a, 0xc4, 0xa2, 0xad,
	0x75, 0x53, 0x6b, 0x78, 0x14, 0x85, 0xee, 0x0a,
	0x00
};

static uint8_t appHash[32 + 1] = {
	0x58, 0x95, 0x7D, 0x8E, 0x32, 0x27, 0x4C, 0xA6,
	0x41, 0xD0, 0xB6, 0x79, 0x31, 0x37, 0xF0, 0xB5,
	0x47, 0xF8, 0x9E, 0x76, 0xCF, 0x35, 0x6C, 0x93,
	0xFA, 0xCA, 0xE1, 0x1A, 0x2C, 0xC7, 0xDF, 0x3F,
	0x00
};

static uint8_t cMessage[32 + 1] = {
	0xEA, 0x80, 0xAB, 0x1C, 0xE3, 0x56, 0x51, 0x82,
	0xB3, 0xCD, 0x6D, 0xD6, 0x0B, 0x5A, 0x8B, 0xFE,
	0xA5, 0x27, 0x28, 0x86, 0xC4, 0x28, 0xD3, 0x36,
	0x00, 0xD1, 0xA8, 0x33, 0xE7, 0x2F, 0x2A, 0x14,
	0x00
};
