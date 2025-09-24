package com.seuprojeto.rhapi.controller;

import com.seuprojeto.rhapi.domain.Colaborador;
import com.seuprojeto.rhapi.dto.ExtratoDiaDTO;
import com.seuprojeto.rhapi.repository.ColaboradorRepository;
import com.seuprojeto.rhapi.repository.SolicitacaoFeriasRepository;
import com.seuprojeto.rhapi.domain.enums.StatusFerias;
import com.seuprojeto.rhapi.service.BancoHorasService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

@RestController
@RequestMapping("/banco-horas")
public class BancoHorasController {

    private final BancoHorasService service;
    private final ColaboradorRepository colabRepo;
    private final SolicitacaoFeriasRepository feriasRepo;

    public BancoHorasController(BancoHorasService service,
                                ColaboradorRepository colabRepo,
                                SolicitacaoFeriasRepository feriasRepo) {
        this.service = service;
        this.colabRepo = colabRepo;
        this.feriasRepo = feriasRepo;
    }


    // EXTRATO (com férias)
 
    @GetMapping("/extrato")
    public ResponseEntity<?> extrato(@RequestParam Long colaboradorId,
                                     @RequestParam LocalDate de,
                                     @RequestParam LocalDate ate) {
        try {
            var dtoBase = service.extratoPeriodo(colaboradorId, de, ate); // dias + saldoTotalMin
            var colab = colabRepo.findById(colaboradorId)
                    .orElseThrow(() -> new IllegalArgumentException("Colaborador não encontrado"));

            // 1) Saldo de férias proporcional:
            int saldoFeriasDias = calcularSaldoFeriasDisponivel(colab);

           
            int bancoAcumuladoMin = dtoBase.saldoTotalMin();

            
            var resposta = new ExtratoPeriodoOutDTO(
                    dtoBase.colaboradorId(),
                    dtoBase.de(),
                    dtoBase.ate(),
                    dtoBase.dias(),
                    dtoBase.saldoTotalMin(),
                    bancoAcumuladoMin,
                    saldoFeriasDias
            );

            return ResponseEntity.ok(resposta);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body("Erro ao gerar extrato: " + ex.getMessage());
        }
    }


    private int calcularSaldoFeriasDisponivel(Colaborador c) {
        LocalDate adm = c.getDataAdmissao();
        if (adm == null) return 0;

        LocalDate hoje = LocalDate.now();
        if (hoje.isBefore(adm)) return 0;

        Period p = Period.between(adm, hoje);
        int meses = p.getYears() * 12 + p.getMonths();

        int adquiridos = (int) Math.floor(meses * 2.5);
        if (adquiridos > 30) adquiridos = 30; // teto do ciclo atual

        // Considera pedidos em aberto (CRIADA) e já aprovados (APROVADA) como "consumo" do saldo disponível
        int consumidos = feriasRepo.sumDiasByStatuses(
                c.getId(),
                List.of(StatusFerias.CRIADA, StatusFerias.APROVADA)
        );

        int saldo = adquiridos - consumidos;
        if (saldo < 0) saldo = 0;
        if (saldo > 30) saldo = 30;

        return saldo;
    }


    // DTO de saída
    public record ExtratoPeriodoOutDTO(
            Long colaboradorId,
            LocalDate de,
            LocalDate ate,
            List<ExtratoDiaDTO> dias,
            int saldoTotalMin,
            int bancoAcumuladoMin,
            int saldoFeriasDias
    ) {}

}
