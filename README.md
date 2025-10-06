# Servidor de Nuvem Simulado

## Alunos
- Higor Abreu Freiberger
- Isabela Vill de Aquino
- Guilherme Henriques do Carmo

## Descrição

Este projeto implementa um servidor simulado de nuvem com as seguintes características de segurança:

- **Autenticação de Dois Fatores (2FA)**: TOTP compatível com Google Authenticator
- **Criptografia Forte**: AES-256-GCM (modo autenticado)
- **Derivação Segura de Chaves**: PBKDF2-HMAC-SHA256 com 100.000 iterações
- **Provedor Criptográfico Seguro**: BouncyCastle
- **Interface de Linha de Comando**: CLI intuitiva e segura
- **Armazenamento Seguro**: Senhas nunca armazenadas em texto plano

## Requisitos

- **Java**: 11 ou superior
- **Maven**: 3.6 ou superior

## Compilação e Execução

### 1. Compilar o projeto
```bash
mvn clean compile
```

### 2. Gerar JAR executável
```bash
mvn package
```

### 3. Executar a aplicação
```bash
java -jar target/server-1.0-SNAPSHOT-shaded.jar
```

## Estrutura do Projeto

```
src/main/java/org/trabalho/seguranca/
├── Server.java                     # Classe principal com CLI
├── auth/
│   ├── AuthenticationManager.java   # Gerenciador de autenticação
│   └── TOTPManager.java            # Gerenciador de TOTP/2FA
├── crypto/
│   ├── CryptoManager.java          # Criptografia AES-GCM
│   └── KeyDerivation.java          # Derivação PBKDF2
└── storage/
    ├── User.java                   # Model de usuário
    ├── UserRepository.java         # Persistência de usuários
    └── FileStorageManager.java     # Armazenamento de arquivos
```

## Diretório de Armazenamento

```
storage/
├── users.json                      # Dados dos usuários (hashs + secrets)
├── qr_usuario.png                  # QR Codes para 2FA
└── files/
    └── usuario/
        ├── arquivo1.txt.enc         # Arquivos criptografados
        └── arquivo2.pdf.enc
```

## Dependências Principais

- **BouncyCastle FIPS**: Provedor criptográfico certificado
- **dev.samstevens.totp**: Implementação TOTP/2FA
- **ZXing**: Geração de QR Codes
- **Apache Commons Codec**: Codificação Base32/Base64
- **org.json**: Persistência em JSON