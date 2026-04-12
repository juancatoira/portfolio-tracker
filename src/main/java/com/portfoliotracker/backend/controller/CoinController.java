package com.portfoliotracker.backend.controller;

import com.portfoliotracker.backend.entity.Coin;
import com.portfoliotracker.backend.repository.CoinRepository;
import com.portfoliotracker.backend.service.CoinSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/coins")
@RequiredArgsConstructor
public class CoinController {

    private final CoinRepository coinRepository;
    private final CoinSyncService coinSyncService;

    @GetMapping("/search")
    public ResponseEntity<List<Coin>> search(@RequestParam String query) {
        if (query.length() < 2) return ResponseEntity.ok(List.of());
        return ResponseEntity.ok(
                coinRepository.findByNameContainingIgnoreCaseOrSymbolContainingIgnoreCase(query, query)
        );
    }

    @GetMapping("/top")
    public ResponseEntity<List<Coin>> getTop(@RequestParam(defaultValue = "50") int limit) {
        return ResponseEntity.ok(
                coinRepository.findByMarketCapRankIsNotNullOrderByMarketCapRankAsc(
                        org.springframework.data.domain.PageRequest.of(0, limit)
                ).getContent()
        );
    }

    @PostMapping("/sync")
    public ResponseEntity<String> syncManual() {
        coinSyncService.sync();
        return ResponseEntity.ok("Sincronización iniciada");
    }
}