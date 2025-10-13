package com.syntaxllama.gitgud.backend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Standard error response structure.
 * Used by global exception handler to provide consistent error responses.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private boolean success = false;
    private String message;
    private String error;
    private Integer status;
    private String path;
    private LocalDateTime timestamp;
    private List<String> details;

    /**
     * Create an error response with basic information.
     */
    public static ErrorResponse of(String message, String error, Integer status, String path) {
        return new ErrorResponse(false, message, error, status, path, LocalDateTime.now(), null);
    }

    /**
     * Create an error response with validation details.
     */
    public static ErrorResponse of(String message, String error, Integer status, String path, List<String> details) {
        return new ErrorResponse(false, message, error, status, path, LocalDateTime.now(), details);
    }
}
