package com.portfoliotracker.backend.repository;

import com.portfoliotracker.backend.entity.Coin;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CoinRepository extends JpaRepository<Coin, String> {
    List<Coin> findByNameContainingIgnoreCaseOrSymbolContainingIgnoreCase(
            String name, String symbol
    );

    Page<Coin> findByMarketCapRankIsNotNullOrderByMarketCapRankAsc(Pageable pageable);

}