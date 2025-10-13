package com.syntaxllama.gitgud.backend.dto.execution;

/**
 * Enum representing the status of a code execution job.
 */
public enum ExecutionStatus {
    /**
     * Job has been submitted and is waiting in the queue.
     */
    QUEUED,

    /**
     * Job is currently being executed.
     */
    RUNNING,

    /**
     * Job execution completed successfully (all test cases ran).
     */
    COMPLETED,

    /**
     * Job execution failed (compilation error, timeout, system error).
     */
    FAILED,

    /**
     * Job timed out during execution.
     */
    TIMEOUT
}
