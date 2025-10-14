package com.syntaxllama.gitgud.backend.services.gamification;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.syntaxllama.gitgud.backend.dtos.gamification.AchievementDTO;
import com.syntaxllama.gitgud.backend.dtos.gamification.UserAchievementDTO;
import com.syntaxllama.gitgud.backend.models.Achievement;
import com.syntaxllama.gitgud.backend.models.User;
import com.syntaxllama.gitgud.backend.models.UserAchievement;
import com.syntaxllama.gitgud.backend.models.UserStats;
import com.syntaxllama.gitgud.backend.repositories.AchievementRepository;
import com.syntaxllama.gitgud.backend.repositories.UserAchievementRepository;
import com.syntaxllama.gitgud.backend.repositories.UserProgressRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing achievements.
 * Handles achievement checking, awarding, and retrieval.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AchievementService {

    private final AchievementRepository achievementRepository;
    private final UserAchievementRepository userAchievementRepository;
    private final UserProgressRepository userProgressRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Get all available achievements.
     *
     * @return List of all achievements
     */
    @Transactional(readOnly = true)
    public List<AchievementDTO> getAllAchievements() {
        log.debug("Fetching all achievements");
        return achievementRepository.findAll().stream()
                .map(AchievementDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get achievements earned by a specific user.
     *
     * @param user The user
     * @return List of earned achievements
     */
    @Transactional(readOnly = true)
    public List<UserAchievementDTO> getUserAchievements(User user) {
        log.debug("Fetching achievements for user: {}", user.getId());
        return userAchievementRepository.findByUserIdOrderByEarnedAtDesc(user.getId()).stream()
                .map(UserAchievementDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Check and award achievements for a user based on their current stats.
     * This is idempotent - will not award duplicate achievements.
     *
     * @param user The user to check achievements for
     * @param userStats The user's current stats
     * @return List of newly awarded achievements
     */
    @Transactional
    public List<UserAchievementDTO> checkAndAwardAchievements(User user, UserStats userStats) {
        log.debug("Checking achievements for user: {}", user.getId());

        List<Achievement> allAchievements = achievementRepository.findAll();
        List<UserAchievementDTO> newlyAwarded = new ArrayList<>();

        for (Achievement achievement : allAchievements) {
            // Skip if user already has this achievement
            if (userAchievementRepository.existsByUserIdAndAchievementId(user.getId(), achievement.getId())) {
                continue;
            }

            // Check if user meets criteria
            if (meetsAchievementCriteria(user, userStats, achievement)) {
                UserAchievementDTO awarded = awardAchievement(user, achievement);
                newlyAwarded.add(awarded);
                log.info("Awarded achievement '{}' to user {}", achievement.getName(), user.getId());
            }
        }

        return newlyAwarded;
    }

    /**
     * Award a specific achievement to a user (idempotent).
     *
     * @param user The user
     * @param achievement The achievement to award
     * @return UserAchievementDTO or null if already awarded
     */
    @Transactional
    public UserAchievementDTO awardAchievement(User user, Achievement achievement) {
        // Check if already awarded
        if (userAchievementRepository.existsByUserIdAndAchievementId(user.getId(), achievement.getId())) {
            log.debug("Achievement '{}' already awarded to user {}", achievement.getName(), user.getId());
            return null;
        }

        UserAchievement userAchievement = new UserAchievement();
        userAchievement.setUser(user);
        userAchievement.setAchievement(achievement);
        userAchievement.setEarnedAt(LocalDateTime.now());

        userAchievement = userAchievementRepository.save(userAchievement);
        return UserAchievementDTO.fromEntity(userAchievement);
    }

    /**
     * Check if user meets the criteria for a specific achievement.
     *
     * @param user The user
     * @param userStats The user's stats
     * @param achievement The achievement to check
     * @return true if criteria is met
     */
    private boolean meetsAchievementCriteria(User user, UserStats userStats, Achievement achievement) {
        try {
            JsonNode criteria = objectMapper.readTree(achievement.getCriteriaJson());
            String type = criteria.get("type").asText();

            return switch (type) {
                case "lesson_complete" -> {
                    int requiredCount = criteria.get("count").asInt();
                    long completedCount = userProgressRepository.countByUserIdAndStatus(
                            user.getId(),
                            com.syntaxllama.gitgud.backend.models.UserProgress.Status.COMPLETED
                    );
                    yield completedCount >= requiredCount;
                }
                case "xp_milestone" -> {
                    long requiredXp = criteria.get("xp").asLong();
                    yield userStats.getTotalXp() >= requiredXp;
                }
                case "level_reached" -> {
                    int requiredLevel = criteria.get("level").asInt();
                    yield userStats.getCurrentLevel() >= requiredLevel;
                }
                case "streak" -> {
                    int requiredDays = criteria.get("days").asInt();
                    yield userStats.getCurrentStreakDays() >= requiredDays;
                }
                case "first_attempt_success" -> {
                    // Check if user has any lessons completed on first attempt (attemptsCount = 1)
                    yield userProgressRepository.existsByUserIdAndStatusAndAttemptsCount(
                            user.getId(),
                            com.syntaxllama.gitgud.backend.models.UserProgress.Status.COMPLETED,
                            1
                    );
                }
                case "module_complete" -> {
                    // Module completion check would require more complex logic
                    // For MVP, we'll implement this later
                    log.debug("Module completion check not yet implemented");
                    yield false;
                }
                default -> {
                    log.warn("Unknown achievement criteria type: {}", type);
                    yield false;
                }
            };
        } catch (JsonProcessingException e) {
            log.error("Failed to parse achievement criteria JSON for achievement: {}", achievement.getName(), e);
            return false;
        }
    }
}
