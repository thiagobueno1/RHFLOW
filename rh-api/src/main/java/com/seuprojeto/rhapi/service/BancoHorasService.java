package com.seuprojeto.rhapi.service;

import com.seuprojeto.rhapi.domain.BancoDeHoras;
import com.seuprojeto.rhapi.domain.Colaborador;
import com.seuprojeto.rhapi.domain.JornadaTrabalho;
import com.seuprojeto.rhapi.domain.enums.StatusFerias;
import com.seuprojeto.rhapi.dto.ExtratoDiaDTO;
import com.seuprojeto.rhapi.dto.ExtratoPeriodoDTO;
import com.seuprojeto.rhapi.repository.BancoDeHorasRepository;
import com.seuprojeto.rhapi.repository.ColaboradorRepository;
import com.seuprojeto.rhapi.repository.JornadaTrabalhoRepository;
import com.seuprojeto.rhapi.repository.RegistroPontoRepository;
import com.seuprojeto.rhapi.repository.SolicitacaoFeriasRepository;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class BancoHorasService {

    private final RegistroPontoRepository pontoRepo;
    private final JornadaTrabalhoRepository jornadaRepo;
    private final BancoDeHorasRepository bancoRepo;
    private final ColaboradorRepository colabRepo;
    private final SolicitacaoFeriasRepository feriasRepo;

    public BancoHorasService(RegistroPontoRepository pontoRepo,
                             JornadaTrabalhoRepository jornadaRepo,
                             BancoDeHorasRepository bancoRepo,
                             ColaboradorRepository colabRepo,
                             SolicitacaoFeriasRepository feriasRepo) {
        this.pontoRepo = pontoRepo;
        this.jornadaRepo = jornadaRepo;
        this.bancoRepo = bancoRepo;
        this.colabRepo = colabRepo;
        this.feriasRepo = feriasRepo;
    }

    /**
     * Recalcula o saldo do mês (competência) e persiste/atualiza em BancoDeHoras.
     */
    public BancoDeHoras recalcularSaldoMensal(Long colaboradorId, YearMonth competencia) {
        Colaborador colab = colabRepo.findById(colaboradorId)
                .orElseThrow(() -> new IllegalArgumentException("Colaborador não encontrado"));
        JornadaTrabalho jornada = jornadaRepo.findByColaborador_Id(colaboradorId).orElse(null);

        LocalDate ini = competencia.atDay(1);
        LocalDate fim = competencia.atEndOfMonth();

        int saldoMes = 0;
        for (LocalDate d = ini; !d.isAfter(fim); d = d.plusDays(1)) {
            int previsto = minutosPrevistosNoDia(jornada, d.getDayOfWeek());
            int trabalhado = minutosTrabalhadosNoDia(colaboradorId, d);
            saldoMes += (trabalhado - previsto);
        }

        String comp = competencia.toString(); // "YYYY-MM"
        BancoDeHoras banco = bancoRepo.findByColaborador_IdAndCompetencia(colaboradorId, comp)
                .orElseGet(() -> {
                    BancoDeHoras b = new BancoDeHoras();
                    b.setColaborador(colab);
                    b.setCompetencia(comp);
                    return b;
                });
        banco.setSaldoMinutos(saldoMes);
        return bancoRepo.save(banco);
    }

    /**
     * Extrato por período (dia a dia) + enriquecimentos:
     * - bancoAcumuladoMin: saldo anterior (até mês anterior ao 'de') + saldo do período
     * - saldoFeriasDias: (anos completos * 30) - dias aprovados 
     */
    public ExtratoPeriodoDTO extratoPeriodo(Long colaboradorId, LocalDate de, LocalDate ate) {
        if (de.isAfter(ate)) throw new IllegalArgumentException("'de' deve ser antes ou igual a 'ate'");

        Colaborador c = colabRepo.findById(colaboradorId)
                .orElseThrow(() -> new IllegalArgumentException("Colaborador não encontrado"));
        JornadaTrabalho jornada = jornadaRepo.findByColaborador_Id(colaboradorId).orElse(null);

        List<ExtratoDiaDTO> dias = new ArrayList<>();
        int saldoTotal = 0;

        for (LocalDate d = de; !d.isAfter(ate); d = d.plusDays(1)) {
            int previsto = minutosPrevistosNoDia(jornada, d.getDayOfWeek());
            int trabalhado = minutosTrabalhadosNoDia(colaboradorId, d);
            int saldo = trabalhado - previsto;
            dias.add(new ExtratoDiaDTO(d, previsto, trabalhado, saldo));
            saldoTotal += saldo;
        }

        // Banco acumulado = soma de saldos até a competência anterior + saldoTotal do período atual:
        YearMonth compAnterior = YearMonth.from(de).minusMonths(1);
        String atéCompAnterior = compAnterior.toString(); // "YYYY-MM"
        Integer acumuladoAnterior = bancoRepo.sumSaldoAte(colaboradorId, atéCompAnterior);
        if (acumuladoAnterior == null) acumuladoAnterior = 0;
        int bancoAcumuladoMin = acumuladoAnterior + saldoTotal;

        // Saldo de férias (simplificado para TCC):
        int anos = Period.between(c.getDataAdmissao(), LocalDate.now()).getYears();
        int diasAdquiridos = Math.max(anos, 0) * 30;
        int aprovadas = safeInt(feriasRepo.sumDiasByStatus(c.getId(), StatusFerias.APROVADA));
        int saldoFeriasDias = Math.max(diasAdquiridos - aprovadas, 0);

        return new ExtratoPeriodoDTO(
                c.getId(), de, ate, dias, saldoTotal, bancoAcumuladoMin, saldoFeriasDias
        );
    }

    // ----- Helpers -----

    private int minutosTrabalhadosNoDia(Long colaboradorId, LocalDate data) {
        return pontoRepo.findByColaborador_IdAndData(colaboradorId, data)
                .map(r -> {
                    int total = minutosEntre(r.getHoraEntrada(), r.getHoraSaida());
                    if (r.getInicioAlmoco() != null && r.getFimAlmoco() != null) {
                        total -= minutosEntre(r.getInicioAlmoco(), r.getFimAlmoco());
                    }
                    return Math.max(total, 0);
                })
                .orElse(0);
    }

    private int minutosPrevistosNoDia(JornadaTrabalho j, DayOfWeek dow) {
        if (j == null) {
            return switch (dow) {
                case MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY -> 480; // 8h padrão
                default -> 0;
            };
        }
        return switch (dow) {
            case MONDAY -> j.getMinutosSeg();
            case TUESDAY -> j.getMinutosTer();
            case WEDNESDAY -> j.getMinutosQua();
            case THURSDAY -> j.getMinutosQui();
            case FRIDAY -> j.getMinutosSex();
            case SATURDAY -> j.getMinutosSab();
            case SUNDAY -> j.getMinutosDom();
        };
    }

    private int minutosEntre(java.time.LocalTime ini, java.time.LocalTime fim) {
        return (int) java.time.Duration.between(ini, fim).toMinutes();
    }

    private int safeInt(Integer v) { return v == null ? 0 : v; }
}
