# Compiler
CC := /home/test/buildroot-2015.05/output/host/usr/bin/mips-buildroot-linux-uclibc-gcc

# CFLAGS (bao gồm đường dẫn OpenSSL)
CFLAGS = -Wall -Wextra -O2 -std=c99 \
    -I/home/test/buildroot-2015.05/output/staging/usr/include/json-c \
    -I/home/test/buildroot-2015.05/output/staging/usr/include \
    -L/home/test/buildroot-2015.05/output/staging/usr/lib


# LDFLAGS (thêm OpenSSL)
LDFLAGS = -L/home/test/buildroot-2015.05/output/staging/usr/lib -lssl -lcrypto -lpthread

# Target binary
TARGET = https_server

# Source files
SRC = https_server.c

# Object files
OBJ = $(SRC:.c=.o)

# Default target
all: $(TARGET)

# Compile sources
%.o: %.c
	$(CC) $(CFLAGS) -c $< -o $@

# Link object files
$(TARGET): $(OBJ)
	$(CC) $(OBJ) $(LDFLAGS) -o $@

# Clean up build files
clean:
	rm -f $(OBJ) $(TARGET)
