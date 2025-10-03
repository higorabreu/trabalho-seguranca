package org.trabalho.seguranca.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.stream.Stream;

/**
 * Gerenciador de armazenamento de arquivos criptografados
 */
public class FileStorageManager {
    
    private static final Path STORAGE_ROOT = Paths.get("storage", "files");
    
    public FileStorageManager() throws IOException {
        // Criar diretório de armazenamento se não existir
        Files.createDirectories(STORAGE_ROOT);
    }
    
    /**
     * Armazena um arquivo criptografado para um usuário
     * 
     * @param username Nome do usuário
     * @param fileName Nome do arquivo
     * @param encryptedContent Conteúdo criptografado
     */
    public void storeFile(String username, String fileName, byte[] encryptedContent) throws IOException {
        Path userDir = getUserDirectory(username);
        Files.createDirectories(userDir);
        
        Path filePath = userDir.resolve(fileName + ".enc");
        Files.write(filePath, encryptedContent, 
                   StandardOpenOption.CREATE, 
                   StandardOpenOption.WRITE, 
                   StandardOpenOption.TRUNCATE_EXISTING);
    }
    
    /**
     * Recupera um arquivo criptografado de um usuário
     * 
     * @param username Nome do usuário
     * @param fileName Nome do arquivo
     * @return Conteúdo criptografado
     */
    public byte[] retrieveFile(String username, String fileName) throws IOException {
        Path filePath = getUserDirectory(username).resolve(fileName + ".enc");
        
        if (!Files.exists(filePath)) {
            throw new IOException("Arquivo não encontrado: " + fileName);
        }
        
        return Files.readAllBytes(filePath);
    }
    
    /**
     * Lista todos os arquivos de um usuário
     * 
     * @param username Nome do usuário
     * @return Array com nomes dos arquivos (sem extensão .enc)
     */
    public String[] listUserFiles(String username) throws IOException {
        Path userDir = getUserDirectory(username);
        
        if (!Files.exists(userDir)) {
            return new String[0];
        }
        
        try (Stream<Path> files = Files.list(userDir)) {
            return files
                .filter(Files::isRegularFile)
                .map(Path::getFileName)
                .map(Path::toString)
                .filter(name -> name.endsWith(".enc"))
                .map(name -> name.substring(0, name.length() - 4)) // Remove .enc
                .toArray(String[]::new);
        }
    }
    
    /**
     * Remove um arquivo de um usuário
     * 
     * @param username Nome do usuário
     * @param fileName Nome do arquivo
     */
    public void removeFile(String username, String fileName) throws IOException {
        Path filePath = getUserDirectory(username).resolve(fileName + ".enc");
        
        if (!Files.exists(filePath)) {
            throw new IOException("Arquivo não encontrado: " + fileName);
        }
        
        Files.delete(filePath);
    }
    
    /**
     * Verifica se um arquivo existe para um usuário
     * 
     * @param username Nome do usuário
     * @param fileName Nome do arquivo
     * @return true se o arquivo existir
     */
    public boolean fileExists(String username, String fileName) {
        Path filePath = getUserDirectory(username).resolve(fileName + ".enc");
        return Files.exists(filePath);
    }
    
    /**
     * Obtém o diretório de um usuário
     * 
     * @param username Nome do usuário
     * @return Path do diretório do usuário
     */
    private Path getUserDirectory(String username) {
        return STORAGE_ROOT.resolve(username);
    }
}