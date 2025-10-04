package org.trabalho.seguranca;

import org.trabalho.seguranca.auth.AuthenticationManager;
import org.trabalho.seguranca.crypto.CryptoManager;
import org.trabalho.seguranca.storage.FileStorageManager;
import org.trabalho.seguranca.storage.UserRepository;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.Console;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Security;
import java.util.Scanner;

/**
 * Classe principal do Servidor
 * Implementa interface CLI com autenticação 2FA e criptografia AES-GCM
 */
public class Server {
    
    private static final Scanner scanner = new Scanner(System.in);
    private static AuthenticationManager authManager;
    private static CryptoManager cryptoManager;
    private static FileStorageManager fileManager;
    private static String currentUser = null;
    private static byte[] currentUserKey = null;
    
    public static void main(String[] args) {
        // Registrar provedor criptográfico BouncyCastle
        Security.addProvider(new BouncyCastleProvider());
        
        // Inicializar componentes
        initializeComponents();
        
        // Loop principal da aplicação
        executarLoopPrincipal();
    }
    
    private static void initializeComponents() {
        try {
            UserRepository userRepo = new UserRepository();
            authManager = new AuthenticationManager(userRepo);
            cryptoManager = new CryptoManager();
            fileManager = new FileStorageManager();
        } catch (Exception e) {
            System.exit(1);
        }
    }
    
    private static void executarLoopPrincipal() {
        boolean executando = true;
        
        while (executando) {
            try {
                if (currentUser == null) {
                    exibirMenuPrincipal();
                } else {
                    exibirMenuUsuario();
                }
                
                String opcao = lerOpcao("Escolha uma opção: ");
                
                if (currentUser == null) {
                    executando = processarComandoPrincipal(opcao);
                } else {
                    executando = processarComandoUsuario(opcao);
                }
                
            } catch (Exception e) {
                System.err.println("Erro: " + e.getMessage());
            }
        }
        
        System.out.println("Você saiu do sistema");
    }
    
    private static void exibirMenuPrincipal() {
        System.out.println("\n┌─────────────────────────────┐");
        System.out.println("│         MENU PRINCIPAL      │");
        System.out.println("├─────────────────────────────┤");
        System.out.println("│ 1 - Registrar usuário       │");
        System.out.println("│ 2 - Fazer login             │");
        System.out.println("│ 0 - Sair                    │");
        System.out.println("└─────────────────────────────┘");
    }
    
    private static void exibirMenuUsuario() {
        System.out.println("\n┌─────────────────────────────────────┐");
        System.out.printf("│    LOGADO COMO: %-18s │%n", currentUser);
        System.out.println("├─────────────────────────────────────┤");
        System.out.println("│ 1 - Upload de arquivo               │");
        System.out.println("│ 2 - Download de arquivo             │");
        System.out.println("│ 3 - Listar meus arquivos           │");
        System.out.println("│ 4 - Remover arquivo                 │");
        System.out.println("│ 0 - Logout                          │");
        System.out.println("└─────────────────────────────────────┘");
    }
    
    private static boolean processarComandoPrincipal(String opcao) {
        switch (opcao) {
            case "1":
                registrarUsuario();
                break;
            case "2":
                fazerLogin();
                break;
            case "0":
                return false;
            default:
                System.out.println("Opção inválida!");
        }
        return true;
    }
    
    private static boolean processarComandoUsuario(String opcao) {
        switch (opcao) {
            case "1":
                uploadArquivo();
                break;
            case "2":
                downloadArquivo();
                break;
            case "3":
                listarArquivos();
                break;
            case "4":
                removerArquivo();
                break;
            case "0":
                logout();
                break;
            default:
                System.out.println("Opção inválida!");
        }
        return true;
    }
    
    private static void registrarUsuario() {
        try {
            System.out.println("\nREGISTRO DE NOVO USUÁRIO");
            
            String username = lerString("Nome de usuário: ");
            if (username.trim().isEmpty()) {
                System.out.println("Digite um nome de usuário");
                return;
            }
            
            String password = lerSenha("Senha: ");
            String confirmPassword = lerSenha("Confirmar senha: ");
            
            if (!password.equals(confirmPassword)) {
                System.out.println("Senhas diferentes");
                return;
            }
            
            System.out.println("Registrando usuário...");
            String qrCodePath = authManager.registerUser(username, password);
            System.out.println("Usuário registrado com sucesso!");
            System.out.println("QR Code gerado em: " + qrCodePath);
            System.out.println("Escaneie o QR Code com seu aplicativo autenticador");
            
        } catch (Exception e) {
            System.err.println("Erro ao registrar usuário: " + e.getMessage());
        }
    }
    
    private static void fazerLogin() {
        try {
            System.out.println("\nLOGIN");
            
            String username = lerString("Nome de usuário: ");
            String password = lerSenha("Senha: ");
            String totpCode = lerString("Código 2FA (6 dígitos): ");
            
            System.out.println("Autenticando...");
            byte[] userKey = authManager.authenticateUser(username, password, totpCode);
            
            currentUser = username;
            currentUserKey = userKey;
            
            System.out.println("Login realizado com sucesso!");            
        } catch (Exception e) {
            System.err.println("Falha na autenticação: " + e.getMessage());
        }
    }
    
