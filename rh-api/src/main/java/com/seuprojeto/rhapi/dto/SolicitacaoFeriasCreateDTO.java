package com.seuprojeto.rhapi.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record SolicitacaoFeriasCreateDTO(
        @NotNull Long colaboradorId,
        @NotNull LocalDate dataInicio,
        @NotNull LocalDate dataFim,
        @Size(max = 255) String motivo
) {}
