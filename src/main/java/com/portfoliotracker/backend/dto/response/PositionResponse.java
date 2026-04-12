package com.portfoliotracker.backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PositionResponse {
    private String coinId;
    private String coinName;
    private String coinSymbol;
    private String imageUrl;
    private BigDecimal quantity;
    private BigDecimal averagePriceUsd;   // Precio medio ponderado
    private BigDecimal currentPriceUsd;   // Precio actual de CoinGecko
    private BigDecimal totalInvestedUsd;  // quantity × averagePrice
    private BigDecimal currentValueUsd;   // quantity × currentPrice
    private BigDecimal pnlUsd;            // currentValue - totalInvested
    private BigDecimal pnlPercent;        // pnl / totalInvested × 100
}