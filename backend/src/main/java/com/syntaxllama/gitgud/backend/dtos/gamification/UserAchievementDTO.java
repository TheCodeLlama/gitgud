package com.syntaxllama.gitgud.backend.dtos.gamification;

import com.syntaxllama.gitgud.backend.models.UserAchievement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for user achievement data (earned achievements).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAchievementDTO {

    private UUID id;
    private UUID userId;
    private AchievementDTO achievement;
    private LocalDateTime earnedAt;

    /**
     * Convert UserAchievement entity to DTO.
     *
     * @param userAchievement The user achievement entity
     * @return UserAchievementDTO
     */
    public static UserAchievementDTO fromEntity(UserAchievement userAchievement) {
        return UserAchievementDTO.builder()
                .id(userAchievement.getId())
                .userId(userAchievement.getUser() != null ? userAchievement.getUser().getId() : null)
                .achievement(userAchievement.getAchievement() != null
                        ? AchievementDTO.fromEntity(userAchievement.getAchievement())
                        : null)
                .earnedAt(userAchievement.getEarnedAt())
                .build();
    }
}
