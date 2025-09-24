package com.seuprojeto.rhapi.dto;

import java.time.Instant;
import java.time.LocalDate;

public record ColaboradorDTO(
        Long id,
        String nome,
        String cpf,
        String email,
        Boolean ativo,
        String cargo,
        LocalDate dataAdmissao,
        DepartamentoResumoDTO departamento,
        Instant createdAt,
        Instant updatedAt
) {}