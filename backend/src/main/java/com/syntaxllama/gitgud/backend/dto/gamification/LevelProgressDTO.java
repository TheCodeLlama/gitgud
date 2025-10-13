package com.syntaxllama.gitgud.backend.dto.gamification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for level progress visualization.
 * Contains all data needed to render a level progress bar in the frontend.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LevelProgressDTO {

    private Integer currentLevel;
    private Long totalXp;
    private Long xpInCurrentLevel;
    private Long xpNeededForNextLevel;
    private Double progressPercentage;
    private String currentLevelTitle;
    private String nextLevelTitle;
    private String currentTierName;
}
