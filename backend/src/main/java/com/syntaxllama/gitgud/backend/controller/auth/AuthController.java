package com.syntaxllama.gitgud.backend.controller.auth;

import com.syntaxllama.gitgud.backend.controller.BaseController;
import com.syntaxllama.gitgud.backend.dto.ApiResponse;
import com.syntaxllama.gitgud.backend.dto.auth.UserProfileDTO;
import com.syntaxllama.gitgud.backend.exception.BadRequestException;
import com.syntaxllama.gitgud.backend.exception.UnauthorizedException;
import com.syntaxllama.gitgud.backend.model.User;
import com.syntaxllama.gitgud.backend.security.AuthenticationUtil;
import com.syntaxllama.gitgud.backend.service.UserSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for authentication and user profile operations.
 * Handles user synchronization with Keycloak and profile management.
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController extends BaseController {

    private final UserSyncService userSyncService;

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
