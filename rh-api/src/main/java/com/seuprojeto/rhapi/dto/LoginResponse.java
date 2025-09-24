package com.seuprojeto.rhapi.dto;

public record LoginResponse(
        String token,
        Long id,
        String nome,
        String email,
        String role
) {}
