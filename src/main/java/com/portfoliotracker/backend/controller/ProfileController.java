package com.portfoliotracker.backend.controller;

import com.portfoliotracker.backend.dto.request.ChangePasswordRequest;
import com.portfoliotracker.backend.dto.request.UpdateProfileRequest;
import com.portfoliotracker.backend.dto.response.ProfileResponse;
import com.portfoliotracker.backend.entity.User;
import com.portfoliotracker.backend.service.ExchangeRateService;
import com.portfoliotracker.backend.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;
    private final ExchangeRateService exchangeRateService;

    @GetMapping
    public ResponseEntity<ProfileResponse> getProfile(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(profileService.getProfile(user));
    }

    @PutMapping
    public ResponseEntity<ProfileResponse> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(profileService.updateProfile(request, user));
    }

    @PutMapping("/password")
    public ResponseEntity<Void> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            @AuthenticationPrincipal User user
    ) {
        profileService.changePassword(request, user);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/currencies")
    public ResponseEntity<Map<String, String>> getCurrencies() {
        return ResponseEntity.ok(exchangeRateService.getSupportedCurrencies());
    }

    @GetMapping("/exchange-rates")
    public ResponseEntity<Map<String, Object>> getExchangeRates() {
        Map<String, Number> rates = exchangeRateService.getRates();
        return ResponseEntity.ok(Map.of("rates", rates));
    }
}