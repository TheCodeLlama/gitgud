package com.syntaxllama.gitgud.backend.services.learning;

import com.syntaxllama.gitgud.backend.dtos.learning.OverallProgressDTO;
import com.syntaxllama.gitgud.backend.dtos.learning.UpdateProgressRequest;
import com.syntaxllama.gitgud.backend.dtos.learning.UserProgressDTO;
import com.syntaxllama.gitgud.backend.exceptions.ResourceNotFoundException;
import com.syntaxllama.gitgud.backend.models.Lesson;
import com.syntaxllama.gitgud.backend.models.User;
import com.syntaxllama.gitgud.backend.models.UserProgress;
import com.syntaxllama.gitgud.backend.models.UserStats;
import com.syntaxllama.gitgud.backend.repositories.LessonRepository;
import com.syntaxllama.gitgud.backend.repositories.UserProgressRepository;
import com.syntaxllama.gitgud.backend.repositories.UserStatsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing user progress tracking.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProgressService {

    private final UserProgressRepository userProgressRepository;
    private final LessonRepository lessonRepository;
    private final UserStatsRepository userStatsRepository;

    /**
     * Update user progress for a lesson.
     *
     * @param user The current user
     * @param request Progress update request
     * @return Updated progress DTO
     */
    @Transactional
    public UserProgressDTO updateProgress(User user, UpdateProgressRequest request) {
        log.debug("Updating progress for user {} on lesson {}", user.getId(), request.getLessonId());

        Lesson lesson = lessonRepository.findById(request.getLessonId())
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found with id: " + request.getLessonId()));

        // Find or create progress record
        UserProgress progress = userProgressRepository.findByUserIdAndLessonId(user.getId(), request.getLessonId())
                .orElseGet(() -> {
                    UserProgress newProgress = new UserProgress();
                    newProgress.setUser(user);
                    newProgress.setLesson(lesson);
                    newProgress.setStatus(UserProgress.Status.NOT_STARTED);
                    newProgress.setAttemptsCount(0);
                    return newProgress;
                });

        // Update progress based on request
        if (request.getStatus() != null) {
            // Track status changes
            if (progress.getStatus() == UserProgress.Status.NOT_STARTED &&
                request.getStatus() != UserProgress.Status.NOT_STARTED) {
                progress.setStartedAt(LocalDateTime.now());
            }

            if (request.getStatus() == UserProgress.Status.COMPLETED &&
                progress.getStatus() != UserProgress.Status.COMPLETED) {
                progress.setCompletedAt(LocalDateTime.now());
                updateStreak(user);
            }

            progress.setStatus(request.getStatus());
        }

        // Update score if provided
        if (request.getScore() != null) {
            progress.setAttemptsCount(progress.getAttemptsCount() + 1);

            if (progress.getBestScore() == null || request.getScore() > progress.getBestScore()) {
                progress.setBestScore(request.getScore());
            }
        }

        progress = userProgressRepository.save(progress);
        log.info("Updated progress for user {} on lesson {}: status={}, attempts={}",
                user.getId(), lesson.getId(), progress.getStatus(), progress.getAttemptsCount());

        return UserProgressDTO.fromEntity(progress);
    }

    /**
     * Get user's overall progress summary.
     *
     * @param user The current user
     * @return Overall progress DTO
     */
    @Transactional(readOnly = true)
    public OverallProgressDTO getOverallProgress(User user) {
        log.debug("Fetching overall progress for user {}", user.getId());

        List<UserProgress> allProgress = userProgressRepository.findByUserId(user.getId());
        long totalLessons = lessonRepository.count();
        long completedLessons = userProgressRepository.countByUserIdAndStatus(
                user.getId(), UserProgress.Status.COMPLETED);
        long inProgressLessons = userProgressRepository.countByUserIdAndStatus(
                user.getId(), UserProgress.Status.IN_PROGRESS);

        double completionPercentage = totalLessons > 0
                ? (completedLessons * 100.0) / totalLessons
                : 0.0;

        // Get streak from UserStats
        UserStats stats = user.getStats();
        Integer currentStreak = stats != null ? stats.getCurrentStreakDays() : 0;

        // Get recent progress (last 10 items)
        List<UserProgressDTO> recentProgress = allProgress.stream()
                .sorted((p1, p2) -> p2.getUpdatedAt().compareTo(p1.getUpdatedAt()))
                .limit(10)
                .map(UserProgressDTO::fromEntity)
                .collect(Collectors.toList());

        return OverallProgressDTO.builder()
                .totalLessons(totalLessons)
                .completedLessons(completedLessons)
                .inProgressLessons(inProgressLessons)
                .completionPercentage(completionPercentage)
                .currentStreak(currentStreak)
                .recentProgress(recentProgress)
                .build();
    }

    /**
     * Get progress for a specific lesson.
     *
     * @param user The current user
     * @param lessonId The lesson ID
     * @return Progress DTO or null if no progress exists
     */
    @Transactional(readOnly = true)
    public UserProgressDTO getProgressForLesson(User user, UUID lessonId) {
        log.debug("Fetching progress for user {} on lesson {}", user.getId(), lessonId);

        return userProgressRepository.findByUserIdAndLessonId(user.getId(), lessonId)
                .map(UserProgressDTO::fromEntity)
                .orElse(null);
    }

    /**
     * Get the lesson where user should continue (last in-progress or next not-started).
     *
     * @param user The current user
     * @return Lesson ID to continue, or null if none found
     */
    @Transactional(readOnly = true)
    public UUID getContinueLesson(User user) {
        log.debug("Finding continue lesson for user {}", user.getId());

        // First, try to find an in-progress lesson
        List<UserProgress> inProgress = userProgressRepository.findByUserIdAndStatus(
                user.getId(), UserProgress.Status.IN_PROGRESS);

        if (!inProgress.isEmpty()) {
            // Return the most recently updated in-progress lesson
            UserProgress latest = inProgress.stream()
                    .max((p1, p2) -> p1.getUpdatedAt().compareTo(p2.getUpdatedAt()))
                    .orElse(null);

            if (latest != null) {
                return latest.getLesson().getId();
            }
        }

        // If no in-progress lessons, find first not-started lesson
        List<Lesson> allLessons = lessonRepository.findAll();
        for (Lesson lesson : allLessons) {
            if (!userProgressRepository.existsByUserIdAndLessonId(user.getId(), lesson.getId())) {
                return lesson.getId();
            }
        }

        return null;
    }

    /**
     * Update user's streak based on activity.
     * Called when a lesson is completed.
     *
     * @param user The current user
     */
    private void updateStreak(User user) {
        UserStats stats = user.getStats();
        if (stats == null) {
            log.warn("UserStats not found for user {}", user.getId());
            return;
        }

        LocalDate today = LocalDate.now();
        LocalDate lastActivity = stats.getLastActivityDate();

        if (lastActivity == null) {
            // First activity ever
            stats.setCurrentStreakDays(1);
            stats.setLongestStreakDays(1);
        } else if (lastActivity.equals(today)) {
            // Already active today, no change
            return;
        } else if (lastActivity.equals(today.minusDays(1))) {
            // Consecutive day - increment streak
            stats.setCurrentStreakDays(stats.getCurrentStreakDays() + 1);

            if (stats.getCurrentStreakDays() > stats.getLongestStreakDays()) {
                stats.setLongestStreakDays(stats.getCurrentStreakDays());
            }
        } else {
            // Streak broken - reset to 1
            stats.setCurrentStreakDays(1);
        }

        stats.setLastActivityDate(today);
        userStatsRepository.save(stats);

        log.info("Updated streak for user {}: current={}, longest={}",
                user.getId(), stats.getCurrentStreakDays(), stats.getLongestStreakDays());
    }
}
