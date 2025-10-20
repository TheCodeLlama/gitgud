package com.syntaxllama.gitgud.backend.dtos.execution;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Internal DTO representing a code execution job.
 * This is the message sent to RabbitMQ queue for processing.
 * Supports both single-file and multi-file submissions.
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
     * Source code to execute (for single-file lessons).
     */
    private String sourceCode;

    /**
     * Project files for multi-file lessons.
     * Map of file path to file content.
     */
    private Map<String, String> projectFiles;

    /**
     * Test case IDs to run against (for single-file lessons).
     */
    private List<UUID> testCaseIds;

    /**
     * Timestamp when job was submitted.
     */
    private LocalDateTime submittedAt;

    /**
     * Check if this is a single-file job.
     */
    public boolean isSingleFile() {
        return sourceCode != null && !sourceCode.isBlank();
    }

    /**
     * Check if this is a multi-file job.
     */
    public boolean isMultiFile() {
        return projectFiles != null && !projectFiles.isEmpty();
    }
}
