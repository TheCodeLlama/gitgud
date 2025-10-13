package com.syntaxllama.gitgud.backend.dto.gamification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Request DTO for awarding XP to a user.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AwardXpRequest {

    private UUID lessonId;
    private Integer baseXp;
    private Boolean firstAttempt;
    private String difficulty;
}
