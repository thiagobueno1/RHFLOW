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

    public static int ymToInt(YearMonth ym) {
        return ym.getYear() * 100 + ym.getMonthValue();
    }

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

    @Transactional(readOnly = true)
    public AssinaturaMensalStatus status(Long colaboradorId, YearMonth competencia) {
        Colaborador c = colabRepo.findById(colaboradorId)
            .orElseThrow(() -> new IllegalArgumentException("Colaborador não encontrado"));
        return repo.findByColaboradorAndCompetencia(c, ymToInt(competencia))
            .map(AssinaturaMensal::getStatus)
            .orElse(AssinaturaMensalStatus.PENDENTE);
    }

    @Transactional
    public AssinaturaMensal decidir(Long colaboradorId, YearMonth competencia, boolean aceita, String ip, String ua) {
        Colaborador c = colabRepo.findById(colaboradorId)
            .orElseThrow(() -> new IllegalArgumentException("Colaborador não encontrado"));

        YearMonth agora = YearMonth.now();
        if (!competencia.equals(agora)) {
            throw new IllegalStateException("Só é permitido decidir a competência atual.");
        }

        // Exige mês anterior ACEITO
        YearMonth anterior = competencia.minusMonths(1);
        repo.findByColaboradorAndCompetencia(c, ymToInt(anterior))
            .ifPresent(prev -> {
                if (prev.getStatus() != AssinaturaMensalStatus.ACEITO) {
                    throw new IllegalStateException("Você possui pendências no mês anterior.");
                }
            });

        AssinaturaMensal am = repo.findByColaboradorAndCompetencia(c, ymToInt(competencia))
            .orElseThrow(() -> new IllegalStateException("Este relatório ainda não foi disponibilizado para aceite."));

        if (am.getEmailSentAt() == null) {
            throw new IllegalStateException("Este relatório ainda não foi enviado por e-mail.");
        }

        if (am.getStatus() == AssinaturaMensalStatus.ACEITO ||
            am.getStatus() == AssinaturaMensalStatus.RECUSADO) {
            return am; // idempotente
        }

        am.setStatus(aceita ? AssinaturaMensalStatus.ACEITO : AssinaturaMensalStatus.RECUSADO);
        am.setDecididoEm(LocalDateTime.now());
        am.setDecididoIp(ip);
        am.setDecididoUa(ua);
        return repo.save(am);
    }

    @Transactional(readOnly = true)
    public boolean temPendenciaAnterior(Long colaboradorId, YearMonth competenciaAtual) {
        Colaborador c = colabRepo.findById(colaboradorId)
            .orElseThrow(() -> new IllegalArgumentException("Colaborador não encontrado"));
        YearMonth anterior = competenciaAtual.minusMonths(1);
        return repo.findByColaboradorAndCompetencia(c, ymToInt(anterior))
            .map(a -> a.getStatus() != AssinaturaMensalStatus.ACEITO)
            .orElse(false);
    }
}
