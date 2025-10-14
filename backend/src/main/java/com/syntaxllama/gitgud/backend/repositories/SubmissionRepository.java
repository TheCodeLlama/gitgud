package com.syntaxllama.gitgud.backend.repositories;

import com.syntaxllama.gitgud.backend.models.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository interface for Submission entity operations.
 */
@Repository
public interface SubmissionRepository extends JpaRepository<Submission, UUID> {

    /**
     * Find all submissions by user.
     */
    List<Submission> findByUserId(UUID userId);

    /**
     * Find all submissions by user, ordered by submission date.
     */
    List<Submission> findByUserIdOrderBySubmittedAtDesc(UUID userId);

    /**
     * Find all submissions for a lesson.
     */
    List<Submission> findByLessonId(UUID lessonId);

    /**
     * Find all submissions by user and lesson.
     */
    List<Submission> findByUserIdAndLessonIdOrderBySubmittedAtDesc(UUID userId, UUID lessonId);

    /**
     * Find submissions by status.
     */
    List<Submission> findByStatus(Submission.Status status);

    /**
     * Count submissions by user.
     */
    long countByUserId(UUID userId);

    /**
     * Count submissions by user and status.
     */
    long countByUserIdAndStatus(UUID userId, Submission.Status status);
}
