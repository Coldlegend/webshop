package com.webshop.service;

import com.webshop.dto.AuthResponse;
import com.webshop.dto.LoginRequest;
import com.webshop.dto.RegisterRequest;
import com.webshop.dto.Role;
import com.webshop.entity.User;
import com.webshop.exceptions.EmailAlreadyExistsException;
import com.webshop.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, AuthenticationManager authenticationManager,
                       PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // 1. Ellenőrizzük, foglalt-e az e-mail
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException(request.email());
        }
// 2. Jelszó hashelése és mentés alapértelmezett ROLE_USER jogosultsággal
        User user = new User(request.email(), passwordEncoder.encode(request.password()), Role.ROLE_USER);
        userRepository.save(user);


        String token = jwtService.generateToken(user);
        return new AuthResponse(user.getEmail(), user.getRole().name(),token, "Sikeres regisztáció");
    }

    public AuthResponse login(LoginRequest request) {
        // A Spring belső mechanizmusa ellenőrzi a jelszó helyességét
        Authentication authentication =
                authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.email(),
                        request.password()));
        String role = authentication.getAuthorities().iterator().next().getAuthority();
        User user = (User) authentication.getPrincipal();
        String token = jwtService.generateToken(user);
        return new AuthResponse(request.email(), role, token,"Sikeres bejelentkezés!");
    }
}
