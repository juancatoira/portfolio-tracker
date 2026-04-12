package com.portfoliotracker.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String coinId;

    @Column(nullable = false)
    private String coinName;

    @Column(nullable = false)
    private String coinSymbol;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Column(nullable = false, precision = 18, scale = 8)
    private BigDecimal quantity;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal priceUsd;

    @Column(nullable = false)
    private LocalDateTime date;

    @Column
    private String notes;


    public enum TransactionType {
        BUY, SELL, MANUAL
    }

}