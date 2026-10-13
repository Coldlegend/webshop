package com.webshop.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. CSRF kikapcsolása (REST API-knál, állapotmentes JWT / HTTP Basic esetén nem szükséges)
                .csrf(csrf -> csrf.disable())

                // 2. H2 Console támogatása böngészőből (ha H2-t használsz fejlesztéshez)
                .headers(headers -> headers.frameOptions(frame -> frame.disable()))

                // 3. Végpontok szabályozása (ki mit érhet el)
                .authorizeHttpRequests(auth -> auth
                        // Termékek böngészése publikus (bárki nézheti GET-tel)
                        .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()

                        // Későbbi regisztráció és bejelentkezés végpontok publikusak
                        .requestMatchers("/api/auth/**").permitAll()

                        // H2 adatbázis konzolja böngészőből (csak fejlesztéshez)
                        .requestMatchers("/h2-console/**").permitAll()

                        // Termékek létrehozása csak ADMIN jogosultsággal
                        .requestMatchers(HttpMethod.POST, "/api/products/**").hasRole("ADMIN")

                        // Rendelésekhez (GET és POST) kötelező bejelentkezni!
                        .requestMatchers("/api/orders/**").authenticated()

                        // Minden egyéb kéréshez kötelező a hitelesítés
                        .anyRequest().authenticated()
                )
                /*
                // 4. HTTP Basic autentikáció bekapcsolása (egyszerű teszteléshez névvel/jelszóval a fejlécben)
                .httpBasic(Customizer.withDefaults())
                */
                // 5. Munkamenet kezelés: REST API-knál állapotmentes (Stateless)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    // 6. Jelszóhashelő Bean: SOHA nem tárolunk nyers jelszót az adatbázisban!
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
