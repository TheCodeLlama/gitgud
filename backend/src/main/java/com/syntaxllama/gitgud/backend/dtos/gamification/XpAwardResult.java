package com.syntaxllama.gitgud.backend.dtos.gamification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Result DTO for XP award operations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class XpAwardResult {

    private Long xpAwarded;
    private Long totalXp;
    private Integer currentLevel;
    private Boolean leveledUp;
    private Integer newLevel;
    private Long xpToNextLevel;
    private Integer streakBonus;
}
