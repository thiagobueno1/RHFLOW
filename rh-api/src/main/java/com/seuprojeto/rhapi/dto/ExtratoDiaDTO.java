package com.seuprojeto.rhapi.dto;

import java.time.LocalDate;

public record ExtratoDiaDTO(
        LocalDate data,
        int previstoMin,
        int trabalhadoMin,
        int saldoMin
) {}
