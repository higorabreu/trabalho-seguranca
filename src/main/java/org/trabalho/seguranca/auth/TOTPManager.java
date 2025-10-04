package org.trabalho.seguranca.auth;

import dev.samstevens.totp.code.*;
import dev.samstevens.totp.exceptions.QrGenerationException;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TOTPManager {
    
    private static final String ISSUER = "Servidor Nuvem Simulado";
    private static final int TIME_PERIOD = 30; // segundos
    private static final int CODE_DIGITS = 6;
    
    private final SecretGenerator secretGenerator;
    private final TimeProvider timeProvider;
    private final CodeGenerator codeGenerator;
    private final CodeVerifier codeVerifier;
    private final QrGenerator qrGenerator;
    
    public TOTPManager() {
        this.secretGenerator = new DefaultSecretGenerator();
        this.timeProvider = new SystemTimeProvider();
        this.codeGenerator = new DefaultCodeGenerator();
        this.codeVerifier = new DefaultCodeVerifier(codeGenerator, timeProvider);
        this.qrGenerator = new ZxingPngQrGenerator();
    }
    
    public String generateSecret() {
        return secretGenerator.generate();
    }
    
    /**
     * Gera um QR Code para configuração no aplicativo autenticador
     * 
     * @param username Nome do usuário
     * @param secret Secret TOTP em Base32
     * @return Caminho para o arquivo PNG do QR Code
     */
    public String generateQRCode(String username, String secret) throws QrGenerationException, IOException {
        QrData data = new QrData.Builder()
            .label(username)
            .secret(secret)
            .issuer(ISSUER)
            .algorithm(HashingAlgorithm.SHA1)
            .digits(CODE_DIGITS)
            .period(TIME_PERIOD)
            .build();
            
        byte[] qrCodeImage = qrGenerator.generate(data);
        
        // salvar QR Code em arquivo
        Path qrCodePath = Paths.get("storage", "qr_" + username + ".png");
        Files.createDirectories(qrCodePath.getParent());
        Files.write(qrCodePath, qrCodeImage);
        
        return qrCodePath.toAbsolutePath().toString();
    }
    
    /**
     * Verifica um código TOTP fornecido pelo usuário
     * 
     * @param secret Secret TOTP do usuário
     * @param code Código de 6 dígitos fornecido
     * @return true se o código estiver válido
     */
    public boolean verifyCode(String secret, String code) {
        try {
            return codeVerifier.isValidCode(secret, code);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Gera o código TOTP atual para um secret (útil para testes)
     * 
     * @param secret Secret TOTP
     * @return Código atual de 6 dígitos
     */
    public String getCurrentCode(String secret) throws Exception {
        long timeWindow = timeProvider.getTime() / TIME_PERIOD;
        return codeGenerator.generate(secret, timeWindow);
    }
    
    /**
     * Obtém a URI OTP para configuração manual
     * 
     * @param username Nome do usuário
     * @param secret Secret TOTP
     * @return URI no formato otpauth://
     */
    public String getOTPUri(String username, String secret) {
        return String.format(
            "otpauth://totp/%s:%s?secret=%s&issuer=%s&algorithm=SHA1&digits=%d&period=%d",
            ISSUER, username, secret, ISSUER, CODE_DIGITS, TIME_PERIOD
        );
    }
}