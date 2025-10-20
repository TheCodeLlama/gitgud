package com.syntaxllama.gitgud.backend.dtos.learning;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating a file's content.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateFileRequest {

    @NotBlank(message = "Content is required")
    private String content;
}
