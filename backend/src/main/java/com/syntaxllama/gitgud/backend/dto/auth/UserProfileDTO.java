package com.syntaxllama.gitgud.backend.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO for user profile information returned to the frontend.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDTO {

    private UUID id;
    private String keycloakId;
    private String email;
    private String username;

    // From UserProfile
    private String displayName;
    private String avatarUrl;
    private String bio;
    private Boolean publicProfile;

    // From UserStats
    private Long totalXp;
    private Integer currentLevel;
    private Long xpToNextLevel;
    private Integer currentStreakDays;
    private Integer longestStreakDays;
}
