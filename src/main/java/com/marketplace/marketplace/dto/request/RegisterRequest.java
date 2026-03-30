package com.marketplace.marketplace.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotEmpty(message = "The name is required")
        String name,

        @Email(message = "Invalid email format")
        String email,


        @NotEmpty(message = "The password can not be null")
        @Size(min=6, max=24, message = "The password must be at least 6 chars and maximum 24 chars")
        String password
) {
}
