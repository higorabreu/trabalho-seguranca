# Servidor de Nuvem Simulado

## Descrição

Este projeto implementa um servidor simulado de nuvem com as seguintes características de segurança:

- ✅ **Autenticação de Dois Fatores (2FA)**: TOTP compatível com Google Authenticator
- ✅ **Criptografia Forte**: AES-256-GCM (modo autenticado)
- ✅ **Derivação Segura de Chaves**: PBKDF2-HMAC-SHA256 com 100.000 iterações
- ✅ **Provedor Criptográfico Seguro**: BouncyCastle
- ✅ **Interface de Linha de Comando**: CLI intuitiva e segura
- ✅ **Armazenamento Seguro**: Senhas nunca armazenadas em texto plano

## Arquitetura de Segurança

### Cadastro de Usuário
1. Senha do usuário é transformada em hash usando PBKDF2 + salt único
2. Secret TOTP é gerado aleatoriamente para 2FA
3. QR Code é criado para configuração no aplicativo autenticador
4. Apenas hash derivado e secret TOTP são armazenados (não a senha)

### Autenticação
1. **Primeiro Fator**: Verificação da senha via PBKDF2
2. **Segundo Fator**: Validação do código TOTP de 6 dígitos
3. Chave de criptografia é derivada em tempo real (não armazenada)

### Criptografia de Arquivos
1. **Algoritmo**: AES-256-GCM (confidencialidade + integridade)
2. **Chave**: Derivada da senha do usuário (256 bits)
3. **IV**: Gerado aleatoriamente para cada arquivo (12 bytes)
4. **Autenticação**: Tag GCM verifica integridade automaticamente

## Requisitos

- **Java**: 11 ou superior
- **Maven**: 3.6 ou superior
- **OS**: Linux (testado), Windows, macOS

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
java -jar target/servidor-nuvem-simulado-1.0-SNAPSHOT-shaded.jar
```

## Como Usar

### 1. Registrar um novo usuário
```
Escolha opção: 1
Nome de usuário: alice
Senha: [digite sua senha]
Confirmar senha: [digite novamente]
```

O sistema irá:
- Gerar um QR Code em `storage/qr_alice.png`
- Exibir a URI OTP para configuração manual
- Salvar as credenciais de forma segura

### 2. Configurar 2FA
- Abra o Google Authenticator (ou similar)
- Escaneie o QR Code gerado
- Ou adicione manualmente usando a URI OTP exibida

### 3. Fazer login
```
Escolha opção: 2
Nome de usuário: alice
Senha: [sua senha]
Código 2FA: [6 dígitos do app]
```

### 4. Upload de arquivo
```
Escolha opção: 1
Caminho do arquivo: /home/user/documento.txt
```

### 5. Download de arquivo
```
Escolha opção: 2
Nome do arquivo: documento.txt
```

O arquivo será descriptografado e seu conteúdo exibido na tela.

## Estrutura do Projeto

```
src/main/java/br/edu/seguranca/
├── ServidorNuvemSimulado.java      # Classe principal com CLI
├── auth/
│   ├── AuthenticationManager.java   # Gerenciador de autenticação
│   └── TOTPManager.java            # Gerenciador de TOTP/2FA
├── crypto/
│   ├── CryptoManager.java          # Criptografia AES-GCM
│   └── KeyDerivation.java          # Derivação PBKDF2
└── storage/
    ├── User.java                   # Modelo de usuário
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

## Características de Segurança

### ✅ O que É Seguro
- Senhas nunca armazenadas em texto plano
- Derivação PBKDF2 com 100k iterações + salt único
- Criptografia AES-GCM autenticada
- Chaves de sessão derivadas em tempo real
- Provedor criptográfico FIPS
- Autenticação de dois fatores obrigatória

### ⚠️ Limitações (Por ser um Protótipo)
- Dados persistidos em arquivos JSON locais
- Execução em um único processo
- Não há comunicação de rede real
- Interface apenas em linha de comando

## Testes

Para testar todas as funcionalidades:

1. **Registre múltiplos usuários** com senhas diferentes
2. **Configure 2FA** no Google Authenticator para cada usuário
3. **Teste login** com credenciais corretas e incorretas
4. **Envie arquivos** de diferentes tipos (texto, binário)
5. **Baixe arquivos** e verifique integridade
6. **Teste isolamento** entre usuários (um usuário não deve ver arquivos de outro)

## Autor

Trabalho de Segurança da Informação
Implementação de servidor simulado com 2FA e criptografia AES-GCM

## Licença

Este projeto é para fins educacionais.