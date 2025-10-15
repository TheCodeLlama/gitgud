package com.syntaxllama.gitgud.backend.security;

import com.syntaxllama.gitgud.backend.configs.FirebaseAuthenticationFilter.FirebaseAuthenticationToken;
import com.syntaxllama.gitgud.backend.models.User;
import com.syntaxllama.gitgud.backend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Utility class for extracting authenticated user information from Firebase tokens.
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
     * Get the Firebase authentication token from the current authentication.
     */
    public static Optional<FirebaseAuthenticationToken> getCurrentFirebaseToken() {
        return getCurrentAuthentication()
                .filter(auth -> auth instanceof FirebaseAuthenticationToken)
                .map(auth -> (FirebaseAuthenticationToken) auth);
    }

    /**
     * Extract the Firebase UID (user ID) from the authentication token.
     * This is the user's unique identifier in Firebase.
     */
    public static Optional<String> getCurrentFirebaseUid() {
        return getCurrentFirebaseToken()
                .map(FirebaseAuthenticationToken::getUid);
    }

    /**
     * Extract the user's email from the Firebase token.
     */
    public static Optional<String> getCurrentUserEmail() {
        return getCurrentFirebaseToken()
                .map(FirebaseAuthenticationToken::getEmail);
    }

    /**
     * Extract the user's display name from the Firebase token.
     */
    public static Optional<String> getCurrentUsername() {
        return getCurrentFirebaseToken()
                .map(token -> {
                    // Try to get username from custom claims first
                    Object username = token.getClaims().get("username");
                    if (username != null) {
                        return username.toString();
                    }
                    // Fall back to name or email
                    String name = token.getName();
                    return name != null ? name : token.getEmail();
                });
    }

    /**
     * Extract the user's name from the Firebase token.
     */
    public static Optional<String> getCurrentName() {
        return getCurrentFirebaseToken()
                .map(FirebaseAuthenticationToken::getName);
    }

    /**
     * Get a custom claim from the Firebase token.
     */
    public static Optional<Object> getClaim(String claimName) {
        return getCurrentFirebaseToken()
                .map(token -> token.getClaims().get(claimName));
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
     * Looks up the user by their Firebase UID.
     */
    public User getCurrentUser() {
        String firebaseUid = getCurrentFirebaseUid()
                .orElseThrow(() -> new RuntimeException("No authenticated user found"));

        return userRepository.findByFirebaseUid(firebaseUid)
                .orElseThrow(() -> new RuntimeException("User not found in database: " + firebaseUid));
    }
}
