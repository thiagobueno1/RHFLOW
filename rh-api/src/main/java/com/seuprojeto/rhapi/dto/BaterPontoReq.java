package com.seuprojeto.rhapi.dto;

import java.time.LocalDate;

public record BaterPontoReq(
        LocalDate data,   // se null, usa "hoje" no fuso America/Sao_Paulo
        Double lat,
        Double lng
) {}
