package com.syntaxllama.gitgud.backend.dto.gamification;

import com.syntaxllama.gitgud.backend.model.UserProfile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO for user profile information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDTO {

    private UUID id;
    private UUID userId;
    private String displayName;
    private String avatarUrl;
    private String bio;
    private Boolean isPublic;

    /**
     * Convert UserProfile entity to DTO.
     *
     * @param profile The user profile entity
     * @return UserProfileDTO
     */
    public static UserProfileDTO fromEntity(UserProfile profile) {
        if (profile == null) {
            return null;
        }

        return UserProfileDTO.builder()
                .id(profile.getId())
                .userId(profile.getUser() != null ? profile.getUser().getId() : null)
                .displayName(profile.getDisplayName())
                .avatarUrl(profile.getAvatarUrl())
                .bio(profile.getBio())
                .isPublic(profile.getIsPublic())
                .build();
    }
}
