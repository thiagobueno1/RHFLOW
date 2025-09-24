package com.seuprojeto.rhapi.dto;

import com.seuprojeto.rhapi.domain.enums.StatusFerias;
import java.time.Instant;
import java.time.LocalDate;

public record SolicitacaoFeriasDTO(
        Long id,
        Long colaboradorId,
        LocalDate dataInicio,
        LocalDate dataFim,
        Integer dias,
        StatusFerias status,
        String motivo,
        Instant createdAt,
        Instant updatedAt
) {}
