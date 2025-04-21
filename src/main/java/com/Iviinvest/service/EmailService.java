package com.Iviinvest.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Serviço para envio de e-mails.
 * <p>
 * Responsável pelo envio de e-mails simples utilizando o JavaMailSender do Spring.
 * Pode ser utilizado para notificações, recuperação de senha, etc.
 * <p>
 * Email sending service.
 * Responsible for sending simple emails using Spring's JavaMailSender.
 * Can be used for notifications, password recovery, etc.
 */
@Service
public class EmailService {

    private final JavaMailSender mailSender;

    /**
     * Construtor para injeção de dependência do JavaMailSender.
     *
     * @param mailSender Instância do JavaMailSender configurado
     *
     * Constructor for JavaMailSender dependency injection.
     *
     * @param mailSender Configured JavaMailSender instance
     */
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Envia um e-mail simples.
     *
     * @param para Endereço de e-mail do destinatário
     * @param assunto Assunto do e-mail
     * @param corpo Corpo/texto do e-mail
     *
     */
    public void enviar(String para, String assunto, String corpo) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(para);
        message.setSubject(assunto);
        message.setText(corpo);

        try {
            mailSender.send(message);
            // Log de sucesso poderia ser adicionado aqui
            // Success log could be added here
        } catch (Exception e) {
            // Log de erro poderia ser adicionado aqui
            // Error log could be added here
            throw new RuntimeException("Falha ao enviar e-mail | Failed to send email", e);
        }
    }
}