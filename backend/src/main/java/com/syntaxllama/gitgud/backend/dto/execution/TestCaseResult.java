package com.syntaxllama.gitgud.backend.dto.execution;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO for individual test case execution result.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestCaseResult {

    /**
     * Test case ID.
     */
    private UUID testCaseId;

    /**
     * Whether this test case passed.
     */
    private Boolean passed;

    /**
     * Input provided to the test case.
     */
    private String input;

    /**
     * Expected output.
     */
    private String expectedOutput;

    /**
     * Actual output from execution.
     */
    private String actualOutput;

    /**
     * Error message if test case failed.
     */
    private String errorMessage;

    /**
     * Execution time for this test case in milliseconds.
     */
    private Long executionTimeMs;
}
