package org.trabalho.seguranca.crypto;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.nio.ByteBuffer;

/**
 * Gerenciador de criptografia usando AES-GCM
 * Implementa criptografia simétrica autenticada (confidencialidade + integridade)
 */
public class CryptoManager {
    
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final String PROVIDER = "BC";
    
    private static final int GCM_IV_LENGTH = 12; // 12 bytes para GCM
    private static final int GCM_TAG_LENGTH = 16; // 16 bytes para autenticação
    private static final int KEY_LENGTH = 32; // 256 bits
    
    private final SecureRandom secureRandom;
    
    public CryptoManager() {
        this.secureRandom = new SecureRandom();
    }
    
    /**
     * Criptografa dados usando AES-GCM
     * 
     * @param plaintext Dados em texto claro
     * @param key Chave de criptografia (256 bits)
     * @return Dados criptografados (IV + ciphertext + tag)
     */
    public byte[] encrypt(byte[] plaintext, byte[] key) throws Exception {
        if (key.length != KEY_LENGTH) {
            throw new IllegalArgumentException("Chave deve ter " + KEY_LENGTH + " bytes (256 bits)");
        }
        
        // Gerar IV aleatório para GCM
        byte[] iv = new byte[GCM_IV_LENGTH];
        secureRandom.nextBytes(iv);
        
        // Configurar cipher
        Cipher cipher = Cipher.getInstance(TRANSFORMATION, PROVIDER);
        SecretKeySpec keySpec = new SecretKeySpec(key, ALGORITHM);
        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
        
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmSpec);
        
        // Criptografar
        byte[] ciphertext = cipher.doFinal(plaintext);
        
        // Combinar IV + ciphertext + tag (tag já está incluído no ciphertext do GCM)
        ByteBuffer buffer = ByteBuffer.allocate(GCM_IV_LENGTH + ciphertext.length);
        buffer.put(iv);
        buffer.put(ciphertext);
        
        return buffer.array();
    }
    
    /**
     * Descriptografa dados usando AES-GCM
     * 
     * @param encryptedData Dados criptografados (IV + ciphertext + tag)
     * @param key Chave de descriptografia (256 bits)
     * @return Dados em texto claro
     */
    public byte[] decrypt(byte[] encryptedData, byte[] key) throws Exception {
        if (key.length != KEY_LENGTH) {
            throw new IllegalArgumentException("Chave deve ter " + KEY_LENGTH + " bytes (256 bits)");
        }
        
        if (encryptedData.length < GCM_IV_LENGTH + GCM_TAG_LENGTH) {
            throw new IllegalArgumentException("Dados criptografados muito pequenos");
        }
        
        ByteBuffer buffer = ByteBuffer.wrap(encryptedData);
        
        // Extrair IV
        byte[] iv = new byte[GCM_IV_LENGTH];
        buffer.get(iv);
        
        // Extrair ciphertext + tag
        byte[] ciphertextWithTag = new byte[buffer.remaining()];
        buffer.get(ciphertextWithTag);
        
        // Configurar cipher
        Cipher cipher = Cipher.getInstance(TRANSFORMATION, PROVIDER);
        SecretKeySpec keySpec = new SecretKeySpec(key, ALGORITHM);
        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
        
        cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec);
        
        // Descriptografar e verificar integridade
        return cipher.doFinal(ciphertextWithTag);
    }
    
    /**
     * Gera uma chave aleatória de 256 bits
     * 
     * @return Chave aleatória
     */
    public byte[] generateRandomKey() {
        byte[] key = new byte[KEY_LENGTH];
        secureRandom.nextBytes(key);
        return key;
    }
}