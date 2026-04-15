package com.portfoliotracker.backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class WatchlistResponse {
    private UUID id;
    private String coinId;
    private String coinName;
    private String coinSymbol;
    private String imageUrl;
    private BigDecimal currentPriceUsd;
    private BigDecimal priceChangePercent24h;
    private BigDecimal priceChangePercent7d;
    private BigDecimal marketCap;
    private BigDecimal volume24h;
    private BigDecimal high24h;
    private BigDecimal low24h;
    private List<Double> sparkline7d;
}