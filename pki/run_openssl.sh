#!/bin/bash

COMMAND="$1" 

if [ -z "$COMMAND" ]; then
    echo "Nenhum comando fornecido."
    exit 1
fi

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
