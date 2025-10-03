package org.trabalho.seguranca.crypto;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

/**
 * Utilitários para derivação segura de chaves usando PBKDF2
 */
public class KeyDerivation {
    
    private static final String PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int SALT_LENGTH = 16; // 128 bits
    private static final int ITERATIONS = 100_000; // 100.000 iterações
    private static final int KEY_LENGTH = 256; // 256 bits
    
    private static final SecureRandom secureRandom = new SecureRandom();
    
    /**
     * Gera um salt aleatório
     * 
     * @return Salt de 16 bytes
     */
    public static byte[] generateSalt() {
        byte[] salt = new byte[SALT_LENGTH];
        secureRandom.nextBytes(salt);
        return salt;
    }
    
    /**
     * Deriva uma chave a partir de uma senha usando PBKDF2
     * 
     * @param password Senha do usuário
     * @param salt Salt único para o usuário
     * @return Chave derivada de 32 bytes (256 bits)
     */
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
            // Limpar senha da memória por segurança
            spec.clearPassword();
        }
    }
    
    /**
     * Verifica se uma senha corresponde ao hash armazenado
     * 
     * @param password Senha fornecida
     * @param salt Salt armazenado
     * @param expectedHash Hash esperado
     * @return true se a senha estiver correta
     */
    public static boolean verifyPassword(String password, byte[] salt, byte[] expectedHash) {
        try {
            byte[] derivedKey = deriveKey(password, salt);
            boolean matches = Arrays.equals(derivedKey, expectedHash);
            
            // Limpar chave derivada da memória
            Arrays.fill(derivedKey, (byte) 0);
            
            return matches;
        } catch (Exception e) {
            return false;
        }
    }
}