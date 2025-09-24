package com.seuprojeto.rhapi.dto;

import java.time.Instant;

public record DepartamentoDTO(
        Long id,
        String nome,
        Instant createdAt,
        Instant updatedAt
) {}