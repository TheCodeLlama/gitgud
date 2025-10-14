package com.syntaxllama.gitgud.backend.dtos.learning;

import com.syntaxllama.gitgud.backend.models.UserProgress;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for UserProgress entity.
 * Represents progress on a specific lesson.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProgressDTO {

    private UUID id;
    private UUID userId;
    private UUID lessonId;
    private UserProgress.Status status;
    private Integer attemptsCount;
    private Integer bestScore;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Convert UserProgress entity to DTO.
     */
    public static UserProgressDTO fromEntity(UserProgress progress) {
        return UserProgressDTO.builder()
                .id(progress.getId())
                .userId(progress.getUser() != null ? progress.getUser().getId() : null)
                .lessonId(progress.getLesson() != null ? progress.getLesson().getId() : null)
                .status(progress.getStatus())
                .attemptsCount(progress.getAttemptsCount())
                .bestScore(progress.getBestScore())
                .startedAt(progress.getStartedAt())
                .completedAt(progress.getCompletedAt())
                .createdAt(progress.getCreatedAt())
                .updatedAt(progress.getUpdatedAt())
                .build();
    }
}
