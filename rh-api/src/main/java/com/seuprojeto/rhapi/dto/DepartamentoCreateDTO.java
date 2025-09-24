package com.seuprojeto.rhapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DepartamentoCreateDTO(
        @NotBlank @Size(max = 120) String nome
) {}