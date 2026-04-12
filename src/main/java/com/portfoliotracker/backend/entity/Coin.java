package com.portfoliotracker.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "coins", indexes = {
        @Index(name = "idx_coin_symbol", columnList = "symbol"),
        @Index(name = "idx_coin_name", columnList = "name")
})
public class Coin {

    @Id
    private String id;

    @Column(nullable = false)
    private String symbol;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String imageUrl;

    @Column
    private Integer marketCapRank;
}