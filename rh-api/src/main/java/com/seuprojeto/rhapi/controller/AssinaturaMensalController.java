package com.seuprojeto.rhapi.controller;

import com.seuprojeto.rhapi.domain.AssinaturaMensal;
import com.seuprojeto.rhapi.domain.enums.AssinaturaMensalStatus;
import com.seuprojeto.rhapi.service.AssinaturaMensalService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.Map;

@RestController
@RequestMapping("/assinaturas-mensais")
public class AssinaturaMensalController {

    private final AssinaturaMensalService assinaturaMensalService;

    public AssinaturaMensalController(AssinaturaMensalService assinaturaMensalService) {
        this.assinaturaMensalService = assinaturaMensalService;
    }

    /**
     * GET /assinaturas-mensais/status?colaboradorId=4&competencia=2025-09
     */
    @GetMapping("/status")
    public ResponseEntity<?> status(@RequestParam Long colaboradorId,
                                    @RequestParam String competencia) {
        YearMonth ym = YearMonth.parse(competencia); // "YYYY-MM"
        AssinaturaMensalStatus status = assinaturaMensalService.status(colaboradorId, ym);
        boolean pendenciaAnterior = assinaturaMensalService.temPendenciaAnterior(colaboradorId, ym);
        boolean podeDecidir = YearMonth.now().equals(ym) && !pendenciaAnterior;

        return ResponseEntity.ok(Map.of(
                "status", status.name(),
                "pendenciaAnterior", pendenciaAnterior,
                "podeDecidir", podeDecidir
        ));
    }

    /**
     * POST /assinaturas-mensais/decidir?colaboradorId=4&competencia=2025-09&aceita=true
     */
    @PostMapping("/decidir")
    public ResponseEntity<?> decidir(@RequestParam Long colaboradorId,
                                     @RequestParam String competencia,
                                     @RequestParam boolean aceita,
                                     HttpServletRequest req) {
        YearMonth ym = YearMonth.parse(competencia);
        String ip = firstNonBlank(req.getHeader("X-Forwarded-For"), req.getRemoteAddr());
        String ua = firstNonBlank(req.getHeader("User-Agent"), "-");

        AssinaturaMensal am = assinaturaMensalService.decidir(colaboradorId, ym, aceita, ip, ua);

        return ResponseEntity.ok(Map.of(
                "status", am.getStatus().name(),
                "decididoEm", am.getDecididoEm(),
                "decididoIp", am.getDecididoIp(),
                "decididoUa", am.getDecididoUa()
        ));
    }

    private static String firstNonBlank(String a, String b) {
        return (a != null && !a.isBlank()) ? a : (b != null ? b : "");
    }
}
