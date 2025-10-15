package com.syntaxllama.gitgud.backend.dtos.learning;

import com.syntaxllama.gitgud.backend.dtos.execution.TestCaseResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO for code submission data.
 * Used to return submission history to the frontend.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionDTO {

    private UUID id;
    private UUID lessonId;
    private String code;
    private String status; // PASSED, FAILED, ERROR, PENDING
    private Integer passedTests;
    private Integer totalTests;
    private Long executionTimeMs;
    private String errorMessage;
    private String consoleOutput;
    private Integer xpAwarded;
    private List<TestCaseResult> testCaseResults;
    private LocalDateTime submittedAt;
}
