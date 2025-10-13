package com.syntaxllama.gitgud.backend.service.learning;

import com.syntaxllama.gitgud.backend.dto.learning.LessonDTO;
import com.syntaxllama.gitgud.backend.exception.ResourceNotFoundException;
import com.syntaxllama.gitgud.backend.model.Lesson;
import com.syntaxllama.gitgud.backend.repository.LessonRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing lessons.
 * Handles lesson retrieval and content rendering support.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LessonService {

    private final LessonRepository lessonRepository;

    /**
     * Get a single lesson by ID.
     *
     * @param lessonId The lesson ID
     * @return Lesson DTO with full details
     * @throws ResourceNotFoundException if lesson not found
     */
    @Transactional(readOnly = true)
    public LessonDTO getLessonById(UUID lessonId) {
        log.debug("Fetching lesson with ID: {}", lessonId);
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found with id: " + lessonId));

        return LessonDTO.fromEntity(lesson);
    }

    /**
     * Get all published lessons for a specific module.
     *
     * @param moduleId The module ID
     * @return List of lessons ordered by display order
     */
    @Transactional(readOnly = true)
    public List<LessonDTO> getLessonsByModule(UUID moduleId) {
        log.debug("Fetching lessons for module: {}", moduleId);
        List<Lesson> lessons = lessonRepository.findByModuleIdAndIsPublishedTrueOrderByDisplayOrderAsc(moduleId);

        return lessons.stream()
                .map(LessonDTO::fromEntity)
                .collect(Collectors.toList());
    }
}
