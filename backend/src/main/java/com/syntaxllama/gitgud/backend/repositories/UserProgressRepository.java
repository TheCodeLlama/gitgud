package com.syntaxllama.gitgud.backend.repositories;

import com.syntaxllama.gitgud.backend.models.UserProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for UserProgress entity operations.
 */
@Repository
public interface UserProgressRepository extends JpaRepository<UserProgress, UUID> {

    /**
     * Find progress for a specific user and lesson.
     */
    Optional<UserProgress> findByUserIdAndLessonId(UUID userId, UUID lessonId);

    /**
     * Find all progress records for a user.
     */
    List<UserProgress> findByUserId(UUID userId);

    /**
     * Find all progress records for a user with specific status.
     */
    List<UserProgress> findByUserIdAndStatus(UUID userId, UserProgress.Status status);

    /**
     * Find all progress records for a lesson.
     */
    List<UserProgress> findByLessonId(UUID lessonId);

    /**
     * Count completed lessons for a user.
     */
    long countByUserIdAndStatus(UUID userId, UserProgress.Status status);

    /**
     * Check if progress exists for user and lesson.
     */
    boolean existsByUserIdAndLessonId(UUID userId, UUID lessonId);

    /**
     * Count completed lessons for a user in a specific module.
     */
    @Query("SELECT COUNT(up) FROM UserProgress up " +
           "WHERE up.user.id = :userId " +
           "AND up.lesson.module.id = :moduleId " +
           "AND up.status = 'COMPLETED'")
    long countCompletedLessonsByUserAndModule(@Param("userId") UUID userId, @Param("moduleId") UUID moduleId);

    /**
     * Check if user has any lessons with specific status and attempts count.
     */
    boolean existsByUserIdAndStatusAndAttemptsCount(UUID userId, UserProgress.Status status, Integer attemptsCount);
}
