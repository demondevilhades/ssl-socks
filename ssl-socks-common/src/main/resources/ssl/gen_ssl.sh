#!/bin/bash

### 1. gen RSA key

openssl genrsa -des3 -out server.key 2048

### 2. gen csr

openssl req -new -key server.key -out server.csr

### 3. remove password

cp server.key server.key.org
openssl rsa -in server.key.org -out server.key

### 4. transfer key to pkcs8

openssl pkcs8 -topk8 -in server.key -out server_pkcs8.key -nocrypt

### 5. gen crt

openssl x509 -req -days 3650 -in server.csr -signkey server.key -out server.crt

### 6. gen fingerprint

openssl x509 -fingerprint -sha256 -in server.crt | head -n 1 | awk -F= '{print $2}' > fingerprint

