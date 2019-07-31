#ifndef BLOCKINGER_TZMONSOCKET_H
#define BLOCKINGER_TZMONSOCKET_H

bool _call_tzmonTA(char *cmd, char *out, int *outLen);
bool _call_tzmonAbusingDetection(const char *appName);

#endif //BLOCKINGER_TZMONSOCKET_H
