package com.syntaxllama.gitgud.backend.dto.learning;

import com.syntaxllama.gitgud.backend.model.TestCase;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO for test case data.
 * Note: Expected output is intentionally excluded for security.
 * Users should not see expected output before solving the problem.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestCaseDTO {

    private UUID id;
    private UUID lessonId;
    private String input;
    private Boolean isHidden;
    private Integer weight;
    private Integer displayOrder;
    // Note: expectedOutput is intentionally excluded for security

    /**
     * Convert TestCase entity to DTO (without expected output).
     *
     * @param testCase The test case entity
     * @return TestCaseDTO with expected output hidden
     */
    public static TestCaseDTO fromEntity(TestCase testCase) {
        return TestCaseDTO.builder()
                .id(testCase.getId())
                .lessonId(testCase.getLesson() != null ? testCase.getLesson().getId() : null)
                .input(testCase.getInput())
                .isHidden(testCase.getIsHidden())
                .weight(testCase.getWeight())
                .displayOrder(testCase.getDisplayOrder())
                .build();
    }

    /**
     * Convert TestCase entity to DTO with expected output included.
     * Use only for admin/evaluation purposes, not for public API responses.
     *
     * @param testCase The test case entity
     * @return TestCaseDTO with all fields including expected output
     */
    public static TestCaseWithOutputDTO fromEntityWithOutput(TestCase testCase) {
        return TestCaseWithOutputDTO.builder()
                .id(testCase.getId())
                .lessonId(testCase.getLesson() != null ? testCase.getLesson().getId() : null)
                .input(testCase.getInput())
                .expectedOutput(testCase.getExpectedOutput())
                .isHidden(testCase.getIsHidden())
                .weight(testCase.getWeight())
                .displayOrder(testCase.getDisplayOrder())
                .build();
    }

    /**
     * DTO that includes expected output - for internal/admin use only.
     * Should never be returned to users in public endpoints.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TestCaseWithOutputDTO {
        private UUID id;
        private UUID lessonId;
        private String input;
        private String expectedOutput;  // Only included in this internal DTO
        private Boolean isHidden;
        private Integer weight;
        private Integer displayOrder;
    }
}
