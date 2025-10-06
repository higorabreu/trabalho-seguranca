package org.trabalho.seguranca.storage;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

// gerencia dados dos usuarios em arquivo json
public class UserRepository {
    
    private static final Path USERS_FILE = Paths.get("storage", "users.json");
    
    public UserRepository() throws IOException {
        // cria diretorio storage se nao existir
        Files.createDirectories(USERS_FILE.getParent());
        
        // cria arquivo de usuarios se nao existir
        if (!Files.exists(USERS_FILE)) {
            JSONArray emptyArray = new JSONArray();
            Files.write(USERS_FILE, emptyArray.toString(2).getBytes());
        }
    }
    
    // salva novo usuario
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
    
    // busca usuario pelo nome
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
    
    // verifica se usuario existe
    public boolean userExists(String username) throws IOException {
        return findUser(username) != null;
    }
    
    // carrega todos os usuários do arquivo
    private JSONArray loadUsers() throws IOException {
        String content = new String(Files.readAllBytes(USERS_FILE));
        return new JSONArray(content);
    }
}