package com.syntaxllama.gitgud.backend.service.learning;

import com.syntaxllama.gitgud.backend.dto.learning.LessonDTO;
import com.syntaxllama.gitgud.backend.dto.learning.TestCaseDTO;
import com.syntaxllama.gitgud.backend.exception.ResourceNotFoundException;
import com.syntaxllama.gitgud.backend.model.Lesson;
import com.syntaxllama.gitgud.backend.model.TestCase;
import com.syntaxllama.gitgud.backend.repository.LessonRepository;
import com.syntaxllama.gitgud.backend.repository.TestCaseRepository;
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
    private final TestCaseRepository testCaseRepository;

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

    /**
     * Get visible (non-hidden) test cases for a lesson.
     * This is a public endpoint - only returns test cases marked as visible to users.
     * Expected outputs are hidden for security reasons.
     *
     * @param lessonId The lesson ID
     * @return List of visible test cases (without expected output)
     */
    @Transactional(readOnly = true)
    public List<TestCaseDTO> getVisibleTestCasesForLesson(UUID lessonId) {
        log.debug("Fetching visible test cases for lesson: {}", lessonId);

        // Verify lesson exists
        if (!lessonRepository.existsById(lessonId)) {
            throw new ResourceNotFoundException("Lesson not found with id: " + lessonId);
        }

        List<TestCase> testCases = testCaseRepository.findByLessonIdAndIsHiddenFalseOrderByDisplayOrderAsc(lessonId);

        return testCases.stream()
                .map(TestCaseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get all test cases for a lesson, including hidden ones.
     * This should only be used internally for code execution validation,
     * NOT exposed to public endpoints.
     *
     * @param lessonId The lesson ID
     * @return List of all test cases with expected output
     */
    @Transactional(readOnly = true)
    public List<TestCaseDTO.TestCaseWithOutputDTO> getAllTestCasesForLesson(UUID lessonId) {
        log.debug("Fetching all test cases (including hidden) for lesson: {}", lessonId);

        // Verify lesson exists
        if (!lessonRepository.existsById(lessonId)) {
            throw new ResourceNotFoundException("Lesson not found with id: " + lessonId);
        }

        List<TestCase> testCases = testCaseRepository.findByLessonIdOrderByDisplayOrderAsc(lessonId);

        return testCases.stream()
                .map(TestCaseDTO::fromEntityWithOutput)
                .collect(Collectors.toList());
    }
}
