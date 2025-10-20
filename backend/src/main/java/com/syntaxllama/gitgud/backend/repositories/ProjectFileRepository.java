package com.syntaxllama.gitgud.backend.repositories;

import com.syntaxllama.gitgud.backend.models.ProjectFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for ProjectFile entity.
 */
@Repository
public interface ProjectFileRepository extends JpaRepository<ProjectFile, UUID> {

    /**
     * Find all project files for a lesson, ordered by display order.
     */
    List<ProjectFile> findByLessonIdOrderByDisplayOrderAsc(UUID lessonId);

    /**
     * Find all visible project files for a lesson (excludes hidden files like tests).
     */
    @Query("SELECT pf FROM ProjectFile pf WHERE pf.lesson.id = :lessonId AND pf.isVisible = true ORDER BY pf.displayOrder ASC")
    List<ProjectFile> findVisibleFilesByLessonId(@Param("lessonId") UUID lessonId);

    /**
     * Find a specific file by lesson ID and path.
     */
    Optional<ProjectFile> findByLessonIdAndPath(UUID lessonId, String path);

    /**
     * Find all editable files for a lesson.
     */
    @Query("SELECT pf FROM ProjectFile pf WHERE pf.lesson.id = :lessonId AND pf.isEditable = true ORDER BY pf.displayOrder ASC")
    List<ProjectFile> findEditableFilesByLessonId(@Param("lessonId") UUID lessonId);

    /**
     * Count files in a lesson.
     */
    long countByLessonId(UUID lessonId);

    /**
     * Delete all files for a lesson (used in cascade operations).
     */
    void deleteByLessonId(UUID lessonId);
}
