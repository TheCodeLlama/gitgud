package com.syntaxllama.gitgud.backend.dto.gamification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for level information.
 * Contains metadata about a specific level including title, XP requirements, and visual data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LevelInfoDTO {

    private Integer level;
    private Long xpRequired;
    private Long xpToNextLevel;
    private String title;
    private String badgeUrl;
    private String tierName;
}
