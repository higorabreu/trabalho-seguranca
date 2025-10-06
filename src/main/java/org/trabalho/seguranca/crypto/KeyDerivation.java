package org.trabalho.seguranca.crypto;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

// derivacao segura de chaves usando pbkdf2
public class KeyDerivation {
    
    private static final String PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int SALT_LENGTH = 16;
    private static final int ITERATIONS = 100_000;
    private static final int KEY_LENGTH = 256;
    
    private static final SecureRandom secureRandom = new SecureRandom();
    
    // gera salt aleatorio
    public static byte[] generateSalt() {
        byte[] salt = new byte[SALT_LENGTH];
        secureRandom.nextBytes(salt);
        return salt;
    }
    
    // deriva chave a partir da senha usando pbkdf2
    public static byte[] deriveKey(String password, byte[] salt) 
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        
        PBEKeySpec spec = new PBEKeySpec(
            password.toCharArray(), 
            salt, 
            ITERATIONS, 
            KEY_LENGTH
        );
        
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM);
            byte[] key = factory.generateSecret(spec).getEncoded();
            return key;
        } finally {
            // limpa senha da memoria
            spec.clearPassword();
        }
    }
    
    // verifica se senha corresponde ao hash armazenado
    public static boolean verifyPassword(String password, byte[] salt, byte[] expectedHash) {
        try {
            byte[] derivedKey = deriveKey(password, salt);
            boolean matches = Arrays.equals(derivedKey, expectedHash);
            
            // limpa chave derivada da memoria
            Arrays.fill(derivedKey, (byte) 0);
            
            return matches;
        } catch (Exception e) {
            return false;
        }
    }
}