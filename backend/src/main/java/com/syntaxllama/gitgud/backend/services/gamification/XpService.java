package com.syntaxllama.gitgud.backend.services.gamification;

import com.syntaxllama.gitgud.backend.dtos.gamification.AwardXpRequest;
import com.syntaxllama.gitgud.backend.dtos.gamification.UserStatsDTO;
import com.syntaxllama.gitgud.backend.dtos.gamification.XpAwardResult;
import com.syntaxllama.gitgud.backend.exceptions.ResourceNotFoundException;
import com.syntaxllama.gitgud.backend.models.User;
import com.syntaxllama.gitgud.backend.models.UserStats;
import com.syntaxllama.gitgud.backend.repositories.UserStatsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Service for managing XP, levels, and streaks.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class XpService {

    private final UserStatsRepository userStatsRepository;

    // XP calculation constants
    private static final double EASY_MULTIPLIER = 1.0;
    private static final double MEDIUM_MULTIPLIER = 1.5;
    private static final double HARD_MULTIPLIER = 2.0;
    private static final double FIRST_ATTEMPT_BONUS_MULTIPLIER = 1.25;
    private static final int XP_PER_STREAK_DAY = 5; // Bonus XP per streak day

    // Level progression constants (exponential growth)
    private static final int BASE_XP = 100;
    private static final double LEVEL_EXPONENT = 1.5;

    /**
     * Get or create user stats.
     *
     * @param user The user
     * @return UserStats entity
     */
    @Transactional
    public UserStats getOrCreateUserStats(User user) {
        return userStatsRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    log.info("Creating new UserStats for user: {}", user.getId());
                    UserStats stats = new UserStats();
                    stats.setUser(user);
                    stats.setTotalXp(0L);
                    stats.setCurrentLevel(1);
                    stats.setXpToNextLevel(calculateXpForLevel(2));
                    stats.setCurrentStreakDays(0);
                    stats.setLongestStreakDays(0);
                    stats.setLastActivityDate(null);
                    return userStatsRepository.save(stats);
                });
    }

    /**
     * Get user stats by user ID.
     *
     * @param user The user
     * @return UserStatsDTO
     * @throws ResourceNotFoundException if user stats not found
     */
    @Transactional(readOnly = true)
    public UserStatsDTO getUserStats(User user) {
        UserStats stats = getOrCreateUserStats(user);
        return UserStatsDTO.fromEntity(stats);
    }

    /**
     * Award XP to a user based on lesson completion.
     *
     * @param user The user
     * @param request Award XP request containing lesson details
     * @return XpAwardResult with level-up info
     */
    @Transactional
    public XpAwardResult awardXp(User user, AwardXpRequest request) {
        log.info("Awarding XP to user: {}, lesson: {}", user.getId(), request.getLessonId());

        UserStats stats = getOrCreateUserStats(user);

        // Update streak before calculating XP
        updateStreak(stats);

        // Calculate total XP to award
        long xpToAward = calculateXp(request, stats.getCurrentStreakDays());

        // Update total XP
        long oldTotalXp = stats.getTotalXp();
        stats.setTotalXp(oldTotalXp + xpToAward);

        // Check for level up
        boolean leveledUp = false;
        Integer newLevel = null;
        int oldLevel = stats.getCurrentLevel();

        while (stats.getTotalXp() >= stats.getXpToNextLevel()) {
            stats.setCurrentLevel(stats.getCurrentLevel() + 1);
            leveledUp = true;
            newLevel = stats.getCurrentLevel();

            // Calculate XP required for next level
            long xpForNextLevel = calculateXpForLevel(stats.getCurrentLevel() + 1);
            stats.setXpToNextLevel(xpForNextLevel);

            log.info("User {} leveled up! New level: {}", user.getId(), stats.getCurrentLevel());
        }

        // Save updated stats
        userStatsRepository.save(stats);

        // Build result
        return XpAwardResult.builder()
                .xpAwarded(xpToAward)
                .totalXp(stats.getTotalXp())
                .currentLevel(stats.getCurrentLevel())
                .leveledUp(leveledUp)
                .newLevel(newLevel)
                .xpToNextLevel(stats.getXpToNextLevel() - stats.getTotalXp())
                .streakBonus(stats.getCurrentStreakDays() * XP_PER_STREAK_DAY)
                .build();
    }

    /**
     * Calculate XP to award based on lesson difficulty, streak, and first attempt bonus.
     *
     * @param request Award XP request
     * @param currentStreakDays Current streak days
     * @return Total XP to award
     */
    private long calculateXp(AwardXpRequest request, int currentStreakDays) {
        int baseXp = request.getBaseXp() != null ? request.getBaseXp() : 10;

        // Apply difficulty multiplier
        double difficultyMultiplier = getDifficultyMultiplier(request.getDifficulty());
        double xp = baseXp * difficultyMultiplier;

        // Apply first attempt bonus
        if (Boolean.TRUE.equals(request.getFirstAttempt())) {
            xp *= FIRST_ATTEMPT_BONUS_MULTIPLIER;
            log.debug("First attempt bonus applied: {}", FIRST_ATTEMPT_BONUS_MULTIPLIER);
        }

        // Add streak bonus
        int streakBonus = currentStreakDays * XP_PER_STREAK_DAY;
        xp += streakBonus;

        log.debug("XP calculation - Base: {}, Difficulty: {}, First Attempt: {}, Streak Bonus: {}, Total: {}",
                baseXp, difficultyMultiplier, request.getFirstAttempt(), streakBonus, (long) xp);

        return (long) xp;
    }

    /**
     * Get difficulty multiplier based on lesson difficulty.
     *
     * @param difficulty Difficulty string (EASY, MEDIUM, HARD)
     * @return Multiplier value
     */
    private double getDifficultyMultiplier(String difficulty) {
        if (difficulty == null) {
            return EASY_MULTIPLIER;
        }

        return switch (difficulty.toUpperCase()) {
            case "EASY" -> EASY_MULTIPLIER;
            case "MEDIUM" -> MEDIUM_MULTIPLIER;
            case "HARD" -> HARD_MULTIPLIER;
            default -> EASY_MULTIPLIER;
        };
    }

    /**
     * Calculate XP required to reach a specific level.
     * Formula: BASE_XP * (level - 1) ^ LEVEL_EXPONENT
     *
     * @param level Target level
     * @return Total XP required
     */
    public long calculateXpForLevel(int level) {
        if (level <= 1) {
            return 0L;
        }
        // Exponential growth: 100, 250, 475, 800, 1225, 1750, ...
        return (long) (BASE_XP * Math.pow(level - 1, LEVEL_EXPONENT));
    }

    /**
     * Update user's streak based on last activity date.
     * - If last activity was yesterday, increment streak
     * - If last activity was today, do nothing
     * - Otherwise, reset streak to 1
     *
     * @param stats User stats to update
     */
    public void updateStreak(UserStats stats) {
        LocalDate today = LocalDate.now();
        LocalDate lastActivity = stats.getLastActivityDate();

        if (lastActivity == null) {
            // First activity ever
            stats.setCurrentStreakDays(1);
            stats.setLongestStreakDays(1);
            stats.setLastActivityDate(today);
            log.info("Started new streak for user stats: {}", stats.getId());
        } else if (lastActivity.equals(today)) {
            // Already active today, no change
            log.debug("User already active today, streak unchanged");
        } else {
            long daysSinceLastActivity = ChronoUnit.DAYS.between(lastActivity, today);

            if (daysSinceLastActivity == 1) {
                // Yesterday - increment streak
                stats.setCurrentStreakDays(stats.getCurrentStreakDays() + 1);
                stats.setLastActivityDate(today);

                // Update longest streak if needed
                if (stats.getCurrentStreakDays() > stats.getLongestStreakDays()) {
                    stats.setLongestStreakDays(stats.getCurrentStreakDays());
                }

                log.info("Streak incremented for user stats: {}, current: {}",
                        stats.getId(), stats.getCurrentStreakDays());
            } else {
                // Missed a day - reset streak
                stats.setCurrentStreakDays(1);
                stats.setLastActivityDate(today);
                log.info("Streak reset for user stats: {}, days missed: {}",
                        stats.getId(), daysSinceLastActivity);
            }
        }
    }
}
