package com.portfoliotracker.backend.service;

import com.portfoliotracker.backend.dto.request.ChangePasswordRequest;
import com.portfoliotracker.backend.dto.request.UpdateProfileRequest;
import com.portfoliotracker.backend.dto.response.ProfileResponse;
import com.portfoliotracker.backend.entity.User;
import com.portfoliotracker.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public ProfileResponse getProfile(User user) {
        return ProfileResponse.builder()
                .email(user.getEmail())
                .username(user.getUsername())
                .currency(user.getCurrency().name())
                .build();
    }

    public ProfileResponse updateProfile(UpdateProfileRequest request, User user) {
        user.setUsername(request.getUsername());
        if (request.getCurrency() != null) {
            user.setCurrency(User.Currency.valueOf(request.getCurrency()));
        }
        userRepository.save(user);
        return getProfile(user);
    }

    public void changePassword(ChangePasswordRequest request, User user) {
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("La contraseña actual es incorrecta");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
}