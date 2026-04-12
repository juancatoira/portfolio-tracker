package com.portfoliotracker.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "watchlist", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "coin_id"})
})
public class Watchlist {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "coin_id", nullable = false)
    private String coinId;

    @Column(nullable = false)
    private String coinName;

    @Column(nullable = false)
    private String coinSymbol;
}