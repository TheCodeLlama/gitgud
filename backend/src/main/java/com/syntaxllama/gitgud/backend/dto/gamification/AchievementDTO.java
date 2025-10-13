package com.syntaxllama.gitgud.backend.dto.gamification;

import com.syntaxllama.gitgud.backend.model.Achievement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO for achievement data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AchievementDTO {

    private UUID id;
    private String name;
    private String description;
    private String iconUrl;
    private Integer xpReward;
    private String rarity;

    /**
     * Convert Achievement entity to DTO.
     * Note: criteriaJson is intentionally excluded for security.
     *
     * @param achievement The achievement entity
     * @return AchievementDTO
     */
    public static AchievementDTO fromEntity(Achievement achievement) {
        return AchievementDTO.builder()
                .id(achievement.getId())
                .name(achievement.getName())
                .description(achievement.getDescription())
                .iconUrl(achievement.getIconUrl())
                .xpReward(achievement.getXpReward())
                .rarity(achievement.getRarity() != null ? achievement.getRarity().name() : null)
                .build();
    }
}
