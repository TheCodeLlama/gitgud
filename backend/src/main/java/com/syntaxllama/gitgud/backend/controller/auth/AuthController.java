package com.syntaxllama.gitgud.backend.controller.auth;

import com.syntaxllama.gitgud.backend.dto.ApiResponse;
import com.syntaxllama.gitgud.backend.dto.auth.RegisterRequest;
import com.syntaxllama.gitgud.backend.dto.auth.UserProfileDTO;
import com.syntaxllama.gitgud.backend.exception.BadRequestException;
import com.syntaxllama.gitgud.backend.exception.UnauthorizedException;
import com.syntaxllama.gitgud.backend.model.User;
import com.syntaxllama.gitgud.backend.security.AuthenticationUtil;
import com.syntaxllama.gitgud.backend.service.KeycloakAdminService;
import com.syntaxllama.gitgud.backend.service.UserSyncService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller for authentication and user profile operations.
 * Handles user synchronization with Keycloak and profile management.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UserSyncService userSyncService;
    private final KeycloakAdminService keycloakAdminService;

    /**
     * Sync the current authenticated user from Keycloak to the local database.
     * This endpoint is called by the frontend after successful Keycloak login to ensure
     * the user exists in our database before making other API calls.
     *
     * @return The synced user profile
     */
    @PostMapping("/sync")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<UserProfileDTO> syncUser() {
        log.debug("Syncing user from JWT");

        // Extract user info from JWT
        String keycloakId = AuthenticationUtil.getCurrentKeycloakUserId()
                .orElseThrow(() -> new UnauthorizedException("No authenticated user found"));

        String email = AuthenticationUtil.getCurrentUserEmail()
                .orElseThrow(() -> new BadRequestException("Email not found in JWT token"));

        String username = AuthenticationUtil.getCurrentUsername()
                .orElseThrow(() -> new BadRequestException("Username not found in JWT token"));

        // Sync user to database
        User user = userSyncService.syncUser(keycloakId, email, username);

        // Convert to DTO
        UserProfileDTO dto = mapUserToDTO(user);

        return ApiResponse.success("User synced successfully", dto);
    }

    /**
     * Get the current authenticated user's profile, including stats and preferences.
     * This endpoint returns all user information needed by the frontend.
     *
     * @return The current user's complete profile
     */
    @GetMapping("/me")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<UserProfileDTO> getCurrentUser() {
        log.debug("Getting current user profile");

        // Extract Keycloak ID from JWT
        String keycloakId = AuthenticationUtil.getCurrentKeycloakUserId()
                .orElseThrow(() -> new UnauthorizedException("No authenticated user found"));

        // Get user from database
        User user = userSyncService.getUserByKeycloakId(keycloakId);

        // Convert to DTO
        UserProfileDTO dto = mapUserToDTO(user);

        return ApiResponse.success(dto);
    }

    /**
     * Register a new user in Keycloak.
     * This endpoint is publicly accessible and creates a new user account.
     *
     * @param request The registration request containing user details
     * @return Success message with user ID
     */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Map<String, String>> registerUser(@Valid @RequestBody RegisterRequest request) {
        log.info("=== REGISTRATION REQUEST RECEIVED ===");
        log.info("Registering new user: username={}, email={}", request.getUsername(), request.getEmail());
        log.debug("Full registration request: {}", request);

        try {
            log.debug("Step 1: Creating user in Keycloak...");
            // Create user in Keycloak
            String keycloakUserId = keycloakAdminService.createUser(request);
            log.info("User created in Keycloak with ID: {}", keycloakUserId);

            log.debug("Step 2: Syncing user to local database...");
            // Sync user to local database
            User user = userSyncService.syncUser(keycloakUserId, request.getEmail(), request.getUsername());
            log.info("User synced to database with ID: {}", user.getId());

            Map<String, String> response = new HashMap<>();
            response.put("keycloakId", keycloakUserId);
            response.put("userId", user.getId().toString());
            response.put("username", user.getUsername());
            response.put("email", user.getEmail());

            log.info("=== REGISTRATION SUCCESSFUL ===");
            return ApiResponse.success("User registered successfully", response);

        } catch (BadRequestException e) {
            log.warn("Registration failed (BadRequestException): {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during registration", e);
            throw new BadRequestException("Registration failed: " + e.getMessage());
        }
    }

    /**
     * Map User entity to UserProfileDTO.
     */
    private UserProfileDTO mapUserToDTO(User user) {
        return UserProfileDTO.builder()
                .id(user.getId())
                .keycloakId(user.getKeycloakId())
                .email(user.getEmail())
                .username(user.getUsername())
                .displayName(user.getProfile() != null ? user.getProfile().getDisplayName() : user.getUsername())
                .avatarUrl(user.getProfile() != null ? user.getProfile().getAvatarUrl() : null)
                .bio(user.getProfile() != null ? user.getProfile().getBio() : null)
                .publicProfile(user.getProfile() != null ? user.getProfile().getIsPublic() : false)
                .totalXp(user.getStats() != null ? user.getStats().getTotalXp() : 0L)
                .currentLevel(user.getStats() != null ? user.getStats().getCurrentLevel() : 1)
                .xpToNextLevel(user.getStats() != null ? user.getStats().getXpToNextLevel() : 100L)
                .currentStreakDays(user.getStats() != null ? user.getStats().getCurrentStreakDays() : 0)
                .longestStreakDays(user.getStats() != null ? user.getStats().getLongestStreakDays() : 0)
                .build();
    }
}
