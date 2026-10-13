package com.webshop.dto;

public record AuthResponse(
        String email,
        String role,
        String token, // ÚJ mező: ide kerül a JWT token
        String message
) {}