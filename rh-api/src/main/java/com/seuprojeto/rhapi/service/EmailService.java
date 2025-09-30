package com.seuprojeto.rhapi.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.lang.Nullable;
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
                                       @Nullable String attachName,
                                       @Nullable byte[] data,
                                       @Nullable String contentType) {
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, StandardCharsets.UTF_8.name());
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);

            if (data != null && data.length > 0 && attachName != null && !attachName.isBlank()) {
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

    // ==========================
    // HELPERS para Assinatura
    // ==========================

    /**
     * Envia o e-mail de solicitação de assinatura com DOIS botões:
     *  - Estou de acordo  -> /public/assinaturas/{token}/sim
     *  - Não estou de acordo -> /public/assinaturas/{token}/nao
     *
     * @param to           e-mail do colaborador
     * @param subject      assunto do e-mail
     * @param hostBase     ex.: "http://localhost:8080" ou "https://sua-api.com"
     * @param token        token de assinatura (gerado no AssinaturaService)
     * @param messageHtml  conteúdo opcional antes dos botões (HTML simples)
     */
    public void sendSignatureRequest(String to,
                                     String subject,
                                     String hostBase,
                                     String token,
                                     @Nullable String messageHtml) {
        String base = normalizeHost(hostBase);
        String urlSim = base + "/public/assinaturas/" + token + "/sim";
        String urlNao = base + "/public/assinaturas/" + token + "/nao";
        String html = buildSignatureHtmlWithTwoButtons(subject, orEmpty(messageHtml), urlSim, urlNao);
        sendHtmlWithAttachment(to, subject, html, null, null, null);
    }

    /**
     * Mesmo de cima, mas permitindo anexar um arquivo (ex.: PDF do holerite).
     */
    public void sendSignatureRequestWithAttachment(String to,
                                                   String subject,
                                                   String hostBase,
                                                   String token,
                                                   @Nullable String messageHtml,
                                                   @Nullable String attachName,
                                                   @Nullable byte[] data,
                                                   @Nullable String contentType) {
        String base = normalizeHost(hostBase);
        String urlSim = base + "/public/assinaturas/" + token + "/sim";
        String urlNao = base + "/public/assinaturas/" + token + "/nao";
        String html = buildSignatureHtmlWithTwoButtons(subject, orEmpty(messageHtml), urlSim, urlNao);
        sendHtmlWithAttachment(to, subject, html, attachName, data, contentType);
    }

    /**
     * HTML minimalista (CSS inline) com dois botões e fallback dos links em texto.
     */
    private String buildSignatureHtmlWithTwoButtons(String subject, String contentHtml, String urlSim, String urlNao) {
        return """
        <!doctype html>
        <html lang="pt-br">
        <head><meta charset="utf-8"/></head>
        <body style="font-family:Arial,Helvetica,sans-serif;color:#222;margin:0;padding:0;">
          <div style="max-width:680px;margin:0 auto;padding:24px;border:1px solid #eee;border-radius:8px;">
            <h2 style="margin-top:0;">%s</h2>
            %s
            <p style="margin:12px 0 20px;">Você confirma as informações?</p>
            <div>
              <a href="%s" style="background:#16a34a;color:#fff;padding:10px 16px;text-decoration:none;border-radius:6px;display:inline-block;margin-right:8px;">
                Estou de acordo
              </a>
              <a href="%s" style="background:#dc2626;color:#fff;padding:10px 16px;text-decoration:none;border-radius:6px;display:inline-block;">
                Não estou de acordo
              </a>
            </div>
            <p style="margin-top:12px;font-size:12px;color:#666;">
              Se os botões não funcionarem, copie e cole:<br/>
              <span style="word-break:break-all;">%s</span> (Sim)<br/>
              <span style="word-break:break-all;">%s</span> (Não)
            </p>
            <hr style="border:none;border-top:1px solid #eee;margin:20px 0;"/>
            <p style="margin:0;font-size:12px;color:#666;">
              Mensagem automática do sistema <strong>RHFlow API</strong>.
            </p>
          </div>
        </body>
        </html>
        """.formatted(
                escape(subject),
                contentHtml,               // já é HTML controlado pela sua app
                escapeUrl(urlSim),
                escapeUrl(urlNao),
                escape(urlSim),
                escape(urlNao)
        );
    }

    // ==========================
    // Utils
    // ==========================

    private static String normalizeHost(String hostBase) {
        if (hostBase == null || hostBase.isBlank()) return "";
        String h = hostBase.trim();
        while (h.endsWith("/")) h = h.substring(0, h.length() - 1);
        return h;
    }

    private static String orEmpty(String s) {
        return (s == null) ? "" : s;
    }

    // Escapes simples para evitar quebrar o HTML em campos de texto
    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("&","&amp;")
                .replace("<","&lt;")
                .replace(">","&gt;")
                .replace("\"","&quot;");
    }

    // Para URLs no atributo href
    private static String escapeUrl(String s) {
        if (s == null) return "";
        return s.replace("&","&amp;")
                .replace("\"","&quot;");
    }
}
