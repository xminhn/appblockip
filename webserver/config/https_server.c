#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <sys/socket.h>
#include <arpa/inet.h>
#include <netinet/in.h>
#include <openssl/ssl.h>
#include <openssl/err.h>
#include <signal.h>
#include <pthread.h>
#include <fcntl.h>
#include <errno.h>

#define PORT 8443
#define CERT_FILE "server.crt"
#define KEY_FILE "server.key"
#define MAX_BUFFER_SIZE 4096

int server_running = 1;
SSL_CTX *ssl_ctx = NULL;

void init_openssl();
void cleanup_openssl();
SSL_CTX *create_ssl_context();
void configure_ssl_context(SSL_CTX *ctx);
void handle_client(SSL *ssl);
void handle_signal(int sig);
void *handle_connection(void *ssl_ptr);
char *get_connected_devices();
char *block_device(const char *mac);
char *unblock_device(const char *mac);
char *block_website(const char *url);
char *unblock_website(const char *url);
void execute_command(char *command, char *output, size_t output_size);

int main() {
    int server_fd;
    struct sockaddr_in server_addr;

    init_openssl();
    ssl_ctx = create_ssl_context();
    configure_ssl_context(ssl_ctx);
    
    signal(SIGINT, handle_signal);
    
    if ((server_fd = socket(AF_INET, SOCK_STREAM, 0)) < 0) {
        perror("Cannot create socket");
        exit(EXIT_FAILURE);
    }
    
    int opt = 1;
    setsockopt(server_fd, SOL_SOCKET, SO_REUSEADDR, &opt, sizeof(opt));
    
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
    
    printf("HTTPS Server started on port %d\n", PORT);
    
    while (server_running) {
        struct sockaddr_in client_addr;
        socklen_t client_len = sizeof(client_addr);
        int client_fd = accept(server_fd, (struct sockaddr*)&client_addr, &client_len);
        if (client_fd < 0) continue;
        
        SSL *ssl = SSL_new(ssl_ctx);
        SSL_set_fd(ssl, client_fd);
        
        if (SSL_accept(ssl) <= 0) {
            ERR_print_errors_fp(stderr);
        } else {
            pthread_t thread_id;
            if (pthread_create(&thread_id, NULL, handle_connection, ssl) == 0) {
                pthread_detach(thread_id);
            } else {
                SSL_free(ssl);
                close(client_fd);
            }
        }
    }
    
    close(server_fd);
    cleanup_openssl();
    
    return 0;
}

void init_openssl() {
    SSL_library_init();
    SSL_load_error_strings();
    OpenSSL_add_all_algorithms();
}

void cleanup_openssl() {
    if (ssl_ctx) SSL_CTX_free(ssl_ctx);
    EVP_cleanup();
}

SSL_CTX *create_ssl_context() {
    SSL_CTX *ctx = SSL_CTX_new(SSLv23_server_method());
    if (!ctx) {
        perror("Unable to create SSL context");
        ERR_print_errors_fp(stderr);
        exit(EXIT_FAILURE);
    }
    return ctx;
}

void configure_ssl_context(SSL_CTX *ctx) {
    if (SSL_CTX_use_certificate_file(ctx, CERT_FILE, SSL_FILETYPE_PEM) <= 0 ||
        SSL_CTX_use_PrivateKey_file(ctx, KEY_FILE, SSL_FILETYPE_PEM) <= 0 ||
        !SSL_CTX_check_private_key(ctx)) {
        ERR_print_errors_fp(stderr);
        exit(EXIT_FAILURE);
    }
}

void *handle_connection(void *ssl_ptr) {
    SSL *ssl = (SSL *)ssl_ptr;
    int client_fd = SSL_get_fd(ssl);
    
    handle_client(ssl);
    
    SSL_shutdown(ssl);
    SSL_free(ssl);
    close(client_fd);
    
    return NULL;
}

void handle_client(SSL *ssl) {
    char buffer[MAX_BUFFER_SIZE] = {0};
    int bytes = SSL_read(ssl, buffer, sizeof(buffer) - 1);
    if (bytes <= 0) return;

    buffer[bytes] = '\0';
    char response[MAX_BUFFER_SIZE];

    if (strstr(buffer, "GET /api/devices") != NULL) {
        snprintf(response, sizeof(response), "HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\n\r\n%s", get_connected_devices());
    } else if (strstr(buffer, "GET /") != NULL) {
        snprintf(response, sizeof(response), "HTTP/1.1 200 OK\r\nContent-Type: text/html\r\n\r\n<html><body><h1>Server Running</h1></body></html>");
    } else {
        snprintf(response, sizeof(response), "HTTP/1.1 404 Not Found\r\nContent-Type: text/plain\r\n\r\nEndpoint not found");
    }

    SSL_write(ssl, response, strlen(response));
}

void handle_signal(int sig) {
    if (sig == SIGINT) {
        printf("\nShutting down server...\n");
        server_running = 0;
    }
}

char *get_connected_devices() {
    return "Connected devices: Device 1, Device 2";  // Giả lập dữ liệu
}

char *block_device(const char *mac) {
    static char response[128];
    snprintf(response, sizeof(response), "Device with MAC %s blocked", mac);
    return response;
}

char *unblock_device(const char *mac) {
    static char response[128];
    snprintf(response, sizeof(response), "Device with MAC %s unblocked", mac);
    return response;
}

char *block_website(const char *url) {
    static char response[128];
    snprintf(response, sizeof(response), "Website %s blocked", url);
    return response;
}

char *unblock_website(const char *url) {
    static char response[128];
    snprintf(response, sizeof(response), "Website %s unblocked", url);
    return response;
}

void execute_command(char *command, char *output, size_t output_size) {
    FILE *fp = popen(command, "r");
    if (!fp) {
        snprintf(output, output_size, "Failed to execute command");
        return;
    }
    
    output[0] = '\0';
    char buffer[128];
    while (fgets(buffer, sizeof(buffer), fp) != NULL) {
        strncat(output, buffer, output_size - strlen(output) - 1);
    }
    
    pclose(fp);
}

