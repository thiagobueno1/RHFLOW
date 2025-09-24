package com.seuprojeto.rhapi.dto;

import com.seuprojeto.rhapi.domain.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.br.CPF;

import java.time.LocalDate;

public record ColaboradorCreateDTO(
        @NotBlank @Size(max = 120) String nome,

        // CPF válido (com ou sem máscara). Ex.: 529.982.247-25
        @NotBlank
        @Size(max = 20)
        @CPF(message = "CPF inválido")
        String cpf,

        // E-mail válido + sem espaços
        @NotBlank
        @Email(message = "E-mail inválido")
        @Size(max = 120)
        @Pattern(regexp = "\\S+", message = "E-mail não pode conter espaços")
        String email,

        @NotBlank @Size(max = 80) String cargo,

        @NotNull LocalDate dataAdmissao,

        @NotNull Long departamentoId,

        // ADMIN pode criar ADMIN/RH/COLABORADOR; RH pode criar RH/COLABORADOR; se null => COLABORADOR
        Role papel
) {}
