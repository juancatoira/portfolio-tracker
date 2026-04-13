package com.portfoliotracker.backend.repository;

import com.portfoliotracker.backend.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    List<Transaction> findByUserIdOrderByDateDesc(UUID userId);
    List<Transaction> findByUserIdAndCoinIdOrderByDateAsc(UUID userId, String coinId);
    Optional<Transaction> findByUserIdAndCoinIdAndType(UUID userId, String coinId, Transaction.TransactionType type);
    boolean existsByCoinId(String coinId);
}