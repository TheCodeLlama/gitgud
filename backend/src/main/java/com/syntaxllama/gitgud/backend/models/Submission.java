package com.syntaxllama.gitgud.backend.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Submission entity tracking code submissions for lessons.
 */
@Entity
@Table(name = "submissions")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Submission extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Status status;

    @Column(name = "passed_tests")
    private Integer passedTests;

    @Column(name = "total_tests")
    private Integer totalTests;

    @Column(name = "execution_time_ms")
    private Long executionTimeMs;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "console_output", columnDefinition = "TEXT")
    private String consoleOutput;

    @Column(name = "xp_awarded")
    private Integer xpAwarded;

    @Column(name = "test_case_results_json", columnDefinition = "TEXT")
    private String testCaseResultsJson;

    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt = LocalDateTime.now();

    public enum Status {
        PENDING,
        RUNNING,
        PASSED,
        FAILED,
        ERROR
    }
}
