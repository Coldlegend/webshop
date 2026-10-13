package com.webshop.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@Service
public class JwtService {
    // Legalább 256 bites (32 karakter hosszú) titkos kulcs HMAC-SHA algoritmushoz
    // Élesben ezt az application.properties-ből érdemes olvasni!
    private static final String SECRET_STRING = "nagyonBiztonsagosEsHosszuTitkosKulcsWebshopAlkalmazashoz2026";
    private static final long EXPIRATION_TIME = 1000 * 60 * 60 * 24; // 24 óra milliszekundumban
    private final SecretKey key = Keys.hmacShaKeyFor(SECRET_STRING.getBytes(StandardCharsets.UTF_16));
    // Token generálása a bejelentkezett UserDetails alapján
    public String generateToken(UserDetails userDetails){
        List<String> roles = userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
        return Jwts.builder()
                .subject(userDetails.getUsername()) // a felhasználó e-mail címe
                .claim("roles", roles)              // szerepkörök hozzáadása a payloadhoz
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(key)
                .compact();
    }
    // E-mail cím kiolvasása a tokenből
    public String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }

    // Token érvényességének ellenőrzése
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token) {
        return extractClaims(token).getExpiration().before(new Date());
    }

    private Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
