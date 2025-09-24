package com.seuprojeto.rhapi.dto;

import com.seuprojeto.rhapi.domain.enums.OrigemRegistro;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalTime;

public record RegistroPontoCreateDTO(
        @NotNull Long colaboradorId,
        @NotNull LocalDate data,
        @NotNull LocalTime horaEntrada,
        LocalTime inicioAlmoco,
        LocalTime fimAlmoco,
        @NotNull LocalTime horaSaida,
        @NotNull OrigemRegistro origem,
        @Size(max = 255) String observacao
) {}