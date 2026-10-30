package com.stocksmart.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateSupplierRequest(
        @NotBlank(message = "Name is required")
        @Size(max = 150)
        String name,

        @Size(max = 100)
        String contactName,

        @Email(message = "Email must be valid")
        @Size(max = 150)
        String email,

        @Size(max = 30)
        String phone,

        @Size(max = 500)
        String address
) {
}
