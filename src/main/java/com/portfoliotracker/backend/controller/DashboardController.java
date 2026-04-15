package com.portfoliotracker.backend.controller;

import com.portfoliotracker.backend.dto.response.PositionResponse;
import com.portfoliotracker.backend.dto.response.TransactionResponse;
import com.portfoliotracker.backend.entity.PortfolioSnapshot;
import com.portfoliotracker.backend.entity.User;
import com.portfoliotracker.backend.service.CoinGeckoService;
import com.portfoliotracker.backend.service.SnapshotService;
import com.portfoliotracker.backend.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;
import org.springframework.core.ParameterizedTypeReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final SnapshotService snapshotService;
    private final TransactionService transactionService;
    private final CoinGeckoService coinGeckoService;

    private final RestClient restClient = RestClient.create();

    @GetMapping("/transactions")
    public ResponseEntity<List<TransactionResponse>> getTransactions(
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(transactionService.getHistory(user));
    }

    @GetMapping("/snapshots")
    public ResponseEntity<List<Map<String, Object>>> getSnapshots(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "30") int days
    ) {
        List<PortfolioSnapshot> snapshots = snapshotService.getSnapshots(user, days);
        List<Map<String, Object>> result = new ArrayList<>();

        for (PortfolioSnapshot s : snapshots) {
            Map<String, Object> map = new HashMap<>();
            map.put("timestamp", s.getTimestamp().toString());
            map.put("totalValueUsd", s.getTotalValueUsd());
            result.add(map);
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("/news")
    public ResponseEntity<List<Map<String, Object>>> getNews(
            @AuthenticationPrincipal User user
    ) {
        List<String> coinSymbols = transactionService.getPositions(user).stream()
                .map(PositionResponse::getCoinSymbol)
                .collect(Collectors.toList());

        log.info("Buscando noticias para símbolos: {}", coinSymbols);

        if (coinSymbols.isEmpty()) return ResponseEntity.ok(List.of());

        try {
            List<Map<String, Object>> news = coinGeckoService.getNews(coinSymbols);
            log.info("Noticias obtenidas: {}", news.size());
            return ResponseEntity.ok(news);
        } catch (Exception e) {
            log.error("Error obteniendo noticias: {}", e.getMessage());
            return ResponseEntity.ok(List.of());
        }
    }
}