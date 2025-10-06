package org.trabalho.seguranca.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.stream.Stream;

// gerencia armazenamento de arquivos criptografados
public class FileStorageManager {
    
    private static final Path STORAGE_ROOT = Paths.get("storage", "files");
    
    public FileStorageManager() throws IOException {
        // cria diretorio de armazenamento se nao existir
        Files.createDirectories(STORAGE_ROOT);
    }
    
    // armazena arquivo criptografado para um usuario
    public void storeFile(String username, String fileName, byte[] encryptedContent) throws IOException {
        Path userDir = getUserDirectory(username);
        Files.createDirectories(userDir);
        
        Path filePath = userDir.resolve(fileName + ".enc");
        Files.write(filePath, encryptedContent, 
                   StandardOpenOption.CREATE, 
                   StandardOpenOption.WRITE, 
                   StandardOpenOption.TRUNCATE_EXISTING);
    }
    
    // recupera arquivo criptografado de um usuario
    public byte[] retrieveFile(String username, String fileName) throws IOException {
        Path filePath = getUserDirectory(username).resolve(fileName + ".enc");
        
        if (!Files.exists(filePath)) {
            throw new IOException("Arquivo não encontrado: " + fileName);
        }
        
        return Files.readAllBytes(filePath);
    }
    
    // lista todos os arquivos de um usuario
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
                .map(name -> name.substring(0, name.length() - 4)) // remove .enc
                .toArray(String[]::new);
        }
    }
    
    // remove arquivo de um usuario
    public void removeFile(String username, String fileName) throws IOException {
        Path filePath = getUserDirectory(username).resolve(fileName + ".enc");
        
        if (!Files.exists(filePath)) {
            throw new IOException("Arquivo não encontrado: " + fileName);
        }
        
        Files.delete(filePath);
    }
    
    // verifica se arquivo existe para um usuario
    public boolean fileExists(String username, String fileName) {
        Path filePath = getUserDirectory(username).resolve(fileName + ".enc");
        return Files.exists(filePath);
    }
    
    // obtem diretorio de um usuario
    private Path getUserDirectory(String username) {
        return STORAGE_ROOT.resolve(username);
    }
}