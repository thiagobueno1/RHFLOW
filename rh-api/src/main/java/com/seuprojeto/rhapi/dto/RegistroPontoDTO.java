package com.seuprojeto.rhapi.dto;

import com.seuprojeto.rhapi.domain.enums.OrigemRegistro;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

public record RegistroPontoDTO(
        Long id,
        Long colaboradorId,
        LocalDate data,
        LocalTime horaEntrada,
        LocalTime inicioAlmoco,
        LocalTime fimAlmoco,
        LocalTime horaSaida,
        OrigemRegistro origem,
        String observacao,
        Instant createdAt,
        Instant updatedAt
) {}