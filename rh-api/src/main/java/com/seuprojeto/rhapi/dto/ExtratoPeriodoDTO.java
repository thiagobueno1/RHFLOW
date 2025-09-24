package com.seuprojeto.rhapi.dto;

import java.time.LocalDate;
import java.util.List;

public record ExtratoPeriodoDTO(
        Long colaboradorId,
        LocalDate de,
        LocalDate ate,
        List<ExtratoDiaDTO> dias,
        int saldoTotalMin,
        Integer bancoAcumuladoMin,
        Integer saldoFeriasDias
) {}
