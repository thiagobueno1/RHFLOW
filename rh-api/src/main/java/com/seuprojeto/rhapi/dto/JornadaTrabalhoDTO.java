package com.seuprojeto.rhapi.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record JornadaTrabalhoDTO(
        @NotNull Long colaboradorId,
        @Min(0) @Max(1440) int minutosSeg,
        @Min(0) @Max(1440) int minutosTer,
        @Min(0) @Max(1440) int minutosQua,
        @Min(0) @Max(1440) int minutosQui,
        @Min(0) @Max(1440) int minutosSex,
        @Min(0) @Max(1440) int minutosSab,
        @Min(0) @Max(1440) int minutosDom
) {}
