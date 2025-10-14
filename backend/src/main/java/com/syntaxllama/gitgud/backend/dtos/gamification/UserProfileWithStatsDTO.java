package com.syntaxllama.gitgud.backend.dtos.gamification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO combining user profile and stats information.
 * Used for the GET /profile endpoint to return all user data in one response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileWithStatsDTO {

    // User basic info
    private UUID userId;
    private String username;
    private String email;

    // Profile info
    private String displayName;
    private String avatarUrl;
    private String bio;
    private Boolean isPublic;

    // Stats
    private Long totalXp;
    private Integer currentLevel;
    private Long xpToNextLevel;
    private Integer currentStreakDays;
    private Integer longestStreakDays;
}
