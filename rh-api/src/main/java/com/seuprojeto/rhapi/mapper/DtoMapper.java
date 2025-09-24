package com.seuprojeto.rhapi.mapper;

import com.seuprojeto.rhapi.domain.*;
import com.seuprojeto.rhapi.dto.*;
import org.springframework.stereotype.Component;

@Component
public final class DtoMapper {

    public DtoMapper() {}

    public static DepartamentoResumoDTO toResumo(Departamento d) {
        return new DepartamentoResumoDTO(d.getId(), d.getNome());
    }

    public static DepartamentoDTO toDTO(Departamento d) {
        return new DepartamentoDTO(
                d.getId(),
                d.getNome(),
                d.getCreatedAt(),
                d.getUpdatedAt()
        );
    }

    public static ColaboradorDTO toDTO(Colaborador c) {
        return new ColaboradorDTO(
                c.getId(),
                c.getNome(),
                c.getCpf(),
                c.getEmail(),
                c.getAtivo(),
                c.getCargo(),
                c.getDataAdmissao(),
                toResumo(c.getDepartamento()),
                c.getCreatedAt(),
                c.getUpdatedAt()
        );
    }

    public static RegistroPontoDTO toDTO(RegistroPonto r) {
        return new RegistroPontoDTO(
                r.getId(),
                r.getColaborador().getId(),
                r.getData(),
                r.getHoraEntrada(),
                r.getInicioAlmoco(),
                r.getFimAlmoco(),
                r.getHoraSaida(),
                r.getOrigem(),
                r.getObservacao(),
                r.getCreatedAt(),
                r.getUpdatedAt()
        );
    }

    public static SolicitacaoFeriasDTO toDTO(SolicitacaoFerias s) {
        return new SolicitacaoFeriasDTO(
                s.getId(),
                s.getColaborador().getId(),
                s.getDataInicio(),
                s.getDataFim(),
                s.getDias(),
                s.getStatus(),
                s.getMotivo(),
                s.getCreatedAt(),
                s.getUpdatedAt()
        );
    }

    public static JornadaTrabalhoOutDTO toDTO(JornadaTrabalho j) {
        return new JornadaTrabalhoOutDTO(
                j.getColaborador().getId(),
                j.getMinutosSeg(),
                j.getMinutosTer(),
                j.getMinutosQua(),
                j.getMinutosQui(),
                j.getMinutosSex(),
                j.getMinutosSab(),
                j.getMinutosDom()
        );
    }

    public static BancoDeHorasDTO toDTO(BancoDeHoras b) {
        return new BancoDeHorasDTO(
                b.getId(),
                b.getColaborador().getId(),
                b.getCompetencia(),
                b.getSaldoMinutos(),
                b.getCreatedAt(),
                b.getUpdatedAt()
        );
    }

    // ---------- MÉTODOS DE INSTÂNCIA (usados pelo ColaboradorController) ----------
    public ColaboradorDTO toColaboradorDTO(Colaborador c) {
        if (c == null) return null;
        return new ColaboradorDTO(
                c.getId(),
                c.getNome(),
                c.getCpf(),
                c.getEmail(),
                c.getAtivo(),
                c.getCargo(),
                c.getDataAdmissao(),
                toDepartamentoResumoDTO(c.getDepartamento()),
                c.getCreatedAt(),
                c.getUpdatedAt()
        );
    }

    public DepartamentoResumoDTO toDepartamentoResumoDTO(Departamento d) {
        if (d == null) return null;
        return new DepartamentoResumoDTO(d.getId(), d.getNome());
    }
}
