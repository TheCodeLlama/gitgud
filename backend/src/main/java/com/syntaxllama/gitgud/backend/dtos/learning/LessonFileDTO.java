package com.syntaxllama.gitgud.backend.dtos.learning;

import com.syntaxllama.gitgud.backend.models.LessonFile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO for LessonFile entity.
 * Used to transfer file data to the frontend for multi-file lessons.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LessonFileDTO {

    private UUID id;
    private String filename;
    private String path;
    private String starterContent;
    private String solutionContent; // Only included for admins/instructors
    private Boolean editable;
    private Boolean visible;
    private Integer displayOrder;
    private String fileType;

    /**
     * Convert LessonFile entity to DTO for regular users.
     * Solution content is excluded for security.
     */
    public static LessonFileDTO fromEntity(LessonFile file) {
        return fromEntity(file, false);
    }

    /**
     * Convert LessonFile entity to DTO.
     * @param file The LessonFile entity
     * @param includeSolution Whether to include solution content (for admins/instructors)
     */
    public static LessonFileDTO fromEntity(LessonFile file, boolean includeSolution) {
        return LessonFileDTO.builder()
                .id(file.getId())
                .filename(file.getFilename())
                .path(file.getPath())
                .starterContent(file.getStarterContent())
                .solutionContent(includeSolution ? file.getSolutionContent() : null)
                .editable(file.getEditable())
                .visible(file.getVisible())
                .displayOrder(file.getDisplayOrder())
                .fileType(file.getFileType() != null ? file.getFileType().name() : null)
                .build();
    }
}
