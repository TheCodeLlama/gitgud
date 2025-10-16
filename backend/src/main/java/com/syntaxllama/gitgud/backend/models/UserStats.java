package com.syntaxllama.gitgud.backend.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * User statistics entity for gamification tracking.
 * One-to-one relationship with User.
 */
@Entity
@Table(name = "user_stats")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class UserStats extends BaseEntity {

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "total_xp", nullable = false)
    private Long totalXp = 0L;

    @Column(name = "current_level", nullable = false)
    private Integer currentLevel = 1;

    @Column(name = "xp_to_next_level", nullable = false)
    private Long xpToNextLevel = 100L;

    /**
     * Stored streak value - the actual database value.
     * Use getCurrentStreakDays() to get the computed value that respects the 48-hour window.
     */
    @Column(name = "current_streak_days", nullable = false)
    private Integer storedStreakDays = 0;

    @Column(name = "longest_streak_days", nullable = false)
    private Integer longestStreakDays = 0;

    @Column(name = "last_activity_date")
    private LocalDate lastActivityDate;

    @Column(name = "last_activity_date_time")
    private LocalDateTime lastActivityDateTime;

    /**
     * Computed property for current streak days.
     * Automatically returns 0 if more than 48 hours have passed since last activity.
     * @return Current streak days (0 if streak is broken)
     */
    @Transient
    public Integer getCurrentStreakDays() {
        if (lastActivityDateTime != null &&
            lastActivityDateTime.isBefore(LocalDateTime.now().minusHours(48))) {
            return 0;
        }
        return storedStreakDays != null ? storedStreakDays : 0;
    }

    /**
     * Update the stored streak value.
     * Use this when incrementing or resetting the streak.
     * @param streakDays New streak value to store
     */
    public void setCurrentStreakDays(Integer streakDays) {
        this.storedStreakDays = streakDays;
    }
}
