package com.seuprojeto.rhapi.controller;

import com.seuprojeto.rhapi.security.CurrentUserService;
import com.seuprojeto.rhapi.service.AssinaturaMensalService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.Map;

@RestController
@RequestMapping("/assinaturas/mensal")
public class AssinaturaMensalController {

    private final AssinaturaMensalService service;
    private final CurrentUserService currentUser;

    public AssinaturaMensalController(AssinaturaMensalService service,
                                      CurrentUserService currentUser) {
        this.service = service;
        this.currentUser = currentUser;
    }

    @GetMapping("/status")
    public ResponseEntity<?> status(@RequestParam String competencia) {
        Long colaboradorId = currentUser.getColaboradorIdOrThrow();
        YearMonth ym = YearMonth.parse(competencia);
        var st = service.status(colaboradorId, ym);
        boolean pendenciaAnterior = service.temPendenciaAnterior(colaboradorId, ym);
        return ResponseEntity.ok(Map.of(
            "competencia", competencia,
            "status", st.name(),
            "pendenciaAnterior", pendenciaAnterior
        ));
    }

    @PostMapping("/decidir")
    public ResponseEntity<?> decidir(@RequestBody DecisaoReq req, HttpServletRequest http) {
        Long colaboradorId = currentUser.getColaboradorIdOrThrow();
        YearMonth ym = YearMonth.parse(req.competencia());
        String ip = firstNonBlank(http.getHeader("X-Forwarded-For"), http.getRemoteAddr());
        String ua = firstNonBlank(http.getHeader("User-Agent"), "-");

        var r = service.decidir(colaboradorId, ym, req.aceita(), ip, ua);
        return ResponseEntity.ok(Map.of(
            "status", r.getStatus().name(),
            "decididoEm", r.getDecididoEm()
        ));
    }

    public record DecisaoReq(String competencia, boolean aceita) {}

    private static String firstNonBlank(String a, String b) {
        return (a != null && !a.isBlank()) ? a : (b != null ? b : "");
    }
}
