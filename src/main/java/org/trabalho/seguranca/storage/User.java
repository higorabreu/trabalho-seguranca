package org.trabalho.seguranca.storage;

import org.json.JSONObject;
import java.util.Base64;

// representa dados de um usuario
public class User {
    private String username;
    private byte[] salt;
    private byte[] passwordHash;
    private String totpSecret;
    
    public User(String username, byte[] salt, byte[] passwordHash, String totpSecret) {
        this.username = username;
        this.salt = salt;
        this.passwordHash = passwordHash;
        this.totpSecret = totpSecret;
    }
    
    public String getUsername() { return username; }
    public byte[] getSalt() { return salt; }
    public byte[] getPasswordHash() { return passwordHash; }
    public String getTotpSecret() { return totpSecret; }
    
    // converte para json
    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("username", username);
        json.put("salt", Base64.getEncoder().encodeToString(salt));
        json.put("passwordHash", Base64.getEncoder().encodeToString(passwordHash));
        json.put("totpSecret", totpSecret);
        return json;
    }
    
    // cria a partir de json
    public static User fromJSON(JSONObject json) {
        return new User(
            json.getString("username"),
            Base64.getDecoder().decode(json.getString("salt")),
            Base64.getDecoder().decode(json.getString("passwordHash")),
            json.getString("totpSecret")
        );
    }
}