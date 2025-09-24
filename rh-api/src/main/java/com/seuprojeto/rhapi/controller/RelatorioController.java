package com.seuprojeto.rhapi.controller;

import com.seuprojeto.rhapi.service.RelatorioEmailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;

@RestController
@RequestMapping("/relatorios")
@Tag(name = "Relatórios")
public class RelatorioController {

    private final RelatorioEmailService service;

    public RelatorioController(RelatorioEmailService service) {
        this.service = service;
    }

    @Operation(summary = "Envia por e-mail o relatório mensal do colaborador (com botão de assinatura se hostBase informado)")
    @PostMapping("/mensal")
    public ResponseEntity<?> enviarMensal(@RequestParam Long colaboradorId,
                                          @RequestParam String competencia,
                                          @RequestParam(required = false) String hostBase) {
        try {
            YearMonth ym = YearMonth.parse(competencia); // ex: 2025-08
            service.enviarRelatorioMensal(colaboradorId, ym, hostBase); // <- hostBase opcional
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Falha ao enviar: " + e.getMessage());
        }
    }
}
