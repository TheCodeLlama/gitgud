package com.syntaxllama.gitgud.backend.repository;

import com.syntaxllama.gitgud.backend.model.UserProgress;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
