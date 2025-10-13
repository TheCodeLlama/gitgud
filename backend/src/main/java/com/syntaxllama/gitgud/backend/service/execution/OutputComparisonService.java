package com.syntaxllama.gitgud.backend.service.execution;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service for comparing expected vs actual output from code execution.
 * Supports multiple comparison strategies:
 * - Exact match
 * - Whitespace normalization
 * - Numeric tolerance for floating-point values
 */
@Service
@Slf4j
public class OutputComparisonService {

    private static final double DEFAULT_NUMERIC_TOLERANCE = 0.0001;

    /**
     * Compare outputs with whitespace normalization.
     * This is the default comparison method for most test cases.
     *
     * @param expected Expected output
     * @param actual Actual output
     * @return true if outputs match after normalization
     */
    public boolean compareWithWhitespaceNormalization(String expected, String actual) {
        if (expected == null && actual == null) {
            return true;
        }
        if (expected == null || actual == null) {
            return false;
        }

        String normalizedExpected = normalizeWhitespace(expected);
        String normalizedActual = normalizeWhitespace(actual);

        return normalizedExpected.equals(normalizedActual);
    }

    /**
     * Compare outputs with numeric tolerance.
     * Useful for test cases involving floating-point calculations.
     *
     * @param expected Expected output (may contain numbers)
     * @param actual Actual output (may contain numbers)
     * @param tolerance Acceptable difference for numeric values
     * @return true if outputs match within tolerance
     */
    public boolean compareWithNumericTolerance(String expected, String actual, double tolerance) {
        if (expected == null && actual == null) {
            return true;
        }
        if (expected == null || actual == null) {
            return false;
        }

        // First normalize whitespace
        String normalizedExpected = normalizeWhitespace(expected);
        String normalizedActual = normalizeWhitespace(actual);

        // If exact match after normalization, return true
        if (normalizedExpected.equals(normalizedActual)) {
            return true;
        }

        // Try to parse as numbers and compare with tolerance
        return compareAsNumbers(normalizedExpected, normalizedActual, tolerance);
    }

    /**
     * Compare outputs with numeric tolerance using default tolerance.
     */
    public boolean compareWithNumericTolerance(String expected, String actual) {
        return compareWithNumericTolerance(expected, actual, DEFAULT_NUMERIC_TOLERANCE);
    }

    /**
     * Exact string comparison (no normalization).
     * Use when exact formatting is required.
     *
     * @param expected Expected output
     * @param actual Actual output
     * @return true if outputs match exactly
     */
    public boolean compareExact(String expected, String actual) {
        if (expected == null && actual == null) {
            return true;
        }
        if (expected == null || actual == null) {
            return false;
        }

        return expected.equals(actual);
    }

    /**
     * Normalize whitespace for comparison.
     * - Trim leading/trailing whitespace
     * - Normalize line endings to \n
     * - Collapse multiple spaces to single space
     * - Remove trailing whitespace from each line
     *
     * @param text Text to normalize
     * @return Normalized text
     */
    private String normalizeWhitespace(String text) {
        if (text == null) {
            return "";
        }

        // Normalize line endings
        String normalized = text.replace("\r\n", "\n").replace("\r", "\n");

        // Split into lines and process each
        String[] lines = normalized.split("\n");
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];

            // Trim trailing whitespace from each line
            line = line.replaceAll("\\s+$", "");

            // Collapse multiple spaces to single space
            line = line.replaceAll("\\s+", " ");

            // Trim leading whitespace
            line = line.trim();

            // Skip empty lines at start/end, but preserve internal empty lines
            if (!line.isEmpty() || (i > 0 && i < lines.length - 1)) {
                result.append(line);
                if (i < lines.length - 1) {
                    result.append("\n");
                }
            }
        }

        // Final trim to remove any leading/trailing newlines
        return result.toString().trim();
    }

    /**
     * Try to compare strings as numbers with tolerance.
     * Handles cases where output is a single number or multiple numbers.
     *
     * @param expected Expected value(s)
     * @param actual Actual value(s)
     * @param tolerance Acceptable difference
     * @return true if numbers match within tolerance
     */
    private boolean compareAsNumbers(String expected, String actual, double tolerance) {
        try {
            // Try single number comparison
            double expectedNum = Double.parseDouble(expected);
            double actualNum = Double.parseDouble(actual);
            boolean matches = Math.abs(expectedNum - actualNum) <= tolerance;

            if (matches) {
                log.debug("Numeric comparison: expected={}, actual={}, tolerance={}, matches=true",
                    expected, actual, tolerance);
            }

            return matches;
        } catch (NumberFormatException e) {
            // Not a single number, try line-by-line comparison
            return compareMultiLineNumbers(expected, actual, tolerance);
        }
    }

    /**
     * Compare multi-line output where each line may contain a number.
     *
     * @param expected Expected output
     * @param actual Actual output
     * @param tolerance Acceptable difference
     * @return true if all numbers match within tolerance
     */
    private boolean compareMultiLineNumbers(String expected, String actual, double tolerance) {
        String[] expectedLines = expected.split("\n");
        String[] actualLines = actual.split("\n");

        // Must have same number of lines
        if (expectedLines.length != actualLines.length) {
            return false;
        }

        for (int i = 0; i < expectedLines.length; i++) {
            String expLine = expectedLines[i].trim();
            String actLine = actualLines[i].trim();

            // If lines are equal, continue
            if (expLine.equals(actLine)) {
                continue;
            }

            // Try to parse as numbers
            try {
                double expectedNum = Double.parseDouble(expLine);
                double actualNum = Double.parseDouble(actLine);

                if (Math.abs(expectedNum - actualNum) > tolerance) {
                    log.debug("Numeric comparison failed: expected={}, actual={}, tolerance={}",
                        expLine, actLine, tolerance);
                    return false;
                }
            } catch (NumberFormatException e) {
                // Not numbers, lines must match exactly
                if (!expLine.equals(actLine)) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Comparison mode enum for future extensibility.
     */
    public enum ComparisonMode {
        EXACT,                  // Exact string match
        WHITESPACE_NORMALIZED,  // Normalize whitespace before comparison
        NUMERIC_TOLERANCE       // Compare numbers with tolerance
    }
}
