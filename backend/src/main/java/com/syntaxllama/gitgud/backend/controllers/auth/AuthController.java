package com.syntaxllama.gitgud.backend.controllers.auth;

import com.syntaxllama.gitgud.backend.dtos.ApiResponse;
import com.syntaxllama.gitgud.backend.dtos.auth.UserProfileDTO;
import com.syntaxllama.gitgud.backend.exceptions.BadRequestException;
import com.syntaxllama.gitgud.backend.exceptions.UnauthorizedException;
import com.syntaxllama.gitgud.backend.models.User;
import com.syntaxllama.gitgud.backend.security.AuthenticationUtil;
import com.syntaxllama.gitgud.backend.services.UserSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for authentication and user profile operations.
 * Handles user synchronization with Firebase and profile management.
 *
 * Note: User registration is handled by Firebase SDK on the client side,
 * so no /register endpoint is needed here.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UserSyncService userSyncService;

    /**
     * Sync the current authenticated user from Firebase to the local database.
     * This endpoint is called by the frontend after successful Firebase login to ensure
     * the user exists in our database before making other API calls.
     *
     * @return The synced user profile
     */
    @PostMapping("/sync")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<UserProfileDTO> syncUser() {
        log.debug("Syncing user from Firebase token");

        // Extract user info from Firebase token
        String firebaseUid = AuthenticationUtil.getCurrentFirebaseUid()
                .orElseThrow(() -> new UnauthorizedException("No authenticated user found"));

        String email = AuthenticationUtil.getCurrentUserEmail()
                .orElseThrow(() -> new BadRequestException("Email not found in Firebase token"));

        String username = AuthenticationUtil.getCurrentUsername()
                .orElse(email); // Fall back to email if username not available

        // Sync user to database
        User user = userSyncService.syncUser(firebaseUid, email, username);

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

        // Extract Firebase UID from token
        String firebaseUid = AuthenticationUtil.getCurrentFirebaseUid()
                .orElseThrow(() -> new UnauthorizedException("No authenticated user found"));

        // Get user from database
        User user = userSyncService.getUserByFirebaseUid(firebaseUid);

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
                .firebaseUid(user.getFirebaseUid())
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
