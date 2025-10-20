package com.syntaxllama.gitgud.backend.dtos.execution;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing a single Spring test result (from JUnit XML parsing).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpringTestResult {

    /**
     * Test class name (e.g., "com.example.UserControllerTest").
     */
    private String className;

    /**
     * Test method name (e.g., "testGetUserById").
     */
    private String methodName;

    /**
     * Full test name (className + methodName).
     */
    private String testName;

    /**
     * Whether the test passed.
     */
    private Boolean passed;

    /**
     * Execution time in milliseconds.
     */
    private Long executionTimeMs;

    /**
     * Error message if test failed.
     */
    private String errorMessage;

    /**
     * Stack trace if test failed.
     */
    private String stackTrace;

    /**
     * Error type (e.g., "AssertionError", "NullPointerException").
     */
    private String errorType;
}
