package com.webshop.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Az e-mail megadása kötelező")
        @Email(message = "Érvénytelen e-mail formátum")
        String email,

        @NotBlank(message = "A jelszó megadása kötelező")
        @Size(min = 6, message = "A jelszónak legalább 6 karakter hosszúnak kell lennie")
        String password
) {}