package org.trabalho.seguranca.storage;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * Repositório para gerenciar dados dos usuários
 * Armazena credenciais de forma segura em arquivo JSON
 */
public class UserRepository {
    
    private static final Path USERS_FILE = Paths.get("storage", "users.json");
    
    public UserRepository() throws IOException {
        // Criar diretório storage se não existir
        Files.createDirectories(USERS_FILE.getParent());
        
        // Criar arquivo de usuários se não existir
        if (!Files.exists(USERS_FILE)) {
            JSONArray emptyArray = new JSONArray();
            Files.write(USERS_FILE, emptyArray.toString(2).getBytes());
        }
    }
    
    /**
     * Salva um novo usuário
     * 
     * @param username Nome do usuário
     * @param salt Salt para derivação de chave
     * @param passwordHash Hash da senha derivado por PBKDF2
     * @param totpSecret Secret TOTP em Base32
     */
    public void saveUser(String username, byte[] salt, byte[] passwordHash, String totpSecret) 
            throws IOException {
        
        if (userExists(username)) {
            throw new IllegalArgumentException("Usuário já existe: " + username);
        }
        
        JSONArray users = loadUsers();
        User newUser = new User(username, salt, passwordHash, totpSecret);
        users.put(newUser.toJSON());
        
        Files.write(USERS_FILE, users.toString(2).getBytes(), 
                   StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
    }
    
    /**
     * Busca um usuário pelo nome
     * 
     * @param username Nome do usuário
     * @return User ou null se não encontrado
     */
    public User findUser(String username) throws IOException {
        JSONArray users = loadUsers();
        
        for (int i = 0; i < users.length(); i++) {
            JSONObject userJson = users.getJSONObject(i);
            if (userJson.getString("username").equals(username)) {
                return User.fromJSON(userJson);
            }
        }
        
        return null;
    }
    
    /**
     * Verifica se um usuário existe
     * 
     * @param username Nome do usuário
     * @return true se o usuário existir
     */
    public boolean userExists(String username) throws IOException {
        return findUser(username) != null;
    }
    
    /**
     * Carrega todos os usuários do arquivo
     */
    private JSONArray loadUsers() throws IOException {
        String content = new String(Files.readAllBytes(USERS_FILE));
        return new JSONArray(content);
    }
}