#!/bin/bash

# Script para compilar e executar o Servidor de Nuvem Simulado
# Trabalho de Segurança da Informação e de Redes

echo "Compilando projeto..."
mvn clean package -q

if [ $? -eq 0 ]; then
    echo "Compilação bem-sucedida!"
    echo ""
    echo "Iniciando Server..."
    echo ""
    
    java -jar target/server-1.0-SNAPSHOT.jar
else
    echo "Erro na compilação."
    exit 1
fi