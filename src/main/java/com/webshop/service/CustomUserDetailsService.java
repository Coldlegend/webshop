package com.webshop.service;

import com.webshop.entity.User;
import com.webshop.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Megkeressük a saját User entitásunkat az adatbázisban az e-mail alapján
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Felhasználó nem található: " + email));
    }
}