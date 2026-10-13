package com.webshop.config;


import com.webshop.dto.Role;
import com.webshop.entity.User;
import com.webshop.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initUsers(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (!userRepository.existsByEmail("user@webshop.com")) {
                userRepository.save(new User(
                        "user@webshop.com",
                        passwordEncoder.encode("password123"), // BCrypt-tel titkosítva!
                        Role.ROLE_USER
                ));
            }

            if (!userRepository.existsByEmail("admin@webshop.com")) {
                userRepository.save(new User(
                        "admin@webshop.com",
                        passwordEncoder.encode("admin123"),
                        Role.ROLE_ADMIN
                ));
            }
        };
    }
}