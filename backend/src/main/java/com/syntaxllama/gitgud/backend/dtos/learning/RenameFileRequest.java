package com.syntaxllama.gitgud.backend.dtos.learning;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for renaming a project file.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RenameFileRequest {

    @NotBlank(message = "New path is required")
    private String newPath;
}
