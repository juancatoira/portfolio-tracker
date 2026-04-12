package com.portfoliotracker.backend.dto.response;

import com.portfoliotracker.backend.entity.Transaction;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class TransactionResponse {
    private UUID id;
    private String coinId;
    private String coinName;
    private String coinSymbol;
    private Transaction.TransactionType type;
    private BigDecimal quantity;
    private BigDecimal priceUsd;
    private LocalDateTime date;
    private String notes;
}