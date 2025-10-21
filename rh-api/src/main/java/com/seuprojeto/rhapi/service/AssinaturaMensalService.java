package com.seuprojeto.rhapi.service;

import com.seuprojeto.rhapi.domain.AssinaturaMensal;
import com.seuprojeto.rhapi.domain.Colaborador;
import com.seuprojeto.rhapi.domain.enums.AssinaturaMensalStatus;
import com.seuprojeto.rhapi.repository.AssinaturaMensalRepository;
import com.seuprojeto.rhapi.repository.ColaboradorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;

@Service
public class AssinaturaMensalService {

    private final AssinaturaMensalRepository repo;
    private final ColaboradorRepository colabRepo;

    public AssinaturaMensalService(AssinaturaMensalRepository repo, ColaboradorRepository colabRepo) {
        this.repo = repo;
        this.colabRepo = colabRepo;
    }

    /** Converte YearMonth -> inteiro YYYYMM (ex.: 2025-09 -> 202509) */
    public static int ymToInt(YearMonth ym) {
        return ym.getYear() * 100 + ym.getMonthValue();
    }

    /** Cria (se não existir) e marca que o e-mail foi enviado para a competência informada. */
    @Transactional
    public AssinaturaMensal marcarEmailEnviado(Long colaboradorId, YearMonth competencia) {
        Colaborador c = colabRepo.findById(colaboradorId)
            .orElseThrow(() -> new IllegalArgumentException("Colaborador não encontrado"));
        int comp = ymToInt(competencia);

        AssinaturaMensal am = repo.findByColaboradorAndCompetencia(c, comp)
            .orElseGet(() -> {
                AssinaturaMensal novo = new AssinaturaMensal();
                novo.setColaborador(c);
                novo.setCompetencia(comp);
                novo.setStatus(AssinaturaMensalStatus.PENDENTE);
                return novo;
            });

        am.setEmailSentAt(LocalDateTime.now());
        return repo.save(am);
    }

    /** Consulta o status da assinatura na competência (default: PENDENTE se não existe registro). */
    @Transactional(readOnly = true)
    public AssinaturaMensalStatus status(Long colaboradorId, YearMonth competencia) {
        Colaborador c = colabRepo.findById(colaboradorId)
            .orElseThrow(() -> new IllegalArgumentException("Colaborador não encontrado"));
        return repo.findByColaboradorAndCompetencia(c, ymToInt(competencia))
            .map(AssinaturaMensal::getStatus)
            .orElse(AssinaturaMensalStatus.PENDENTE);
    }

    /**
     * **Modo flexibilizado (TCC/demo):**
     * Registra a decisão para QUALQUER competência, sem exigir:
     *  - que seja a competência atual;
     *  - que o mês anterior esteja assinado;
     *  - que o e-mail tenha sido enviado.
     * Única regra: deve existir o registro de assinatura para a competência.
     */
    @Transactional
    public AssinaturaMensal decidir(Long colaboradorId, YearMonth competencia, boolean aceita, String ip, String ua) {
        Colaborador c = colabRepo.findById(colaboradorId)
            .orElseThrow(() -> new IllegalArgumentException("Colaborador não encontrado"));

        int comp = ymToInt(competencia);

        // ÚNICA validação: precisa existir o registro dessa competência
        AssinaturaMensal am = repo.findByColaboradorAndCompetencia(c, comp)
            .orElseThrow(() -> new IllegalStateException("Este relatório ainda não foi disponibilizado para aceite."));

        // Idempotente se já houver decisão final
        if (am.getStatus() == AssinaturaMensalStatus.ASSINADO ||
            am.getStatus() == AssinaturaMensalStatus.RECUSADO) {
            return am;
        }

        am.setStatus(aceita ? AssinaturaMensalStatus.ASSINADO : AssinaturaMensalStatus.RECUSADO);
        am.setDecididoEm(LocalDateTime.now());
        am.setDecididoIp(ip);
        am.setDecididoUa(ua);

        return repo.save(am);
    }

    /**
     * Mantive este helper caso o front ainda queira exibir aviso de pendências.
     * Ele **não bloqueia** mais nada no servidor.
     */
    @Transactional(readOnly = true)
    public boolean temPendenciaAnterior(Long colaboradorId, YearMonth competenciaAtual) {
        Colaborador c = colabRepo.findById(colaboradorId)
            .orElseThrow(() -> new IllegalArgumentException("Colaborador não encontrado"));
        YearMonth anterior = competenciaAtual.minusMonths(1);
        return repo.findByColaboradorAndCompetencia(c, ymToInt(anterior))
            .map(a -> a.getStatus() != AssinaturaMensalStatus.ASSINADO)
            .orElse(false);
    }
}
