package com.syntaxllama.gitgud.backend.repositories;

import com.syntaxllama.gitgud.backend.models.UserStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for UserStats entity operations.
 */
@Repository
public interface UserStatsRepository extends JpaRepository<UserStats, UUID> {

    /**
     * Find user stats by user ID.
     */
    Optional<UserStats> findByUserId(UUID userId);
}
