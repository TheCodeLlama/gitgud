package com.syntaxllama.gitgud.backend.controllers.learning;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.syntaxllama.gitgud.backend.dtos.ApiResponse;
import com.syntaxllama.gitgud.backend.dtos.execution.TestCaseResult;
import com.syntaxllama.gitgud.backend.dtos.learning.SubmissionDTO;
import com.syntaxllama.gitgud.backend.models.Submission;
import com.syntaxllama.gitgud.backend.models.User;
import com.syntaxllama.gitgud.backend.repositories.SubmissionRepository;
import com.syntaxllama.gitgud.backend.security.AuthenticationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Controller for managing code submissions.
 * Provides endpoints to retrieve submission history.
 */
@RestController
@RequestMapping("/api/v1/learning/submissions")
@RequiredArgsConstructor
@Slf4j
public class SubmissionController {

    private final SubmissionRepository submissionRepository;
    private final AuthenticationUtil authenticationUtil;
    private final ObjectMapper objectMapper;

    /**
     * Get the latest submission for the current user and a specific lesson.
     * Returns null if no submission exists.
     *
     * @param lessonId The lesson UUID
     * @return The latest submission or null
     */
    @GetMapping("/latest/{lessonId}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<SubmissionDTO> getLatestSubmission(@PathVariable UUID lessonId) {
        log.debug("Getting latest submission for lesson {}", lessonId);

        User currentUser = authenticationUtil.getCurrentUser();

        List<Submission> submissions = submissionRepository
                .findByUserIdAndLessonIdOrderBySubmittedAtDesc(currentUser.getId(), lessonId);

        if (submissions.isEmpty()) {
            return ApiResponse.success(null); // No submissions yet
        }

        Submission latest = submissions.get(0);
        SubmissionDTO dto = mapToDTO(latest);

        return ApiResponse.success(dto);
    }

    /**
     * Get all submissions for the current user and a specific lesson.
     *
     * @param lessonId The lesson UUID
     * @return List of submissions ordered by submission date (newest first)
     */
    @GetMapping("/history/{lessonId}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<List<SubmissionDTO>> getSubmissionHistory(@PathVariable UUID lessonId) {
        log.debug("Getting submission history for lesson {}", lessonId);

        User currentUser = authenticationUtil.getCurrentUser();

        List<Submission> submissions = submissionRepository
                .findByUserIdAndLessonIdOrderBySubmittedAtDesc(currentUser.getId(), lessonId);

        List<SubmissionDTO> dtos = submissions.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());

        return ApiResponse.success(dtos);
    }

    /**
     * Map Submission entity to DTO.
     */
    private SubmissionDTO mapToDTO(Submission submission) {
        // Deserialize test case results from JSON
        List<TestCaseResult> testCaseResults = null;
        if (submission.getTestCaseResultsJson() != null) {
            try {
                testCaseResults = objectMapper.readValue(
                        submission.getTestCaseResultsJson(),
                        new TypeReference<List<TestCaseResult>>() {}
                );
            } catch (Exception e) {
                log.warn("Failed to deserialize test case results for submission {}: {}",
                        submission.getId(), e.getMessage());
            }
        }

        return SubmissionDTO.builder()
                .id(submission.getId())
                .lessonId(submission.getLesson().getId())
                .code(submission.getCode())
                .status(submission.getStatus().name())
                .passedTests(submission.getPassedTests())
                .totalTests(submission.getTotalTests())
                .executionTimeMs(submission.getExecutionTimeMs())
                .errorMessage(submission.getErrorMessage())
                .consoleOutput(submission.getConsoleOutput())
                .xpAwarded(submission.getXpAwarded())
                .testCaseResults(testCaseResults)
                .submittedAt(submission.getSubmittedAt())
                .build();
    }
}
