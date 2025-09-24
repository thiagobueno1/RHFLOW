package com.seuprojeto.rhapi.dto;

import java.time.Instant;

public record BancoDeHorasDTO(
        Long id,
        Long colaboradorId,
        String competencia, // YYYY-MM
        int saldoMinutos,
        Instant createdAt,
        Instant updatedAt
) {}