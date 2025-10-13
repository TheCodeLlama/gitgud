package com.syntaxllama.gitgud.backend.dto.learning;

import com.syntaxllama.gitgud.backend.model.Lesson;
import com.syntaxllama.gitgud.backend.model.Module;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Detailed DTO for Module entity, including lessons.
 * Used for single module view with full lesson information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModuleDetailDTO {

    private UUID id;
    private String title;
    private String description;
    private Module.Difficulty difficulty;
    private Integer requiredLevel;
    private Integer estimatedTimeMinutes;
    private Integer displayOrder;
    private Boolean isPublished;
    private List<LessonSummaryDTO> lessons;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Convert Module entity to detailed DTO.
     */
    public static ModuleDetailDTO fromEntity(Module module) {
        List<LessonSummaryDTO> lessonDTOs = module.getLessons() != null
                ? module.getLessons().stream()
                        .filter(Lesson::getIsPublished)
                        .sorted((l1, l2) -> l1.getDisplayOrder().compareTo(l2.getDisplayOrder()))
                        .map(LessonSummaryDTO::fromEntity)
                        .collect(Collectors.toList())
                : List.of();

        return ModuleDetailDTO.builder()
                .id(module.getId())
                .title(module.getTitle())
                .description(module.getDescription())
                .difficulty(module.getDifficulty())
                .requiredLevel(module.getRequiredLevel())
                .estimatedTimeMinutes(module.getEstimatedTimeMinutes())
                .displayOrder(module.getDisplayOrder())
                .isPublished(module.getIsPublished())
                .lessons(lessonDTOs)
                .createdAt(module.getCreatedAt())
                .updatedAt(module.getUpdatedAt())
                .build();
    }

    /**
     * Summary DTO for lessons within a module.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LessonSummaryDTO {
        private UUID id;
        private String title;
        private String description;
        private Lesson.LessonType lessonType;
        private Lesson.Difficulty difficulty;
        private Integer xpReward;
        private Integer displayOrder;

        public static LessonSummaryDTO fromEntity(Lesson lesson) {
            return LessonSummaryDTO.builder()
                    .id(lesson.getId())
                    .title(lesson.getTitle())
                    .description(lesson.getDescription())
                    .lessonType(lesson.getLessonType())
                    .difficulty(lesson.getDifficulty())
                    .xpReward(lesson.getXpReward())
                    .displayOrder(lesson.getDisplayOrder())
                    .build();
        }
    }
}
