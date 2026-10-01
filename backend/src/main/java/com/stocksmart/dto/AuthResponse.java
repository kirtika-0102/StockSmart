package com.stocksmart.dto;

import java.util.List;

public record AuthResponse(
        String accessToken,
        String tokenType,
        long expiresInMs,
        String username,
        String fullName,
        List<String> roles
) {
}
