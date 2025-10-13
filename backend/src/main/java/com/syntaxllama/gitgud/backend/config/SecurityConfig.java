package com.syntaxllama.gitgud.backend.config;

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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Spring Security configuration for Keycloak OAuth2/OIDC integration.
 *
 * This configuration:
 * - Validates JWT tokens from Keycloak
 * - Configures public vs protected endpoints
 * - Enables CORS for React frontend
 * - Extracts roles from Keycloak JWT claims
 * - Uses stateless session management (no server-side sessions)
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    /**
     * Configure the security filter chain with JWT authentication and authorization rules.
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
                        // Public endpoints (no authentication required)
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/actuator/health/**").permitAll()
                        .requestMatchers("/actuator/prometheus").permitAll()
                        .requestMatchers("/error").permitAll()

                        // Public API endpoints for unauthenticated users
                        .requestMatchers(HttpMethod.GET, "/api/learning/modules").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/learning/modules/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/learning/lessons/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/gamification/achievements").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/gamification/levels").permitAll()

                        // All other API endpoints require authentication
                        .requestMatchers("/api/**").authenticated()

                        // Deny everything else
                        .anyRequest().denyAll()
                )

                // Configure OAuth2 Resource Server with JWT validation
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(new KeycloakJwtAuthenticationConverter()))
                );

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
