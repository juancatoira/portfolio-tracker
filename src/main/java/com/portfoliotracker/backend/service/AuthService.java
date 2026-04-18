package com.portfoliotracker.backend.service;

import com.portfoliotracker.backend.dto.request.LoginRequest;
import com.portfoliotracker.backend.dto.request.RegisterRequest;
import com.portfoliotracker.backend.dto.response.AuthResponse;
import com.portfoliotracker.backend.entity.User;
import com.portfoliotracker.backend.repository.UserRepository;
import com.portfoliotracker.backend.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("El email ya está registrado");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .currency(User.Currency.EUR)
                .build();

        userRepository.save(user);

        String token = jwtService.generateToken(user);

        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .currency(user.getCurrency().name())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        String token = jwtService.generateToken(user);

        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .currency(user.getCurrency().name())
                .build();
    }

    @jakarta.annotation.PostConstruct
    public void initDemoAccount() {
        if (!userRepository.existsByEmail("demo@portfoliotracker.com")) {
            User user = User.builder()
                    .email("demo@portfoliotracker.com")
                    .password(passwordEncoder.encode("demo1234"))
                    .currency(User.Currency.EUR)
                    .build();
            userRepository.save(user);
            log.info("Cuenta demo creada");
        }
    }
}