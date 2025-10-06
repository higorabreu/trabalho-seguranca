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
    
    // gera qr code para configuracao no app autenticador
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
        
        // salva qr code em arquivo
        Path qrCodePath = Paths.get("storage", "qr_" + username + ".png");
        Files.createDirectories(qrCodePath.getParent());
        Files.write(qrCodePath, qrCodeImage);
        
        return qrCodePath.toAbsolutePath().toString();
    }
    
    // verifica codigo totp fornecido pelo usuario
    public boolean verifyCode(String secret, String code) {
        try {
            return codeVerifier.isValidCode(secret, code);
        } catch (Exception e) {
            return false;
        }
    }
    
    // gera codigo totp atual (util para testes)
    public String getCurrentCode(String secret) throws Exception {
        long timeWindow = timeProvider.getTime() / TIME_PERIOD;
        return codeGenerator.generate(secret, timeWindow);
    }
    
    // obtem uri otp para configuracao manual
    public String getOTPUri(String username, String secret) {
        return String.format(
            "otpauth://totp/%s:%s?secret=%s&issuer=%s&algorithm=SHA1&digits=%d&period=%d",
            ISSUER, username, secret, ISSUER, CODE_DIGITS, TIME_PERIOD
        );
    }
}