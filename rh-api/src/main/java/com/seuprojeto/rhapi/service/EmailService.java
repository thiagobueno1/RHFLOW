package com.seuprojeto.rhapi.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final String from;

    public EmailService(JavaMailSender mailSender,
                        @Value("${spring.mail.from}") String from) {
        this.mailSender = mailSender;
        this.from = from;
    }

    /** Envia texto simples (sem anexo) */
    public void sendText(String to, String subject, String body) {
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, false, StandardCharsets.UTF_8.name());
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, false);
            mailSender.send(msg);
        } catch (MessagingException e) {
            throw new IllegalStateException("Falha ao montar/enviar e-mail de texto.", e);
        }
    }

    /** Envia HTML e, opcionalmente, um anexo */
    public void sendHtmlWithAttachment(String to,
                                       String subject,
                                       String html,
                                       String attachName,
                                       byte[] data,
                                       String contentType) {
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, StandardCharsets.UTF_8.name());
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);

            if (data != null && data.length > 0) {
                ByteArrayResource res = new ByteArrayResource(data);
                String ct = (contentType != null && !contentType.isBlank())
                        ? contentType
                        : "application/octet-stream";
                helper.addAttachment(attachName, res, ct);
            }

            mailSender.send(msg);
        } catch (MessagingException e) {
            throw new IllegalStateException("Falha ao montar/enviar e-mail HTML com anexo.", e);
        }
    }
}
