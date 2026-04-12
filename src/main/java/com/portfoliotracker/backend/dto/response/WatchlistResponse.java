package com.portfoliotracker.backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class WatchlistResponse {
    private UUID id;
    private String coinId;
    private String coinName;
    private String coinSymbol;
    private BigDecimal currentPriceUsd;
    private BigDecimal priceChangePercent24h;
    private BigDecimal priceChangePercent7d;
}