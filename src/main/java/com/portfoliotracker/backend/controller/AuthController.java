package com.portfoliotracker.backend.controller;

import com.portfoliotracker.backend.dto.request.LoginRequest;
import com.portfoliotracker.backend.dto.request.RegisterRequest;
import com.portfoliotracker.backend.dto.response.AuthResponse;
import com.portfoliotracker.backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/demo")
    public ResponseEntity<AuthResponse> demo() {
        return ResponseEntity.ok(authService.login(
                new LoginRequest() {{
                    setEmail("demo@portfoliotracker.com");
                    setPassword("demo1234");
                }}
        ));
    }
}