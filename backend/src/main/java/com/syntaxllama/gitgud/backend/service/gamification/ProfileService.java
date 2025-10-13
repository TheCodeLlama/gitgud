package com.syntaxllama.gitgud.backend.service.gamification;

import com.syntaxllama.gitgud.backend.dto.gamification.UpdateProfileRequest;
import com.syntaxllama.gitgud.backend.dto.gamification.UserProfileDTO;
import com.syntaxllama.gitgud.backend.dto.gamification.UserProfileWithStatsDTO;
import com.syntaxllama.gitgud.backend.model.User;
import com.syntaxllama.gitgud.backend.model.UserProfile;
import com.syntaxllama.gitgud.backend.model.UserStats;
import com.syntaxllama.gitgud.backend.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing user profiles.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileService {

    private final UserProfileRepository userProfileRepository;
    private final XpService xpService;

    /**
     * Get or create user profile.
     *
     * @param user The user
     * @return UserProfile entity
     */
    @Transactional
    public UserProfile getOrCreateUserProfile(User user) {
        return userProfileRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    log.info("Creating new UserProfile for user: {}", user.getId());
                    UserProfile profile = new UserProfile();
                    profile.setUser(user);
                    profile.setDisplayName(user.getUsername());
                    profile.setAvatarUrl(null);
                    profile.setBio(null);
                    profile.setIsPublic(false);
                    return userProfileRepository.save(profile);
                });
    }

    /**
     * Get user profile with stats.
     *
     * @param user The user
     * @return UserProfileWithStatsDTO combining profile and stats
     */
    @Transactional(readOnly = true)
    public UserProfileWithStatsDTO getUserProfileWithStats(User user) {
        UserProfile profile = getOrCreateUserProfile(user);
        UserStats stats = xpService.getOrCreateUserStats(user);

        return UserProfileWithStatsDTO.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .displayName(profile.getDisplayName())
                .avatarUrl(profile.getAvatarUrl())
                .bio(profile.getBio())
                .isPublic(profile.getIsPublic())
                .totalXp(stats.getTotalXp())
                .currentLevel(stats.getCurrentLevel())
                .xpToNextLevel(stats.getXpToNextLevel() - stats.getTotalXp())
                .currentStreakDays(stats.getCurrentStreakDays())
                .longestStreakDays(stats.getLongestStreakDays())
                .build();
    }

    /**
     * Update user profile.
     *
     * @param user The user
     * @param request Update request with new profile data
     * @return Updated UserProfileDTO
     */
    @Transactional
    public UserProfileDTO updateUserProfile(User user, UpdateProfileRequest request) {
        log.info("Updating profile for user: {}", user.getId());

        UserProfile profile = getOrCreateUserProfile(user);

        // Update fields if provided
        if (request.getDisplayName() != null) {
            profile.setDisplayName(request.getDisplayName());
        }
        if (request.getAvatarUrl() != null) {
            profile.setAvatarUrl(request.getAvatarUrl());
        }
        if (request.getBio() != null) {
            profile.setBio(request.getBio());
        }
        if (request.getIsPublic() != null) {
            profile.setIsPublic(request.getIsPublic());
        }

        UserProfile savedProfile = userProfileRepository.save(profile);
        log.info("Profile updated successfully for user: {}", user.getId());

        return UserProfileDTO.fromEntity(savedProfile);
    }
}
