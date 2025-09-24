package com.seuprojeto.rhapi.controller;

import com.seuprojeto.rhapi.domain.AssinaturaToken;
import com.seuprojeto.rhapi.service.AssinaturaService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/assinaturas")
public class AssinaturaController {

    private final AssinaturaService assinaturaService;

    public AssinaturaController(AssinaturaService assinaturaService) {
        this.assinaturaService = assinaturaService;
    }

    /**
     * Cria um token de assinatura para um colaborador e retorna o link para assinatura.
     * Ex.: POST /assinaturas/criar?colaboradorId=1&de=2025-08-01&ate=2025-08-31&validadeHoras=72
     */
    @PostMapping("/criar")
    public ResponseEntity<?> criar(@RequestParam Long colaboradorId,
                                   @RequestParam LocalDate de,
                                   @RequestParam LocalDate ate,
                                   @RequestParam(defaultValue = "72") int validadeHoras,
                                   HttpServletRequest req) {

        // host base para montar link (ajuste se usar proxy/reverse-proxy em produção)
        String hostBase = req.getScheme() + "://" + req.getServerName()
                + ((req.getServerPort() == 80 || req.getServerPort() == 443) ? "" : ":" + req.getServerPort());

        AssinaturaToken t = assinaturaService.criarToken(colaboradorId, de, ate, validadeHoras, hostBase);

        String link = hostBase + "/assinaturas/aceitar?token=" + t.getToken();
        return ResponseEntity.ok(Map.of(
                "token", t.getToken(),
                "expiraEm", t.getExpiresAt(),
                "link", link
        ));
    }

    /**
     * Página de aceite de assinatura (pública).
     * GET /assinaturas/aceitar?token=...
     * Retorna um HTML simples de sucesso/erro.
     */
    @GetMapping("/aceitar")
    public ResponseEntity<String> aceitar(@RequestParam String token, HttpServletRequest req) {
        String ip = firstNonBlank(req.getHeader("X-Forwarded-For"), req.getRemoteAddr());
        String ua = firstNonBlank(req.getHeader("User-Agent"), "-");

        boolean ok = assinaturaService.aceitar(token, ip, ua);

        String htmlOk = """
                <!doctype html>
                <html lang="pt-br">
                <head><meta charset="utf-8"/><title>Assinatura registrada</title></head>
                <body style="font-family:Arial,Helvetica,sans-serif;padding:24px;background:#f6f7fb">
                  <div style="max-width:680px;margin:0 auto;background:#fff;border-radius:12px;padding:20px;box-shadow:0 6px 18px rgba(0,0,0,.06)">
                    <h2 style="margin:0 0 10px 0;">Assinatura registrada com sucesso</h2>
                    <p style="margin:0;">Seu aceite foi registrado. Você já pode fechar esta página.</p>
                  </div>
                </body>
                </html>
                """;

        String htmlErro = """
                <!doctype html>
                <html lang="pt-br">
                <head><meta charset="utf-8"/><title>Assinatura inválida</title></head>
                <body style="font-family:Arial,Helvetica,sans-serif;padding:24px;background:#f6f7fb">
                  <div style="max-width:680px;margin:0 auto;background:#fff;border-radius:12px;padding:20px;box-shadow:0 6px 18px rgba(0,0,0,.06)">
                    <h2 style="margin:0 0 10px 0;">Não foi possível registrar sua assinatura</h2>
                    <p style="margin:0;">O token é inválido, expirado, ou já foi utilizado.</p>
                  </div>
                </body>
                </html>
                """;

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "text/html; charset=UTF-8")
                .body(ok ? htmlOk : htmlErro);
    }

    /**
     * Consulta status de uma assinatura por token (público).
     * GET /assinaturas/status?token=...
     */
    @GetMapping("/status")
    public ResponseEntity<?> status(@RequestParam String token) {
        // O service retorna um DTO/Map com informações do token (status, expiracao, etc.)
        var dto = assinaturaService.status(token);
        return ResponseEntity.ok(dto);
    }

    private static String firstNonBlank(String a, String b) {
        return (a != null && !a.isBlank()) ? a : (b != null ? b : "");
    }
}
