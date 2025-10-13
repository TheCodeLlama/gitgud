package com.syntaxllama.gitgud.backend.repository;

import com.syntaxllama.gitgud.backend.model.UserAchievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for UserAchievement entity operations.
 */
@Repository
public interface UserAchievementRepository extends JpaRepository<UserAchievement, UUID> {

    /**
     * Find all achievements earned by a user.
     */
    List<UserAchievement> findByUserId(UUID userId);

    /**
     * Find all achievements earned by a user, ordered by earned date.
     */
    List<UserAchievement> findByUserIdOrderByEarnedAtDesc(UUID userId);

    /**
     * Find specific user achievement.
     */
    Optional<UserAchievement> findByUserIdAndAchievementId(UUID userId, UUID achievementId);

    /**
     * Count achievements earned by user.
     */
    long countByUserId(UUID userId);

    /**
     * Check if user has earned specific achievement.
     */
    boolean existsByUserIdAndAchievementId(UUID userId, UUID achievementId);
}
