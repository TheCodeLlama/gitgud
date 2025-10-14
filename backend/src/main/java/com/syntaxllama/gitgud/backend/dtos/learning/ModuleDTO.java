package com.syntaxllama.gitgud.backend.dtos.learning;

import com.syntaxllama.gitgud.backend.models.Module;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

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
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Convert Module entity to DTO.
     */
    public static ModuleDTO fromEntity(Module module) {
        return ModuleDTO.builder()
                .id(module.getId())
                .title(module.getTitle())
                .description(module.getDescription())
                .difficulty(module.getDifficulty())
                .requiredLevel(module.getRequiredLevel())
                .estimatedTimeMinutes(module.getEstimatedTimeMinutes())
                .displayOrder(module.getDisplayOrder())
                .isPublished(module.getIsPublished())
                .lessonCount(module.getLessons() != null ? module.getLessons().size() : 0)
                .createdAt(module.getCreatedAt())
                .updatedAt(module.getUpdatedAt())
                .build();
    }
}
