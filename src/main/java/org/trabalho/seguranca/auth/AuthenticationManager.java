package org.trabalho.seguranca.auth;

import org.trabalho.seguranca.crypto.KeyDerivation;
import org.trabalho.seguranca.storage.UserRepository;
import org.trabalho.seguranca.storage.User;

// gerencia autenticacao com senha + totp
public class AuthenticationManager {
    
    private final UserRepository userRepository;
    private final TOTPManager totpManager;
    
    public AuthenticationManager(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.totpManager = new TOTPManager();
    }
    
    // registra usuario com 2fa e retorna caminho do qr code
    public String registerUser(String username, String password) throws Exception {
        // verifica se usuario ja existe
        if (userRepository.userExists(username)) {
            throw new IllegalArgumentException("Usuário já existe: " + username);
        }
        
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome de usuário não pode estar vazio");
        }
        
        if (password == null || password.length() < 4) {
            throw new IllegalArgumentException("Senha deve ter pelo menos 4 caracteres");
        }
        
        // gerar salt e derivar chave da senha
        byte[] salt = KeyDerivation.generateSalt();
        byte[] passwordHash = KeyDerivation.deriveKey(password, salt);
        
        // gerar secret totp
        String totpSecret = totpManager.generateSecret();
        
        // salvar usuario
        userRepository.saveUser(username, salt, passwordHash, totpSecret);
        
        // gerar qr code para configuracao 2fa
        String qrCodePath = totpManager.generateQRCode(username, totpSecret);
        
        return qrCodePath;
    }
    
    // autentica usuario com senha + codigo totp e retorna chave derivada
    public byte[] authenticateUser(String username, String password, String totpCode) throws Exception {
        // buscar usuario
        User user = userRepository.findUser(username);
        if (user == null) {
            throw new SecurityException("Usuário não encontrado");
        }
        
        // verificar senha (1o fator)
        if (!KeyDerivation.verifyPassword(password, user.getSalt(), user.getPasswordHash())) {
            throw new SecurityException("Senha incorreta");
        }
        
        // verificar codigo totp (2o fator)
        if (!totpManager.verifyCode(user.getTotpSecret(), totpCode)) {
            throw new SecurityException("Código 2FA inválido");
        }
        
        // derivar chave para criptografia de arquivos
        return KeyDerivation.deriveKey(password, user.getSalt());
    }
}