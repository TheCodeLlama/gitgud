package com.syntaxllama.gitgud.backend.repositories;

import com.syntaxllama.gitgud.backend.models.User;
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
     * Find user by Firebase UID (primary lookup for authentication).
     */
    Optional<User> findByFirebaseUid(String firebaseUid);

    /**
     * Find user by email.
     */
    Optional<User> findByEmail(String email);

    /**
     * Find user by username.
     */
    Optional<User> findByUsername(String username);

    /**
     * Check if user exists by Firebase UID.
     */
    boolean existsByFirebaseUid(String firebaseUid);

    /**
     * Check if user exists by email.
     */
    boolean existsByEmail(String email);

    /**
     * Check if user exists by username.
     */
    boolean existsByUsername(String username);
}
