package com.syntaxllama.gitgud.backend.dto.learning;

import com.syntaxllama.gitgud.backend.model.Lesson;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
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
    private String starterCode;
    private Integer displayOrder;
    private Boolean isPublished;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

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
                .createdAt(lesson.getCreatedAt())
                .updatedAt(lesson.getUpdatedAt())
                .build();
    }
}
