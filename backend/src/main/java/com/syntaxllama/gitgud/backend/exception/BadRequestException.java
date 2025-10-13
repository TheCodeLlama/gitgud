package com.syntaxllama.gitgud.backend.exception;

/**
 * Exception thrown when a request contains invalid data or violates business rules.
 */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
