package com.syntaxllama.gitgud.backend.dtos.execution;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Internal DTO representing a code execution job.
 * This is the message sent to RabbitMQ queue for processing.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodeExecutionJob implements Serializable {

    /**
     * Unique job ID.
     */
    private String jobId;

    /**
     * User ID who submitted the code.
     */
    private UUID userId;

    /**
     * Lesson ID this code is for.
     */
    private UUID lessonId;

    /**
     * Programming language.
     */
    private String language;

    /**
     * Source code to execute.
     */
    private String sourceCode;

    /**
     * Test case IDs to run against.
     */
    private List<UUID> testCaseIds;

    /**
     * Timestamp when job was submitted.
     */
    private LocalDateTime submittedAt;
}
