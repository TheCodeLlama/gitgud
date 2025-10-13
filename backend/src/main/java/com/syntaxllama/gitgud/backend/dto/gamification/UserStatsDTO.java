package com.syntaxllama.gitgud.backend.dto.gamification;

import com.syntaxllama.gitgud.backend.model.UserStats;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO for user statistics.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatsDTO {

    private UUID id;
    private UUID userId;
    private Long totalXp;
    private Integer currentLevel;
    private Long xpToNextLevel;
    private Integer currentStreakDays;
    private Integer longestStreakDays;
    private LocalDate lastActivityDate;

    /**
     * Convert UserStats entity to DTO.
     *
     * @param stats The user stats entity
     * @return UserStatsDTO
     */
    public static UserStatsDTO fromEntity(UserStats stats) {
        return UserStatsDTO.builder()
                .id(stats.getId())
                .userId(stats.getUser() != null ? stats.getUser().getId() : null)
                .totalXp(stats.getTotalXp())
                .currentLevel(stats.getCurrentLevel())
                .xpToNextLevel(stats.getXpToNextLevel())
                .currentStreakDays(stats.getCurrentStreakDays())
                .longestStreakDays(stats.getLongestStreakDays())
                .lastActivityDate(stats.getLastActivityDate())
                .build();
    }
}
