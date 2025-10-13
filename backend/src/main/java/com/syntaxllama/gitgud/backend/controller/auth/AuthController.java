package com.syntaxllama.gitgud.backend.controller.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for authentication and user profile operations.
 * Handles user synchronization with Keycloak and profile management.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    // TODO: Implement endpoints:
    // GET /api/auth/me - Get current authenticated user profile
    // POST /api/auth/sync - Sync Keycloak user to local database
}
