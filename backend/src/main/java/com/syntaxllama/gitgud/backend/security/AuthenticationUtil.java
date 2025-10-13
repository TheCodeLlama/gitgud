package com.syntaxllama.gitgud.backend.security;

import com.syntaxllama.gitgud.backend.model.User;
import com.syntaxllama.gitgud.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Utility class for extracting authenticated user information from JWT tokens.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthenticationUtil {

    private final UserRepository userRepository;

    /**
     * Get the current authentication from SecurityContext.
     */
    public static Optional<Authentication> getCurrentAuthentication() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication());
    }

    /**
     * Get the JWT token from the current authentication.
     */
    public static Optional<Jwt> getCurrentJwt() {
        return getCurrentAuthentication()
                .filter(auth -> auth instanceof JwtAuthenticationToken)
                .map(auth -> ((JwtAuthenticationToken) auth).getToken());
    }

    /**
     * Extract the Keycloak user ID (subject claim) from the JWT.
     * The "sub" claim in Keycloak JWTs contains the user's unique identifier.
     */
    public static Optional<String> getCurrentKeycloakUserId() {
        return getCurrentJwt()
                .map(Jwt::getSubject);
    }

    /**
     * Extract the user's email from the JWT.
     */
    public static Optional<String> getCurrentUserEmail() {
        return getCurrentJwt()
                .map(jwt -> jwt.getClaimAsString("email"));
    }

    /**
     * Extract the user's preferred username from the JWT.
     * This is typically the username set in Keycloak.
     */
    public static Optional<String> getCurrentUsername() {
        return getCurrentJwt()
                .map(jwt -> jwt.getClaimAsString("preferred_username"));
    }

    /**
     * Extract the user's given name (first name) from the JWT.
     */
    public static Optional<String> getCurrentGivenName() {
        return getCurrentJwt()
                .map(jwt -> jwt.getClaimAsString("given_name"));
    }

    /**
     * Extract the user's family name (last name) from the JWT.
     */
    public static Optional<String> getCurrentFamilyName() {
        return getCurrentJwt()
                .map(jwt -> jwt.getClaimAsString("family_name"));
    }

    /**
     * Check if the current user has a specific role.
     * Role names should be uppercase (e.g., "USER", "ADMIN").
     */
    public static boolean hasRole(String role) {
        return getCurrentAuthentication()
                .map(auth -> auth.getAuthorities().stream()
                        .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + role.toUpperCase())))
                .orElse(false);
    }

    /**
     * Check if the current user is authenticated.
     */
    public static boolean isAuthenticated() {
        return getCurrentAuthentication()
                .map(Authentication::isAuthenticated)
                .orElse(false);
    }

    /**
     * Get the current authenticated User entity.
     * Looks up the user by their Keycloak ID from the JWT.
     */
    public User getCurrentUser() {
        String keycloakId = getCurrentKeycloakUserId()
                .orElseThrow(() -> new RuntimeException("No authenticated user found"));

        return userRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new RuntimeException("User not found in database: " + keycloakId));
    }
}
