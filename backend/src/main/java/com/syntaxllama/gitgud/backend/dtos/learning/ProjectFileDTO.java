package com.syntaxllama.gitgud.backend.dtos.learning;

import com.syntaxllama.gitgud.backend.models.ProjectFile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Data Transfer Object for ProjectFile entity.
 * Contains file information for multi-file lesson projects.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectFileDTO {

    private UUID id;
    private UUID lessonId;
    private String path;
    private String content;
    private ProjectFile.FileType fileType;
    private Boolean isEditable;
    private Boolean isDeletable;
    private Boolean isVisible;
    private Boolean isRenameable;
    private Integer displayOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Convert ProjectFile entity to DTO.
     * Uses starterContent for user-facing content.
     *
     * @param file The project file entity
     * @return ProjectFileDTO
     */
    public static ProjectFileDTO fromEntity(ProjectFile file) {
        return ProjectFileDTO.builder()
                .id(file.getId())
                .lessonId(file.getLesson().getId())
                .path(file.getPath())
                .content(file.getStarterContent()) // Default to starter content
                .fileType(file.getFileType())
                .isEditable(file.getIsEditable())
                .isDeletable(file.getIsDeletable())
                .isVisible(file.getIsVisible())
                .isRenameable(file.getIsRenameable())
                .displayOrder(file.getDisplayOrder())
                .createdAt(file.getCreatedAt())
                .updatedAt(file.getUpdatedAt())
                .build();
    }

    /**
     * Convert ProjectFile entity to DTO with custom content.
     * Useful for returning user-modified code instead of starter code.
     *
     * @param file The project file entity
     * @param content Custom content to include
     * @return ProjectFileDTO
     */
    public static ProjectFileDTO fromEntityWithContent(ProjectFile file, String content) {
        ProjectFileDTO dto = fromEntity(file);
        dto.setContent(content);
        return dto;
    }
}
