package com.seuprojeto.rhapi.dto;

public record JornadaTrabalhoOutDTO(
        Long colaboradorId,
        int minutosSeg,
        int minutosTer,
        int minutosQua,
        int minutosQui,
        int minutosSex,
        int minutosSab,
        int minutosDom
) {}
