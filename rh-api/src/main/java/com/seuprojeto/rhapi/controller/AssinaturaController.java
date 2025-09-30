package com.seuprojeto.rhapi.controller;

import com.seuprojeto.rhapi.domain.AssinaturaToken;
import com.seuprojeto.rhapi.service.AssinaturaService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/assinaturas")
public class AssinaturaController {

    private final AssinaturaService assinaturaService;

    // Use o host-base do application.properties (ex.: http://localhost:8080 ou http://localhost:8080/rh-api)
    private final String hostBase;

    public AssinaturaController(AssinaturaService assinaturaService,
                                @Value("${app.host-base:http://localhost:8080}") String hostBase) {
        this.assinaturaService = assinaturaService;
        this.hostBase = normalizeBase(hostBase);
    }

    /**
     * Cria um token de assinatura para um colaborador e retorna o link público de assinatura.
     * Ex.: POST /assinaturas/criar?colaboradorId=1&de=2025-08-01&ate=2025-08-31&validadeHoras=72
     */
    @PostMapping("/criar")
    public ResponseEntity<?> criar(@RequestParam Long colaboradorId,
                                   @RequestParam LocalDate de,
                                   @RequestParam LocalDate ate,
                                   @RequestParam(defaultValue = "72") int validadeHoras,
                                   HttpServletRequest req) {

        AssinaturaToken t = assinaturaService.criarToken(colaboradorId, de, ate, validadeHoras, hostBase);

        // Link CORRETO para a página pública (singular): /public/assinatura/{token}
        String linkPublico = hostBase + "/public/assinatura/" + t.getToken();

        return ResponseEntity.ok(Map.of(
                "token", t.getToken(),
                "expiraEm", t.getExpiresAt(),
                "link", linkPublico
        ));
    }

    /**
     * (LEGADO) Página de aceite via query param. Mantida por compatibilidade.
     * OBS: esta rota NÃO é pública; em produção prefira SEMPRE o link público /public/assinatura/{token}.
     * GET /assinaturas/aceitar?token=...
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
     * Consulta status de uma assinatura por token (normalmente uso interno).
     * GET /assinaturas/status?token=...
     */
    @GetMapping("/status")
    public ResponseEntity<?> status(@RequestParam String token) {
        var dto = assinaturaService.status(token);
        return ResponseEntity.ok(dto);
    }

    // ===== helpers =====

    private static String firstNonBlank(String a, String b) {
        return (a != null && !a.isBlank()) ? a : (b != null ? b : "");
    }

    private static String normalizeBase(String base) {
        if (base == null || base.isBlank()) return "http://localhost:8080";
        base = base.trim();
        if (base.endsWith("/")) base = base.substring(0, base.length() - 1);
        return base;
    }
}
