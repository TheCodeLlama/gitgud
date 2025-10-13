package com.syntaxllama.gitgud.backend.repository;

import com.syntaxllama.gitgud.backend.model.TestCase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository interface for TestCase entity operations.
 */
@Repository
public interface TestCaseRepository extends JpaRepository<TestCase, UUID> {

    /**
     * Find all test cases for a lesson, ordered by display order.
     */
    List<TestCase> findByLessonIdOrderByDisplayOrderAsc(UUID lessonId);

    /**
     * Find visible (non-hidden) test cases for a lesson.
     */
    List<TestCase> findByLessonIdAndIsHiddenFalseOrderByDisplayOrderAsc(UUID lessonId);

    /**
     * Find hidden test cases for a lesson.
     */
    List<TestCase> findByLessonIdAndIsHiddenTrueOrderByDisplayOrderAsc(UUID lessonId);

    /**
     * Count test cases for a lesson.
     */
    long countByLessonId(UUID lessonId);
}
