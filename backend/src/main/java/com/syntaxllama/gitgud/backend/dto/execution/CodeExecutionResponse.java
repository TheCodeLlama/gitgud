package com.syntaxllama.gitgud.backend.dto.execution;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for code execution submission response.
 * Returns a job ID that can be used to poll for results.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodeExecutionResponse {

    /**
     * Unique job ID for this execution request.
     */
    private String jobId;

    /**
     * Status of the job (QUEUED, RUNNING, COMPLETED, FAILED).
     */
    private ExecutionStatus status;

    /**
     * Message about the submission (e.g., "Code submitted successfully").
     */
    private String message;
}
