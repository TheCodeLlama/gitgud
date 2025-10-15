package com.syntaxllama.gitgud.backend.configs;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Spring Security configuration for Firebase Authentication.
 *
 * This configuration:
 * - Validates Firebase ID tokens
 * - Configures public vs protected endpoints
 * - Enables CORS for React frontend
 * - Extracts user information from Firebase tokens
 * - Uses stateless session management (no server-side sessions)
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final FirebaseAuthenticationFilter firebaseAuthenticationFilter;

    /**
     * Configure the security filter chain with Firebase authentication and authorization rules.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF for stateless API (using JWT tokens)
                .csrf(AbstractHttpConfigurer::disable)

                // Configure CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Stateless session management (JWT-based, no server sessions)
                .sessionManagement(session ->
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Configure authorization rules
                .authorizeHttpRequests(auth -> auth
                        // Public monitoring endpoints
                        .requestMatchers("/actuator/health/**").permitAll()
                        .requestMatchers("/actuator/prometheus").permitAll()
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/error").permitAll()

                        // Note: Firebase handles registration on the client side
                        // No backend /register endpoint needed

                        // Public API endpoints for unauthenticated users
                        .requestMatchers(HttpMethod.GET, "/api/v1/learning/modules").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/learning/modules/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/learning/lessons/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/gamification/achievements").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/gamification/levels").permitAll()

                        // All other API endpoints require authentication
                        .requestMatchers("/api/v1/**").authenticated()

                        // Deny everything else by default
                        .anyRequest().denyAll()
                )

                // Add Firebase authentication filter before Spring Security's authentication filter
                .addFilterBefore(firebaseAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * CORS configuration to allow requests from React frontend.
     * Configured to work with frontend running on http://localhost:5173
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:5173"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }

}
