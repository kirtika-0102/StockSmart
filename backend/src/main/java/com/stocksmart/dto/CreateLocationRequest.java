package com.stocksmart.dto;

import com.stocksmart.entity.LocationType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateLocationRequest(
        @NotBlank(message = "Name is required")
        @Size(max = 100)
        String name,

        @NotNull(message = "Type is required (STORE or WAREHOUSE)")
        LocationType type,

        @Size(max = 500)
        String address
) {
}
