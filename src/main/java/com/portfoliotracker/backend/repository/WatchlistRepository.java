package com.portfoliotracker.backend.repository;

import com.portfoliotracker.backend.entity.Watchlist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WatchlistRepository extends JpaRepository<Watchlist, UUID> {
    List<Watchlist> findByUserId(UUID userId);
    Optional<Watchlist> findByUserIdAndCoinId(UUID userId, String coinId);
    boolean existsByUserIdAndCoinId(UUID userId, String coinId);
}