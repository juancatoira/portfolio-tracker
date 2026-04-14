package com.portfoliotracker.backend.repository;

import com.portfoliotracker.backend.entity.PortfolioSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface PortfolioSnapshotRepository extends JpaRepository<PortfolioSnapshot, UUID> {
    List<PortfolioSnapshot> findByUserIdOrderByTimestampAsc(UUID userId);
    List<PortfolioSnapshot> findByUserIdAndTimestampAfterOrderByTimestampAsc(UUID userId, LocalDateTime since);
}