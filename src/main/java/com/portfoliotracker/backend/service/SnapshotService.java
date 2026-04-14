package com.portfoliotracker.backend.service;

import com.portfoliotracker.backend.dto.response.PositionResponse;
import com.portfoliotracker.backend.entity.PortfolioSnapshot;
import com.portfoliotracker.backend.entity.User;
import com.portfoliotracker.backend.repository.PortfolioSnapshotRepository;
import com.portfoliotracker.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SnapshotService {

    private final PortfolioSnapshotRepository snapshotRepository;
    private final UserRepository userRepository;
    private final TransactionService transactionService;

    public void takeSnapshotsForAllUsers() {
        List<User> users = userRepository.findAll();
        for (User user : users) {
            try {
                takeSnapshot(user);
            } catch (Exception e) {
                log.error("Error tomando snapshot para usuario {}: {}", user.getId(), e.getMessage());
            }
        }
        log.info("Snapshots tomados para {} usuarios", users.size());
    }

    public void takeSnapshot(User user) {
        BigDecimal totalValue = transactionService.getPositions(user).stream()
                .map(PositionResponse::getCurrentValueUsd)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        PortfolioSnapshot snapshot = PortfolioSnapshot.builder()
                .user(user)
                .totalValueUsd(totalValue)
                .timestamp(LocalDateTime.now())
                .build();

        snapshotRepository.save(snapshot);
    }

    public List<PortfolioSnapshot> getSnapshots(User user, int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return snapshotRepository
                .findByUserIdAndTimestampAfterOrderByTimestampAsc(user.getId(), since);
    }
}