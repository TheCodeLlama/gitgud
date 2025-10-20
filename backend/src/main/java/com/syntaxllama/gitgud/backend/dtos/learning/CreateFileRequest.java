package com.syntaxllama.gitgud.backend.dtos.learning;

import com.syntaxllama.gitgud.backend.models.ProjectFile;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating a new file in a lesson's project.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateFileRequest {

    @NotBlank(message = "File path is required")
    private String path;

    private String content;

    @NotNull(message = "File type is required")
    private ProjectFile.FileType fileType;

    @Builder.Default
    private Boolean isEditable = true;

    @Builder.Default
    private Boolean isDeletable = true;

    @Builder.Default
    private Boolean isVisible = true;

    @Builder.Default
    private Boolean isRenameable = true;

    @Builder.Default
    private Integer displayOrder = 0;
}
