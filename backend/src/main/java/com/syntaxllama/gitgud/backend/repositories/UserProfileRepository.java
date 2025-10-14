package com.syntaxllama.gitgud.backend.repositories;

import com.syntaxllama.gitgud.backend.models.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for UserProfile entity operations.
 */
@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, UUID> {

    /**
     * Find user profile by user ID.
     */
    Optional<UserProfile> findByUserId(UUID userId);
}
