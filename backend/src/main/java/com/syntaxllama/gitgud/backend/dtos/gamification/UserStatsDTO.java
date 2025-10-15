package com.syntaxllama.gitgud.backend.dtos.gamification;

import com.syntaxllama.gitgud.backend.models.UserStats;
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
    private Long currentLevelXp; // XP progress within current level
    private Long xpForCurrentLevel; // Total XP needed to complete current level
    private Long xpToNextLevel; // Total XP required to reach next level (from level 1)
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
        // Calculate XP at start of current level
        long xpAtStartOfLevel = calculateXpForLevel(stats.getCurrentLevel());

        // Calculate XP at start of next level (same as xpToNextLevel stored in entity)
        long xpAtStartOfNextLevel = stats.getXpToNextLevel();

        // Calculate XP within current level
        long currentLevelXp = stats.getTotalXp() - xpAtStartOfLevel;

        // Calculate total XP needed to complete current level
        long xpForCurrentLevel = xpAtStartOfNextLevel - xpAtStartOfLevel;

        return UserStatsDTO.builder()
                .id(stats.getId())
                .userId(stats.getUser() != null ? stats.getUser().getId() : null)
                .totalXp(stats.getTotalXp())
                .currentLevel(stats.getCurrentLevel())
                .currentLevelXp(currentLevelXp)
                .xpForCurrentLevel(xpForCurrentLevel)
                .xpToNextLevel(stats.getXpToNextLevel())
                .currentStreakDays(stats.getCurrentStreakDays())
                .longestStreakDays(stats.getLongestStreakDays())
                .lastActivityDate(stats.getLastActivityDate())
                .build();
    }

    /**
     * Calculate XP required to reach a specific level.
     * Copied from XpService to avoid circular dependency.
     * Formula: BASE_XP * (level - 1) ^ LEVEL_EXPONENT
     *
     * @param level Target level
     * @return Total XP required from level 1
     */
    private static long calculateXpForLevel(int level) {
        if (level <= 1) {
            return 0L;
        }
        // Exponential growth: 100, 250, 475, 800, 1225, 1750, ...
        final int BASE_XP = 100;
        final double LEVEL_EXPONENT = 1.5;
        return (long) (BASE_XP * Math.pow(level - 1, LEVEL_EXPONENT));
    }
}
