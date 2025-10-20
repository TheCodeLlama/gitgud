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
    private Lesson.ProjectType projectType;
    private Integer xpReward;
    private Lesson.Difficulty difficulty;
    private String starterCode; // DEPRECATED: Only for backward compatibility, use projectFiles instead
    private Integer displayOrder;
    private Boolean isPublished;
    private List<String> hints;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Convert Lesson entity to DTO.
     * Note: Solution code is intentionally excluded for security.
     * For single-file lessons, starterCode is populated from the first project file.
     * For multi-file lessons, use the /files endpoint to get project files.
     */
    public static LessonDTO fromEntity(Lesson lesson) {
        // For single-file lessons, get starter code from first project file
        String starterCode = null;
        if (lesson.getProjectType() == Lesson.ProjectType.JAVA_SINGLE_FILE &&
            lesson.getProjectFiles() != null && !lesson.getProjectFiles().isEmpty()) {
            starterCode = lesson.getProjectFiles().get(0).getStarterContent();
        }

        return LessonDTO.builder()
                .id(lesson.getId())
                .moduleId(lesson.getModule() != null ? lesson.getModule().getId() : null)
                .title(lesson.getTitle())
                .description(lesson.getDescription())
                .content(lesson.getContent())
                .lessonType(lesson.getLessonType())
                .projectType(lesson.getProjectType())
                .xpReward(lesson.getXpReward())
                .difficulty(lesson.getDifficulty())
                .starterCode(starterCode)
                .displayOrder(lesson.getDisplayOrder())
                .isPublished(lesson.getIsPublished())
                .hints(lesson.getHints())
                .createdAt(lesson.getCreatedAt())
                .updatedAt(lesson.getUpdatedAt())
                .build();
    }
}
