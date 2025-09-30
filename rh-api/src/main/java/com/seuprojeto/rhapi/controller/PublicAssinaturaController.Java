package com.seuprojeto.rhapi.controller;

import com.seuprojeto.rhapi.domain.enums.AssinaturaStatus;
import com.seuprojeto.rhapi.service.AssinaturaService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/public/assinatura")
public class PublicAssinaturaController {

    private final AssinaturaService assinaturaService;

    // --- Ping
    @GetMapping("/ping")
    public String ping() { return "ok"; }

    // --- Página/stub de status
    @GetMapping(value = "/{token}", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> pagina(@PathVariable String token) {
        var dto = assinaturaService.status(token);

        String status = String.valueOf(dto.get("status"));
        String de = String.valueOf(dto.getOrDefault("de", ""));
        String ate = String.valueOf(dto.getOrDefault("ate", ""));

        String html = """
        <!doctype html>
        <html lang="pt-br"><head><meta charset="utf-8"/><title>Status da assinatura</title></head>
        <body style="font-family:Arial,Helvetica,sans-serif;padding:24px;background:#f6f7fb">
          <div style="max-width:560px;margin:0 auto;background:#fff;border-radius:12px;padding:20px;box-shadow:0 6px 18px rgba(0,0,0,.06)">
            <h2 style="margin:0 0 12px 0;">Status da assinatura</h2>
            <p style="margin:8px 0;color:#555;">Competência: <strong>%s a %s</strong></p>
            <p style="margin:8px 0;color:#555;">Status: <strong>%s</strong></p>
          </div>
        </body></html>
        """.formatted(safe(de), safe(ate), safe(status));

        return ResponseEntity.ok(html);
    }

    // --- NOVO: decisão via GET (para botões do e-mail)
    @GetMapping(value = "/{token}/decidir", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> decidirGet(@PathVariable String token,
                                             @RequestParam("acao") String acao,
                                             HttpServletRequest req) {
        boolean aceita = "sim".equalsIgnoreCase(acao);
        return decidirHtml(token, aceita, req);
    }

    // --- (Opcional) decisão via POST (usada pela página /{token})
    @PostMapping(value = "/{token}/decidir",
                 consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
                 produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> decidirPost(@PathVariable String token,
                                              @RequestParam("acao") String acao,
                                              HttpServletRequest req) {
        boolean aceita = "sim".equalsIgnoreCase(acao);
        return decidirHtml(token, aceita, req);
    }

    // ===== helpers =====

    private ResponseEntity<String> decidirHtml(String token, boolean aceita, HttpServletRequest req) {
        String ip = first(req.getHeader("X-Forwarded-For"), req.getRemoteAddr());
        String ua = first(req.getHeader("User-Agent"), "-");

        var t = assinaturaService.decidir(token, aceita, ip, ua);

        String msg = switch (t.getStatus()) {
            case ASSINADO -> "Confirmação registrada. Obrigado!";
            case RECUSADO -> "Sua negativa foi registrada.";
            case EXPIRADO -> "Token expirado. Não foi possível registrar.";
            default -> "Nada a fazer.";
        };

        String html = """
        <!doctype html>
        <html lang="pt-br"><head><meta charset="utf-8"/><title>Confirmação</title></head>
        <body style="font-family:Arial,Helvetica,sans-serif;padding:24px;background:#f6f7fb">
          <div style="max-width:560px;margin:0 auto;background:#fff;border-radius:12px;padding:20px;box-shadow:0 6px 18px rgba(0,0,0,.06)">
            <h2 style="margin:0 0 12px 0;">%s</h2>
            <p style="margin:8px 0;color:#555;">Status atual: <strong>%s</strong></p>
            <a href="/public/assinatura/%s" style="display:inline-block;margin-top:8px;">Ver status</a>
          </div>
        </body></html>
        """.formatted(msg, t.getStatus().name(), t.getToken());

        return ResponseEntity.ok(html);
    }

    private static String first(String a, String b) {
        return (a != null && !a.isBlank()) ? a : (b != null ? b : "");
    }

    private static String safe(String s) {
        return s == null ? "" : s.replace("&", "&amp;")
                                 .replace("<", "&lt;")
                                 .replace(">", "&gt;")
                                 .replace("\"", "&quot;");
    }
}
