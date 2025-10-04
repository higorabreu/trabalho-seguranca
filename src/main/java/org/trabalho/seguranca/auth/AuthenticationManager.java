package org.trabalho.seguranca.auth;

import org.trabalho.seguranca.crypto.KeyDerivation;
import org.trabalho.seguranca.storage.UserRepository;
import org.trabalho.seguranca.storage.User;

/**
 * Gerenciador de autenticação que integra senha + 2FA TOTP
 */
public class AuthenticationManager {
    
    private final UserRepository userRepository;
    private final TOTPManager totpManager;
    
    public AuthenticationManager(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.totpManager = new TOTPManager();
    }
    
    /**
     * Registra um novo usuário com 2FA
     * 
     * @param username Nome do usuário
     * @param password Senha do usuário
     * @return Caminho para o QR Code gerado
     */
    public String registerUser(String username, String password) throws Exception {
        // Verificar se usuário já existe
        if (userRepository.userExists(username)) {
            throw new IllegalArgumentException("Usuário já existe: " + username);
        }
        
        // Validar entrada
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome de usuário não pode estar vazio");
        }
        
        if (password == null || password.length() < 4) {
            throw new IllegalArgumentException("Senha deve ter pelo menos 4 caracteres");
        }
        
        // Gerar salt e derivar chave da senha
        byte[] salt = KeyDerivation.generateSalt();
        byte[] passwordHash = KeyDerivation.deriveKey(password, salt);
        
        // Gerar secret TOTP
        String totpSecret = totpManager.generateSecret();
        
        // Salvar usuário
        userRepository.saveUser(username, salt, passwordHash, totpSecret);
        
        // Gerar QR Code para configuração 2FA
        String qrCodePath = totpManager.generateQRCode(username, totpSecret);
        
        return qrCodePath;
    }
    
    /**
     * Autentica um usuário com senha + código TOTP
     * 
     * @param username Nome do usuário
     * @param password Senha do usuário
     * @param totpCode Código TOTP de 6 dígitos
     * @return Chave derivada do usuário para criptografia
     */
    public byte[] authenticateUser(String username, String password, String totpCode) throws Exception {
        // Buscar usuário
        User user = userRepository.findUser(username);
        if (user == null) {
            throw new SecurityException("Usuário não encontrado");
        }
        
        // Verificar senha (1º fator)
        if (!KeyDerivation.verifyPassword(password, user.getSalt(), user.getPasswordHash())) {
            throw new SecurityException("Senha incorreta");
        }
        
        // Verificar código TOTP (2º fator)
        if (!totpManager.verifyCode(user.getTotpSecret(), totpCode)) {
            throw new SecurityException("Código 2FA inválido");
        }
        
        // Se chegou até aqui, autenticação foi bem-sucedida
        // Derivar chave para criptografia de arquivos
        return KeyDerivation.deriveKey(password, user.getSalt());
    }
}