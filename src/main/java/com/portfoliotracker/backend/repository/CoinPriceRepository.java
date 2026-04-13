package com.portfoliotracker.backend.repository;

import com.portfoliotracker.backend.entity.CoinPrice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CoinPriceRepository extends JpaRepository<CoinPrice, String> {
    List<CoinPrice> findByUpdatedAtAfter(java.time.LocalDateTime since);
}