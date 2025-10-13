package com.syntaxllama.gitgud.backend.service.gamification;

import com.syntaxllama.gitgud.backend.dto.gamification.LevelInfoDTO;
import com.syntaxllama.gitgud.backend.dto.gamification.LevelProgressDTO;
import com.syntaxllama.gitgud.backend.model.User;
import com.syntaxllama.gitgud.backend.model.UserStats;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Service for managing the level system.
 * Provides level metadata, progression information, and tier definitions.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LevelService {

    private final XpService xpService;

    // Level progression constants (must match XpService)
    private static final int BASE_XP = 100;
    private static final double LEVEL_EXPONENT = 1.5;

    // Level tier definitions
    private static final String TIER_NOVICE = "Novice";
    private static final String TIER_APPRENTICE = "Apprentice";
    private static final String TIER_DEVELOPER = "Developer";
    private static final String TIER_EXPERT = "Expert";

    /**
     * Get level information for a specific level.
     *
     * @param level The level number
     * @return LevelInfoDTO with level metadata
     */
    public LevelInfoDTO getLevelInfo(int level) {
        long xpRequired = xpService.calculateXpForLevel(level);
        long xpToNext = xpService.calculateXpForLevel(level + 1) - xpRequired;
        String tierName = getTierName(level);
        String title = getLevelTitle(level, tierName);
        String badgeUrl = getBadgeUrl(level);

        return LevelInfoDTO.builder()
                .level(level)
                .xpRequired(xpRequired)
                .xpToNextLevel(xpToNext)
                .title(title)
                .badgeUrl(badgeUrl)
                .tierName(tierName)
                .build();
    }

    /**
     * Get level information for a range of levels.
     *
     * @param startLevel Starting level (inclusive)
     * @param endLevel Ending level (inclusive)
     * @return List of LevelInfoDTO
     */
    public List<LevelInfoDTO> getLevelRange(int startLevel, int endLevel) {
        List<LevelInfoDTO> levels = new ArrayList<>();
        for (int level = startLevel; level <= endLevel; level++) {
            levels.add(getLevelInfo(level));
        }
        return levels;
    }

    /**
     * Get all level information (levels 1-50).
     * This provides a lookup table for the frontend.
     *
     * @return List of all level metadata
     */
    public List<LevelInfoDTO> getAllLevels() {
        return getLevelRange(1, 50);
    }

    /**
     * Get level progress visualization data for a user.
     *
     * @param user The user
     * @return LevelProgressDTO with progress bar data
     */
    public LevelProgressDTO getLevelProgress(User user) {
        UserStats stats = xpService.getOrCreateUserStats(user);
        int currentLevel = stats.getCurrentLevel();
        long totalXp = stats.getTotalXp();

        // Calculate XP in current level
        long xpForCurrentLevel = xpService.calculateXpForLevel(currentLevel);
        long xpForNextLevel = xpService.calculateXpForLevel(currentLevel + 1);
        long xpInCurrentLevel = totalXp - xpForCurrentLevel;
        long xpNeededForNextLevel = xpForNextLevel - xpForCurrentLevel;

        // Calculate progress percentage
        double progressPercentage = (double) xpInCurrentLevel / xpNeededForNextLevel * 100.0;

        // Get level titles
        String currentTierName = getTierName(currentLevel);
        String currentLevelTitle = getLevelTitle(currentLevel, currentTierName);
        String nextTierName = getTierName(currentLevel + 1);
        String nextLevelTitle = getLevelTitle(currentLevel + 1, nextTierName);

        return LevelProgressDTO.builder()
                .currentLevel(currentLevel)
                .totalXp(totalXp)
                .xpInCurrentLevel(xpInCurrentLevel)
                .xpNeededForNextLevel(xpNeededForNextLevel)
                .progressPercentage(progressPercentage)
                .currentLevelTitle(currentLevelTitle)
                .nextLevelTitle(nextLevelTitle)
                .currentTierName(currentTierName)
                .build();
    }

    /**
     * Get tier name based on level.
     *
     * @param level The level number
     * @return Tier name (Novice, Apprentice, Developer, Expert)
     */
    private String getTierName(int level) {
        if (level <= 5) {
            return TIER_NOVICE;
        } else if (level <= 10) {
            return TIER_APPRENTICE;
        } else if (level <= 20) {
            return TIER_DEVELOPER;
        } else {
            return TIER_EXPERT;
        }
    }

    /**
     * Get level title based on level and tier.
     *
     * @param level Level number
     * @param tierName Tier name
     * @return Formatted level title
     */
    private String getLevelTitle(int level, String tierName) {
        return tierName + " Level " + level;
    }

    /**
     * Get badge URL for a level.
     * Placeholder implementation - returns a path that can be replaced with actual badge images.
     *
     * @param level Level number
     * @return Badge URL path
     */
    private String getBadgeUrl(int level) {
        String tierName = getTierName(level);
        return "/badges/" + tierName.toLowerCase() + "/" + level + ".png";
    }

    /**
     * Calculate XP required for a specific level.
     * This is a convenience method that delegates to XpService.
     *
     * @param level Target level
     * @return Total XP required
     */
    public long calculateXpForLevel(int level) {
        return xpService.calculateXpForLevel(level);
    }
}
