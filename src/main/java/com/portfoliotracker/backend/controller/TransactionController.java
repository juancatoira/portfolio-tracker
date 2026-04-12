package com.portfoliotracker.backend.controller;

import com.portfoliotracker.backend.dto.request.ManualPositionRequest;
import com.portfoliotracker.backend.dto.request.TransactionRequest;
import com.portfoliotracker.backend.dto.response.PositionResponse;
import com.portfoliotracker.backend.dto.response.TransactionResponse;
import com.portfoliotracker.backend.entity.User;
import com.portfoliotracker.backend.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

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
}