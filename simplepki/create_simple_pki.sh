#!/bin/bash

# Função para imprimir mensagens em verde
print_green() {
    echo -e "\033[1;32m$1\033[0m"
}

# Função para imprimir mensagens em vermelho
print_red() {
    echo -e "\033[1;31m$1\033[0m"
}

# Criar diretórios e banco de dados para UnBRootCA
print_green "## Criando diretórios e banco de dados para UnBRootCA..."
mkdir -p ca/unbroot-ca/private ca/unbroot-ca/db crl certs || {
    print_red "Erro ao criar diretórios para UnBRootCA."
    exit 1
}
chmod 700 ca/unbroot-ca/private

cp /dev/null ca/unbroot-ca/db/unbroot-ca.db || {
    print_red "Erro ao criar banco de dados unbroot-ca.db."
    exit 1
}
echo 01 > ca/unbroot-ca/db/unbroot-ca.crt.srl
echo 01 > ca/unbroot-ca/db/unbroot-ca.crl.srl

# Criar requisição de certificado para UnBRootCA
print_green "## Criando requisição de certificado para UnBRootCA..."
openssl req -new \
    -config etc/unbroot-ca.conf \
    -out ca/unbroot-ca.csr \
    -keyout ca/unbroot-ca/private/unbroot-ca.key || {
    print_red "Erro ao criar a requisição de certificado para UnBRootCA."
    exit 1
}

# Emitir certificado autoassinado para UnBRootCA
print_green "## Emitindo certificado autoassinado para UnBRootCA..."
openssl ca -selfsign \
    -config etc/unbroot-ca.conf \
    -in ca/unbroot-ca.csr \
    -out ca/unbroot-ca.crt \
    -extensions unbroot_ca_ext || {
    print_red "Erro ao emitir certificado autoassinado para UnBRootCA."
    exit 1
}

# Criar diretórios e banco de dados para UnBSubCA
print_green "## Criando diretórios e banco de dados para UnBSubCA..."
mkdir -p ca/unbsub-ca/private ca/unbsub-ca/db crl certs || {
    print_red "Erro ao criar diretórios para UnBSubCA."
    exit 1
}
chmod 700 ca/unbsub-ca/private

cp /dev/null ca/unbsub-ca/db/unbsub-ca.db || {
    print_red "Erro ao criar banco de dados unbsub-ca.db."
    exit 1
}
echo 01 > ca/unbsub-ca/db/unbsub-ca.crt.srl
echo 01 > ca/unbsub-ca/db/unbsub-ca.crl.srl

# Criar requisição de certificado para UnBSubCA
print_green "## Criando requisição de certificado para UnBSubCA..."
openssl req -new \
    -config etc/unbsub-ca.conf \
    -out ca/unbsub-ca.csr \
    -keyout ca/unbsub-ca/private/unbsub-ca.key || {
    print_red "Erro ao criar a requisição de certificado para UnBSubCA."
    exit 1
}

# Emitir certificado para UnBSubCA
print_green "## Emitindo certificado para UnBSubCA..."
openssl ca \
    -config etc/unbroot-ca.conf \
    -in ca/unbsub-ca.csr \
    -out ca/unbsub-ca.crt \
    -extensions unbsub_ca_ext || {
    print_red "Erro ao emitir certificado para UnBSubCA."
    exit 1
}

print_green "Processo concluído com sucesso!"

## openssl req -new -config etc/unbsub-ca.conf -out certs/user.csr -keyout certs/user.key
