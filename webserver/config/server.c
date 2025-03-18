#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <sys/socket.h>
#include <arpa/inet.h>
#include <netinet/in.h>
#include <signal.h>
#include <pthread.h>
#include <fcntl.h>
#include <errno.h>
#include <json-c/json.h>

// HTTP Server Configuration
#define PORT 8080
#define MAX_BUFFER_SIZE 4096

int server_running = 1;

void handle_signal(int sig) {
    if (sig == SIGINT) {
        printf("\nShutting down server...\n");
        server_running = 0;
    }
}

void *handle_connection(void *client_socket);

int main() {
    int server_fd;
    struct sockaddr_in server_addr;
    
    signal(SIGINT, handle_signal);
    
    if ((server_fd = socket(AF_INET, SOCK_STREAM, 0)) < 0) {
        perror("Cannot create socket");
        exit(EXIT_FAILURE);
    }
    
    int opt = 1;
    if (setsockopt(server_fd, SOL_SOCKET, SO_REUSEADDR, &opt, sizeof(opt)) < 0) {
        perror("setsockopt failed");
        exit(EXIT_FAILURE);
    }
    
    memset(&server_addr, 0, sizeof(server_addr));
    server_addr.sin_family = AF_INET;
    server_addr.sin_addr.s_addr = htonl(INADDR_ANY);
    server_addr.sin_port = htons(PORT);
    
    if (bind(server_fd, (struct sockaddr*)&server_addr, sizeof(server_addr)) < 0) {
        perror("Cannot bind socket");
        exit(EXIT_FAILURE);
    }
    
    if (listen(server_fd, 10) < 0) {
        perror("Cannot listen on socket");
        exit(EXIT_FAILURE);
    }
    
    printf("HTTP Server started on port %d\n", PORT);
    
    while (server_running) {
        struct sockaddr_in client_addr;
        socklen_t client_len = sizeof(client_addr);
        int client_fd = accept(server_fd, (struct sockaddr*)&client_addr, &client_len);
        
        if (client_fd < 0) {
            perror("Cannot accept connection");
            continue;
        }
        
        pthread_t thread_id;
        if (pthread_create(&thread_id, NULL, handle_connection, (void*)(intptr_t)client_fd) != 0) {
            perror("Failed to create thread");
            close(client_fd);
        } else {
            pthread_detach(thread_id);
        }
    }
    
    close(server_fd);
    return 0;
}

void *handle_connection(void *client_socket) {
    int client_fd = (intptr_t)client_socket;
    char buffer[MAX_BUFFER_SIZE] = {0};
    int bytes = read(client_fd, buffer, sizeof(buffer) - 1);
    
    if (bytes > 0) {
        buffer[bytes] = '\0';
        printf("Request:\n%s\n", buffer);
        
        const char *response =
            "HTTP/1.1 200 OK\r\n"
            "Content-Type: text/html\r\n"
            "Connection: close\r\n"
            "\r\n"
            "<!DOCTYPE html>\n"
            "<html><head><title>Simple HTTP Server</title></head>"
            "<body><h1>Server is running!</h1></body></html>";
        
        write(client_fd, response, strlen(response));
    }
    
    close(client_fd);
    return NULL;
}
