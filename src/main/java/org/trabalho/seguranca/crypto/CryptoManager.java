package org.trabalho.seguranca.crypto;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.nio.ByteBuffer;

// criptografia aes-gcm autenticada (confidencialidade + integridade)
public class CryptoManager {
    
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final String PROVIDER = "BC";
    
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 16;
    private static final int KEY_LENGTH = 32;
    
    private final SecureRandom secureRandom;
    
    public CryptoManager() {
        this.secureRandom = new SecureRandom();
    }
    
    // criptografa dados usando aes-gcm
    public byte[] encrypt(byte[] plaintext, byte[] key) throws Exception {
        if (key.length != KEY_LENGTH) {
            throw new IllegalArgumentException("Chave deve ter " + KEY_LENGTH + " bytes (256 bits)");
        }
        
        // gera iv aleatorio
        byte[] iv = new byte[GCM_IV_LENGTH];
        secureRandom.nextBytes(iv);
        
        // configura cipher
        Cipher cipher = Cipher.getInstance(TRANSFORMATION, PROVIDER);
        SecretKeySpec keySpec = new SecretKeySpec(key, ALGORITHM);
        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
        
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmSpec);
        
        // criptografa
        byte[] ciphertext = cipher.doFinal(plaintext);
        
        // combina iv + ciphertext + tag
        ByteBuffer buffer = ByteBuffer.allocate(GCM_IV_LENGTH + ciphertext.length);
        buffer.put(iv);
        buffer.put(ciphertext);
        
        return buffer.array();
    }
    
    // descriptografa dados usando aes-gcm
    public byte[] decrypt(byte[] encryptedData, byte[] key) throws Exception {
        if (key.length != KEY_LENGTH) {
            throw new IllegalArgumentException("Chave deve ter " + KEY_LENGTH + " bytes (256 bits)");
        }
        
        if (encryptedData.length < GCM_IV_LENGTH + GCM_TAG_LENGTH) {
            throw new IllegalArgumentException("Dados criptografados muito pequenos");
        }
        
        ByteBuffer buffer = ByteBuffer.wrap(encryptedData);
        
        // extrai iv
        byte[] iv = new byte[GCM_IV_LENGTH];
        buffer.get(iv);
        
        // extrai ciphertext + tag
        byte[] ciphertextWithTag = new byte[buffer.remaining()];
        buffer.get(ciphertextWithTag);
        
        // configura cipher
        Cipher cipher = Cipher.getInstance(TRANSFORMATION, PROVIDER);
        SecretKeySpec keySpec = new SecretKeySpec(key, ALGORITHM);
        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
        
        cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec);
        
        // descriptografa e verifica integridade
        return cipher.doFinal(ciphertextWithTag);
    }

    // gera uma chave aleatória de 256 bits
    public byte[] generateRandomKey() {
        byte[] key = new byte[KEY_LENGTH];
        secureRandom.nextBytes(key);
        return key;
    }
}