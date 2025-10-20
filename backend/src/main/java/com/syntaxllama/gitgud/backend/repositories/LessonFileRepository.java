package com.syntaxllama.gitgud.backend.repositories;

import com.syntaxllama.gitgud.backend.models.LessonFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for LessonFile entities.
 */
@Repository
public interface LessonFileRepository extends JpaRepository<LessonFile, UUID> {

    /**
     * Find all files for a lesson, ordered by display order.
     */
    List<LessonFile> findByLessonIdOrderByDisplayOrder(UUID lessonId);

    /**
     * Find a specific file in a lesson by its path.
     */
    Optional<LessonFile> findByLessonIdAndPath(UUID lessonId, String path);

    /**
     * Delete all files for a lesson.
     */
    void deleteByLessonId(UUID lessonId);
}
