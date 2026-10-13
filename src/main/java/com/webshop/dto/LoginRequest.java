package com.webshop.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "Az e-mail megadása kötelező")
        @Email(message = "Érvénytelen e-mail formátum")
        String email,

        @NotBlank(message = "A jelszó megadása kötelező")
        String password
) {}