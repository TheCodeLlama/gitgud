package com.syntaxllama.gitgud.backend.repository;

import com.syntaxllama.gitgud.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for User entity operations.
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Find user by Keycloak ID (primary lookup for authentication).
     */
    Optional<User> findByKeycloakId(String keycloakId);

    /**
     * Find user by email.
     */
    Optional<User> findByEmail(String email);

    /**
     * Find user by username.
     */
    Optional<User> findByUsername(String username);

    /**
     * Check if user exists by Keycloak ID.
     */
    boolean existsByKeycloakId(String keycloakId);

    /**
     * Check if user exists by email.
     */
    boolean existsByEmail(String email);

    /**
     * Check if user exists by username.
     */
    boolean existsByUsername(String username);
}
