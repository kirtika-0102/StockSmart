package com.stocksmart.dto;

import com.stocksmart.entity.RoleName;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
        @NotBlank(message = "Email is required")
        @Email
        String email,

        @NotBlank
        @Size(max = 100)
        String firstName,

        @NotBlank
        @Size(max = 100)
        String lastName,

        @NotNull(message = "Role is required")
        RoleName role,

        @NotNull
        Boolean active
) {
}
