#!/bin/bash

# Script para executar o comando OpenSSL automaticamente
COMMAND="$1"  # O primeiro argumento será o comando OpenSSL

# Verifica se o comando foi passado
if [ -z "$COMMAND" ]; then
    echo "Nenhum comando fornecido."
    exit 1
fi

# Cria um script expect para automatizar a interação com o processo
expect <<EOF
    spawn $COMMAND

    expect "Enter pass phrase for"
    send "foo123\r"

    expect "Sign the certificate?"
    send "yes\r"

    expect "1 out of 1 certificate requests"
    send "yes\r"

    expect eof
EOF

if [ $? -eq 0 ]; then
    echo "Comando executado com sucesso."
else
    echo "Erro ao executar o comando."
    exit 1
fi
