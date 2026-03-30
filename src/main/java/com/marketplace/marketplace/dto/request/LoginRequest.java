package com.marketplace.marketplace.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;

public record LoginRequest(
         @Email(message = "Invalid format to email") String email,
         @NotEmpty(message = "The password can not be null") String password) {

}
