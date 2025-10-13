package com.syntaxllama.gitgud.backend.controller.auth;

import com.syntaxllama.gitgud.backend.controller.BaseController;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for authentication and user profile operations.
 * Handles user synchronization with Keycloak and profile management.
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController extends BaseController {

    // TODO: Implement endpoints:
    // GET /api/v1/auth/me - Get current authenticated user profile
    // POST /api/v1/auth/sync - Sync Keycloak user to local database
}
