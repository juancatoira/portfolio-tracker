package com.portfoliotracker.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "coin_prices")
public class CoinPrice {

    @Id
    private String coinId;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal priceUsd;

    @Column(nullable = false)
    private LocalDateTime updatedAt;
}