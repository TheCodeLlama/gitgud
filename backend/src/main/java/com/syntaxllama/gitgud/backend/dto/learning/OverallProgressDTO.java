package com.syntaxllama.gitgud.backend.dto.learning;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for user's overall progress across all lessons.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OverallProgressDTO {

    private Long totalLessons;
    private Long completedLessons;
    private Long inProgressLessons;
    private Double completionPercentage;
    private Integer currentStreak;
    private List<UserProgressDTO> recentProgress;
}
