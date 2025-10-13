package com.syntaxllama.gitgud.backend.dto.gamification;

import com.syntaxllama.gitgud.backend.model.Avatar;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DTO for avatar information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvatarDTO {

    private String url;
    private String displayName;

    /**
     * Convert Avatar enum to DTO.
     *
     * @param avatar The avatar enum
     * @return AvatarDTO
     */
    public static AvatarDTO fromEnum(Avatar avatar) {
        return AvatarDTO.builder()
                .url(avatar.getUrl())
                .displayName(avatar.getDisplayName())
                .build();
    }

    /**
     * Get all available avatars as DTOs.
     *
     * @return List of all avatars
     */
    public static List<AvatarDTO> getAllAvatars() {
        return Arrays.stream(Avatar.values())
                .map(AvatarDTO::fromEnum)
                .collect(Collectors.toList());
    }
}
