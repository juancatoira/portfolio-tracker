package com.portfoliotracker.backend.controller;

import com.portfoliotracker.backend.dto.request.ManualPositionRequest;
import com.portfoliotracker.backend.dto.request.TransactionRequest;
import com.portfoliotracker.backend.dto.response.PositionResponse;
import com.portfoliotracker.backend.dto.response.TransactionResponse;
import com.portfoliotracker.backend.entity.Transaction;
import com.portfoliotracker.backend.entity.User;
import com.portfoliotracker.backend.repository.TransactionRepository;
import com.portfoliotracker.backend.service.PriceService;
import com.portfoliotracker.backend.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;
    private final TransactionRepository transactionRepository;
    private final PriceService priceService;
    @PostMapping("/transactions")
    public ResponseEntity<TransactionResponse> create(
            @Valid @RequestBody TransactionRequest request,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(transactionService.create(request, user));
    }

    @GetMapping("/transactions")
    public ResponseEntity<List<TransactionResponse>> getHistory(
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(transactionService.getHistory(user));
    }

    @DeleteMapping("/transactions/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id,
            @AuthenticationPrincipal User user
    ) {
        transactionService.delete(id, user);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/portfolio")
    public ResponseEntity<List<PositionResponse>> getPositions(
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(transactionService.getPositions(user));
    }

    @PostMapping("/portfolio/manual")
    public ResponseEntity<TransactionResponse> addManualPosition(
            @Valid @RequestBody ManualPositionRequest request,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(transactionService.addManualPosition(request, user));
    }

    @PostMapping("/portfolio/prices/refresh")
    public ResponseEntity<Map<String, Object>> refreshPrices(
            @AuthenticationPrincipal User user
    ) {
        List<Transaction> transactions = transactionRepository.findByUserIdOrderByDateDesc(user.getId());
        List<String> coinIds = transactions.stream()
                .map(Transaction::getCoinId)
                .distinct()
                .collect(Collectors.toList());

        priceService.updatePrices(coinIds);

        return ResponseEntity.ok(Map.of(
                "updatedAt", LocalDateTime.now().toString(),
                "coins", coinIds.size()
        ));
    }

    @GetMapping("/portfolio/prices/last-updated")
    public ResponseEntity<Map<String, Object>> getLastUpdated() {
        LocalDateTime lastUpdated = priceService.getLastUpdated();
        return ResponseEntity.ok(Map.of(
                "lastUpdated", lastUpdated != null ? lastUpdated.toString() : "never"
        ));
    }
}