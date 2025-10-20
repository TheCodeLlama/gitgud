package com.syntaxllama.gitgud.backend.dtos.learning;

import com.syntaxllama.gitgud.backend.models.Lesson;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO for Lesson entity with full details.
 * Includes lesson content but excludes solution code (hidden from users).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LessonDTO {

    private UUID id;
    private UUID moduleId;
    private String title;
    private String description;
    private String content;
    private Lesson.LessonType lessonType;
    private Integer xpReward;
    private Lesson.Difficulty difficulty;
    private String starterCode; // Deprecated - use files instead
    private Integer displayOrder;
    private Boolean isPublished;
    private List<String> hints;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Multi-file support
    private List<LessonFileDTO> files;

    // Docker execution configuration
    private String dockerImage;
    private String buildCommand;
    private String runCommand;
    private String workingDirectory;

    /**
     * Convert Lesson entity to DTO.
     * Note: Solution code is intentionally excluded for security.
     */
    public static LessonDTO fromEntity(Lesson lesson) {
        return LessonDTO.builder()
                .id(lesson.getId())
                .moduleId(lesson.getModule() != null ? lesson.getModule().getId() : null)
                .title(lesson.getTitle())
                .description(lesson.getDescription())
                .content(lesson.getContent())
                .lessonType(lesson.getLessonType())
                .xpReward(lesson.getXpReward())
                .difficulty(lesson.getDifficulty())
                .starterCode(lesson.getStarterCode())
                .displayOrder(lesson.getDisplayOrder())
                .isPublished(lesson.getIsPublished())
                .hints(lesson.getHints())
                .createdAt(lesson.getCreatedAt())
                .updatedAt(lesson.getUpdatedAt())
                // Multi-file support
                .files(lesson.getFiles() != null ?
                        lesson.getFiles().stream()
                                .map(LessonFileDTO::fromEntity)
                                .toList() : null)
                // Docker execution configuration
                .dockerImage(lesson.getDockerImage())
                .buildCommand(lesson.getBuildCommand())
                .runCommand(lesson.getRunCommand())
                .workingDirectory(lesson.getWorkingDirectory())
                .build();
    }
}
