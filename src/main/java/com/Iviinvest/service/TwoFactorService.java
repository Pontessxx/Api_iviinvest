// service/TwoFactorService.java
package com.Iviinvest.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.*;

import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.MimeMessageHelper;

@Service
public class TwoFactorService {

    private final Map<String, TokenInfo> tokenStorage = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    @Autowired
    private JavaMailSender mailSender;

    public void generateAndSendToken(String email) {
        String code = String.format("%06d", new Random().nextInt(999999));
        Instant expiresAt = Instant.now().plusSeconds(300); // 5 minutos

        tokenStorage.put(email, new TokenInfo(code, expiresAt));

        scheduler.schedule(() -> tokenStorage.remove(email), 5, TimeUnit.MINUTES);

        sendEmail(email, code);
    }

    public boolean verifyToken(String email, String token) {
        TokenInfo info = tokenStorage.get(email);
        if (info == null || Instant.now().isAfter(info.expiresAt)) {
            return false;
        }
        boolean isValid = info.token.equals(token);
        if (isValid) {
            tokenStorage.remove(email);
        }
        return isValid;
    }

    private void sendEmail(String to, String code) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(to);
            helper.setSubject("Código de Verificação - Ivi Invest");
            helper.setText("Seu código de verificação é: <b>" + code + "</b>", true);
            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao enviar email", e);
        }
    }

    private record TokenInfo(String token, Instant expiresAt) {}
}
