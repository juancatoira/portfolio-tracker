package com.portfoliotracker.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class WatchlistRequest {

    @NotBlank(message = "El id de la moneda es obligatorio")
    private String coinId;

    @NotBlank(message = "El nombre de la moneda es obligatorio")
    private String coinName;

    @NotBlank(message = "El símbolo de la moneda es obligatorio")
    private String coinSymbol;
}