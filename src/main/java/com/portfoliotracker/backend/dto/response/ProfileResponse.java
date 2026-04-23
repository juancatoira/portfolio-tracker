package com.portfoliotracker.backend.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProfileResponse {
    private String email;
    private String username;
    private String currency;
}