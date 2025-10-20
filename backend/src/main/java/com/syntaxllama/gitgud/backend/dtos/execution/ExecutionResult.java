package com.syntaxllama.gitgud.backend.dtos.execution;

import com.syntaxllama.gitgud.backend.dtos.gamification.UserAchievementDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for complete code execution results.
 * Returned when polling for job status after execution completes.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecutionResult {

    /**
     * Job ID.
     */
    private String jobId;

    /**
     * Current status of the execution.
     */
    private ExecutionStatus status;

    /**
     * Whether all test cases passed.
     */
    private Boolean passed;

    /**
     * Number of test cases that passed.
     */
    private Integer testsPassed;

    /**
     * Total number of test cases.
     */
    private Integer totalTests;

    /**
     * Individual test case results (for single-file lessons).
     */
    private List<TestCaseResult> testCaseResults;

    /**
     * Spring test results (for multi-file lessons).
     */
    private List<SpringTestResult> springTestResults;

    /**
     * Compilation output (if compilation failed).
     */
    private String compilationOutput;

    /**
     * Console output from the execution (stdout).
     */
    private String consoleOutput;

    /**
     * Error output from the execution (stderr).
     */
    private String errorOutput;

    /**
     * Execution time in milliseconds.
     */
    private Long executionTimeMs;

    /**
     * XP awarded for this submission (if passed).
     */
    private Integer xpAwarded;

    /**
     * Achievements earned during this submission.
     */
    private List<UserAchievementDTO> achievementsEarned;

    /**
     * Timestamp when execution started.
     */
    private LocalDateTime startedAt;

    /**
     * Timestamp when execution completed.
     */
    private LocalDateTime completedAt;

    /**
     * Error message if execution failed.
     */
    private String errorMessage;
}