    private static void uploadArquivo() {
        try {
            System.out.println("\nUPLOAD DE ARQUIVO");
            
            String filePath = lerString("Caminho do arquivo: ");
            Path path = Paths.get(filePath);
            
            if (!Files.exists(path) || !Files.isReadable(path)) {
                System.out.println("Arquivo não encontrado.");
                return;
            }
            
            System.out.println("Criptografando e enviando arquivo...");
            
            byte[] fileContent = Files.readAllBytes(path);
            byte[] encryptedContent = cryptoManager.encrypt(fileContent, currentUserKey);
            
            String fileName = path.getFileName().toString();
            fileManager.storeFile(currentUser, fileName, encryptedContent);
            
            System.out.printf("Arquivo '%s' enviado e criptografado com sucesso!%n", fileName);
            System.out.printf("Tamanho original: %d bytes%n", fileContent.length);
            System.out.printf("Tamanho criptografado: %d bytes%n", encryptedContent.length);
            
        } catch (Exception e) {
            System.err.println("Erro no upload: " + e.getMessage());
        }
    }
    
    private static void downloadArquivo() {
        try {
            System.out.println("\nDOWNLOAD DE ARQUIVO");
            
            String fileName = lerString("Nome do arquivo: ");
            
            System.out.println("Baixando e descriptografando arquivo...");
            
            byte[] encryptedContent = fileManager.retrieveFile(currentUser, fileName);
            byte[] decryptedContent = cryptoManager.decrypt(encryptedContent, currentUserKey);
            
            System.out.printf("Arquivo '%s' descriptografado com sucesso!%n", fileName);
            System.out.println("CONTEÚDO DO ARQUIVO:");
            System.out.println("─".repeat(60));
            
            // Tentar exibir como texto, caso contrário mostrar hexdump
            try {
                String content = new String(decryptedContent, "UTF-8");
                if (isDisplayableText(content)) {
                    System.out.println(content);
                } else {
                    System.out.println("(Arquivo binário - exibindo hexdump dos primeiros 256 bytes)");
                    exibirHexdump(decryptedContent, Math.min(256, decryptedContent.length));
                }
            } catch (Exception e) {
                System.out.println("(Arquivo binário - exibindo hexdump dos primeiros 256 bytes)");
                exibirHexdump(decryptedContent, Math.min(256, decryptedContent.length));
            }
            
            System.out.println("─".repeat(60));
            
            // Opção de salvar localmente
            String salvar = lerString("Deseja salvar o arquivo localmente? (s/N): ");
            if ("s".equalsIgnoreCase(salvar.trim())) {
                String localPath = lerString("Caminho para salvar (ou ENTER para usar nome original): ");
                if (localPath.trim().isEmpty()) {
                    localPath = fileName;
                }
                
                Files.write(Paths.get(localPath), decryptedContent);
                System.out.printf("Arquivo salvo em: %s%n", localPath);
            }
            
        } catch (Exception e) {
            System.err.println("Erro no download: " + e.getMessage());
        }
    }
    
    private static void listarArquivos() {
        try {
            System.out.println("\nMEUS ARQUIVOS");
            
            String[] files = fileManager.listUserFiles(currentUser);
            
            if (files.length == 0) {
                System.out.println("Nenhum arquivo encontrado.");
            } else {
                System.out.printf("Encontrados %d arquivo(s):%n", files.length);
                for (int i = 0; i < files.length; i++) {
                    System.out.printf("%d. %s%n", i + 1, files[i]);
                }
            }
            
        } catch (Exception e) {
            System.err.println("Erro ao listar arquivos: " + e.getMessage());
        }
    }
    
    private static void removerArquivo() {
        try {
            System.out.println("\nREMOVER ARQUIVO");
            
            String fileName = lerString("Nome do arquivo: ");
            String confirmacao = lerString("Tem certeza? Esta ação não pode ser desfeita! (s/N): ");
            
            if (!"s".equalsIgnoreCase(confirmacao.trim())) {
                System.out.println("Operação cancelada.");
                return;
            }
            
            fileManager.removeFile(currentUser, fileName);
            System.out.printf("Arquivo '%s' removido com sucesso!%n", fileName);
            
        } catch (Exception e) {
            System.err.println("Erro ao remover arquivo: " + e.getMessage());
        }
    }
    
    private static void logout() {
        currentUser = null;
        if (currentUserKey != null) {
            // Limpar chave da memória por segurança
            java.util.Arrays.fill(currentUserKey, (byte) 0);
            currentUserKey = null;
        }
        System.out.println("Logout realizado com sucesso!");
    }
    
    // Métodos utilitários
    private static String lerString(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }
    
    private static String lerOpcao(String prompt) {
        System.out.print("\n" + prompt);
        return scanner.nextLine().trim();
    }
    
    private static String lerSenha(String prompt) {
        Console console = System.console();
        if (console != null) {
            char[] password = console.readPassword(prompt);
            return new String(password);
        } else {
            // Fallback para IDEs que não suportam Console
            System.out.print(prompt);
            return scanner.nextLine();
        }
    }
    
    private static boolean isDisplayableText(String content) {
        return content.chars()
                .allMatch(c -> c >= 32 || c == '\n' || c == '\r' || c == '\t');
    }
    
    private static void exibirHexdump(byte[] data, int maxBytes) {
        for (int i = 0; i < maxBytes; i += 16) {
            System.out.printf("%08x: ", i);
            
            // Hex bytes
            for (int j = 0; j < 16; j++) {
                if (i + j < maxBytes) {
                    System.out.printf("%02x ", data[i + j] & 0xFF);
                } else {
                    System.out.print("   ");
                }
            }
            
            System.out.print(" ");
            
            // ASCII representation
            for (int j = 0; j < 16 && i + j < maxBytes; j++) {
                char c = (char) (data[i + j] & 0xFF);
                System.out.print(c >= 32 && c <= 126 ? c : '.');
            }
            
            System.out.println();
        }
        
        if (data.length > maxBytes) {
            System.out.printf("... (%d bytes totais)%n", data.length);
        }
    }
}