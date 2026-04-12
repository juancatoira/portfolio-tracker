package com.portfoliotracker.backend.controller;

import com.portfoliotracker.backend.dto.request.WatchlistRequest;
import com.portfoliotracker.backend.dto.response.WatchlistResponse;
import com.portfoliotracker.backend.entity.User;
import com.portfoliotracker.backend.service.WatchlistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/watchlist")
@RequiredArgsConstructor
public class WatchlistController {

    private final WatchlistService watchlistService;

    @PostMapping
    public ResponseEntity<WatchlistResponse> add(
            @Valid @RequestBody WatchlistRequest request,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(watchlistService.add(request, user));
    }

    @GetMapping
    public ResponseEntity<List<WatchlistResponse>> getWatchlist(
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(watchlistService.getWatchlist(user));
    }

    @DeleteMapping("/{coinId}")
    public ResponseEntity<Void> remove(
            @PathVariable String coinId,
            @AuthenticationPrincipal User user
    ) {
        watchlistService.remove(coinId, user);
        return ResponseEntity.noContent().build();
    }
}
