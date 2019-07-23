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

#define CMD	"adb shell /vendor/bin/optee_tzmon"

#ifdef DEBUG
#define ANSI_COLOR "\x1b[31m"
#define ANSI_COLOR_RESET "\x1b[0m"
#define DEBUG_PREFIX ANSI_COLOR "[S2B_LOGD] "
#define DEBUG_NOPREFIX ANSI_COLOR
#define LOGD(msg, ...) fprintf(stderr, DEBUG_PREFIX msg "\n" ANSI_COLOR_RESET, \
								##__VA_ARGS__)
#define LOGN(msg, ...) fprintf(stderr, DEBUG_PREFIX msg ANSI_COLOR_RESET, \
								##__VA_ARGS__)
#else
#define LOGD(...)
#define LOGN(...)
#endif

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
	LOGD("[ip]	: %s", ipstr);
	LOGD("[port]	: %d", port);
	LOGD("=========================\n");

	return retVal;
}

int main(int argc, char **argv)
{
    char buf[512], result[512];
    int server_sockfd, client_sockfd;
    int state = 0, client_len, port;
	int bufLen = sizeof(buf);

	FILE *fp = NULL;

    struct sockaddr_in clientaddr, serveraddr;

	if (argc != 2) {
		LOGD("Usage	: ./server [port]");
		LOGD("example	: ./server 9999");
		exit(0);
	}

	port = atoi(argv[1]);
	if (printServerInfo("enp3s0", port) != 0x00) {
		LOGD("printServerInfo error");
		exit(0);
	}

    if ((server_sockfd = socket(AF_INET, SOCK_STREAM, 0)) < 0) {
        perror("[server]: socket error --> ");
        exit(0);
    }

    bzero(&serveraddr, sizeof(serveraddr));
    serveraddr.sin_family = AF_INET;
    serveraddr.sin_addr.s_addr = htonl(INADDR_ANY);
    serveraddr.sin_port = htons(port);

    state = bind(server_sockfd , (struct sockaddr *)&serveraddr, sizeof(serveraddr));
    if (state == -1) {
        perror("[server]: bind error --> ");
        exit(0);
    }

    state = listen(server_sockfd, 5);
    if (state == -1) {
        perror("[server]: listen error --> ");
        exit(0);
    }

	system("adb root");
    while(1) {
		LOGD("[server]: wait for connection");
        client_sockfd = accept(server_sockfd, (struct sockaddr *)&clientaddr, &client_len);
        if (client_sockfd == -1) {
            perror("[server]: accept error --> ");
            exit(0);
        }
		LOGD("[server]: accept %s's request", inet_ntoa(clientaddr.sin_addr));

        while(1) {
            memset(buf, 0x00, sizeof(buf));
            if (read(client_sockfd, buf, bufLen) <= 0) {
				perror("[server]: close connection(Read Error)\n");
                close(client_sockfd);
                break;
            }

            if (strncmp(buf, "quit", strlen("quit")) == 0 ||
				strncmp(buf, "server_quit", strlen("server_quit")) == 0) {
				LOGD("[server]: close connection\n");
                close(client_sockfd);
                break;
			} else {
				LOGD("Cmd: %s", buf);

				if (strncmp(buf, CMD, strlen(CMD)) == 0) {
					system(buf);

					system("adb pull /vendor/bin/result.txt");
					fp = fopen("result.txt", "r");
					fgets(result, sizeof(result), fp);
					fclose(fp);

					LOGD("Result(%d): %s", (int)strlen(result) / 2, result);

					write(client_sockfd, result, strlen(result));
				} else {
					LOGD("This cmd is not excutable!");
					write(client_sockfd, "fail", strlen("fail"));
				}
			}
        }
		
		if (strncmp(buf, "server_quit", strlen("server_quit")) == 0) {
			break;
		}
	}
	
	LOGD("[server]: close server socket\n");
    close(server_sockfd);
}

