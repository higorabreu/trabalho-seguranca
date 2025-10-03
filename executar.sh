#!/bin/bash

# Script para compilar e executar o Servidor de Nuvem Simulado
# Trabalho de Segurança da Informação

echo "🔧 Compilando projeto..."
mvn clean package -q

if [ $? -eq 0 ]; then
    echo "✅ Compilação bem-sucedida!"
    echo ""
    echo "🚀 Iniciando Servidor de Nuvem Simulado..."
    echo "   (Ctrl+C para sair)"
    echo ""
    
    java -jar target/servidor-nuvem-simulado-1.0-SNAPSHOT.jar
else
    echo "❌ Erro na compilação!"
    exit 1
fi