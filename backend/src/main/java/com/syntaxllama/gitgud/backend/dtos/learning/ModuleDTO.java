package com.syntaxllama.gitgud.backend.dtos.learning;

import com.syntaxllama.gitgud.backend.models.Lesson;
import com.syntaxllama.gitgud.backend.models.Module;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * DTO for Module entity.
 * Used to transfer module data to frontend clients.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModuleDTO {

    private UUID id;
    private String title;
    private String description;
    private Module.Difficulty difficulty;
    private Integer requiredLevel;
    private Integer estimatedTimeMinutes;
    private Integer displayOrder;
    private Boolean isPublished;
    private Integer lessonCount;
    private List<SimpleLessonDTO> lessons;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Convert Module entity to DTO.
     */
    public static ModuleDTO fromEntity(Module module) {
        List<SimpleLessonDTO> lessonDTOs = module.getLessons() != null
                ? module.getLessons().stream()
                        .filter(Lesson::getIsPublished)
                        .sorted((l1, l2) -> l1.getDisplayOrder().compareTo(l2.getDisplayOrder()))
                        .map(SimpleLessonDTO::fromEntity)
                        .collect(Collectors.toList())
                : List.of();

        return ModuleDTO.builder()
                .id(module.getId())
                .title(module.getTitle())
                .description(module.getDescription())
                .difficulty(module.getDifficulty())
                .requiredLevel(module.getRequiredLevel())
                .estimatedTimeMinutes(module.getEstimatedTimeMinutes())
                .displayOrder(module.getDisplayOrder())
                .isPublished(module.getIsPublished())
                .lessonCount(lessonDTOs.size())
                .lessons(lessonDTOs)
                .createdAt(module.getCreatedAt())
                .updatedAt(module.getUpdatedAt())
                .build();
    }

    /**
     * Simple lesson info for module listings (just ID needed for progress calculation)
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SimpleLessonDTO {
        private UUID id;

        public static SimpleLessonDTO fromEntity(Lesson lesson) {
            return SimpleLessonDTO.builder()
                    .id(lesson.getId())
                    .build();
        }
    }
}
