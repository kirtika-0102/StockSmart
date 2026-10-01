package com.stocksmart.dto;

import java.util.List;

public record UserResponse(
        Long id,
        String username,
        String email,
        String firstName,
        String lastName,
        boolean active,
        List<String> roles
) {
}
