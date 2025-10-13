package com.syntaxllama.gitgud.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

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

    @Column(name = "current_streak_days", nullable = false)
    private Integer currentStreakDays = 0;

    @Column(name = "longest_streak_days", nullable = false)
    private Integer longestStreakDays = 0;

    @Column(name = "last_activity_date")
    private LocalDate lastActivityDate;
}
