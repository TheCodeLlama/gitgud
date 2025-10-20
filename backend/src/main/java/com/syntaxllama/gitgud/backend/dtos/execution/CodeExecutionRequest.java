package com.syntaxllama.gitgud.backend.dtos.execution;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * DTO for code execution submission request.
 * Supports both single-file (legacy) and multi-file (Spring Boot) submissions.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodeExecutionRequest {

    /**
     * Programming language (currently only "java" supported).
     */
    @NotBlank(message = "Language is required")
    private String language;

    /**
     * Source code to be executed (for single-file lessons only).
     * Either sourceCode OR projectFiles must be provided.
     */
    @Size(max = 50000, message = "Source code must not exceed 50,000 characters")
    private String sourceCode;

    /**
     * Project files for multi-file lessons.
     * Map of file path to file content.
     * Example: {"src/main/java/com/example/UserController.java": "package com.example;..."}
     * Either sourceCode OR projectFiles must be provided.
     */
    private Map<String, String> projectFiles;

    /**
     * ID of the lesson this code is for.
     */
    @NotNull(message = "Lesson ID is required")
    private UUID lessonId;

    /**
     * IDs of test cases to run against the code.
     * For single-file lessons: runs specified test cases.
     * For multi-file lessons: typically empty (tests are in project files).
     * If empty, all test cases for the lesson will be used (single-file only).
     */
    private List<UUID> testCaseIds;

    /**
     * Check if this is a single-file submission.
     */
    public boolean isSingleFile() {
        return sourceCode != null && !sourceCode.isBlank();
    }

    /**
     * Check if this is a multi-file submission.
     */
    public boolean isMultiFile() {
        return projectFiles != null && !projectFiles.isEmpty();
    }
}
