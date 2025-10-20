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
 * Supports both single-file (sourceCode) and multi-file (files) submissions.
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
     * Source code to be executed (for single-file lessons).
     * @deprecated Use files map for multi-file support. Kept for backwards compatibility.
     */
    @Size(max = 50000, message = "Source code must not exceed 50,000 characters")
    private String sourceCode;

    /**
     * Map of file paths to file contents (for multi-file lessons).
     * Example: {"src/main/java/HelloController.java": "public class HelloController {...}", "pom.xml": "..."}
     */
    private Map<String, String> files;

    /**
     * ID of the lesson this code is for.
     */
    @NotNull(message = "Lesson ID is required")
    private UUID lessonId;

    /**
     * IDs of test cases to run against the code.
     * If empty, all test cases for the lesson will be used.
     */
    private List<UUID> testCaseIds;
}
