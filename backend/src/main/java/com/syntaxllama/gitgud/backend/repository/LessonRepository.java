package com.syntaxllama.gitgud.backend.repository;

import com.syntaxllama.gitgud.backend.model.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository interface for Lesson entity operations.
 */
@Repository
public interface LessonRepository extends JpaRepository<Lesson, UUID> {

    /**
     * Find all lessons for a specific module ordered by display order.
     */
    List<Lesson> findByModuleIdOrderByDisplayOrderAsc(UUID moduleId);

    /**
     * Find all published lessons for a specific module.
     */
    List<Lesson> findByModuleIdAndIsPublishedTrueOrderByDisplayOrderAsc(UUID moduleId);

    /**
     * Find lessons by type.
     */
    List<Lesson> findByLessonTypeOrderByDisplayOrderAsc(Lesson.LessonType lessonType);

    /**
     * Find lessons by difficulty.
     */
    List<Lesson> findByDifficultyOrderByDisplayOrderAsc(Lesson.Difficulty difficulty);
}
