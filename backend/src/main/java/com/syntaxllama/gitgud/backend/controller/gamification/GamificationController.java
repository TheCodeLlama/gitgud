package com.syntaxllama.gitgud.backend.controller.gamification;

import com.syntaxllama.gitgud.backend.controller.BaseController;
import com.syntaxllama.gitgud.backend.dto.ApiResponse;
import com.syntaxllama.gitgud.backend.dto.gamification.AchievementDTO;
import com.syntaxllama.gitgud.backend.dto.gamification.AwardXpRequest;
import com.syntaxllama.gitgud.backend.dto.gamification.UserAchievementDTO;
import com.syntaxllama.gitgud.backend.dto.gamification.UserStatsDTO;
import com.syntaxllama.gitgud.backend.dto.gamification.XpAwardResult;
import com.syntaxllama.gitgud.backend.model.User;

import java.util.List;
import com.syntaxllama.gitgud.backend.security.AuthenticationUtil;
import com.syntaxllama.gitgud.backend.service.UserSyncService;
import com.syntaxllama.gitgud.backend.service.gamification.AchievementService;
import com.syntaxllama.gitgud.backend.service.gamification.XpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for gamification operations.
 * Handles XP, levels, achievements, and user stats.
 */
@RestController
@RequestMapping("/gamification")
@RequiredArgsConstructor
@Slf4j
public class GamificationController extends BaseController {

    private final XpService xpService;
    private final AchievementService achievementService;
    private final UserSyncService userSyncService;

    /**
     * Get user stats (XP, level, streak) for the authenticated user.
     * Requires authentication.
     *
     * @return User stats
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<UserStatsDTO>> getUserStats() {
        String keycloakId = AuthenticationUtil.getCurrentKeycloakUserId()
                .orElseThrow(() -> new com.syntaxllama.gitgud.backend.exception.UnauthorizedException("User not authenticated"));

        User currentUser = userSyncService.getUserByKeycloakId(keycloakId);
        log.info("GET /api/v1/gamification/stats - user: {}", currentUser.getId());

        UserStatsDTO stats = xpService.getUserStats(currentUser);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    /**
     * Award XP to the authenticated user.
     * Requires authentication.
     *
     * @param request Award XP request containing lesson details
     * @return XP award result with level-up information
     */
    @PostMapping("/xp")
    public ResponseEntity<ApiResponse<XpAwardResult>> awardXp(@RequestBody AwardXpRequest request) {
        String keycloakId = AuthenticationUtil.getCurrentKeycloakUserId()
                .orElseThrow(() -> new com.syntaxllama.gitgud.backend.exception.UnauthorizedException("User not authenticated"));

        User currentUser = userSyncService.getUserByKeycloakId(keycloakId);
        log.info("POST /api/v1/gamification/xp - user: {}, lesson: {}", currentUser.getId(), request.getLessonId());

        XpAwardResult result = xpService.awardXp(currentUser, request);

        if (Boolean.TRUE.equals(result.getLeveledUp())) {
            log.info("User {} leveled up to level {}!", currentUser.getId(), result.getNewLevel());
            return ResponseEntity.ok(ApiResponse.success(
                    "Congratulations! You leveled up to level " + result.getNewLevel() + "!",
                    result
            ));
        }

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * Get all available achievements.
     * Public endpoint - no authentication required.
     *
     * @return List of all achievements
     */
    @GetMapping("/achievements")
    public ResponseEntity<ApiResponse<List<AchievementDTO>>> getAllAchievements() {
        log.info("GET /api/v1/gamification/achievements");

        List<AchievementDTO> achievements = achievementService.getAllAchievements();
        return ResponseEntity.ok(ApiResponse.success(achievements));
    }

    /**
     * Get achievements earned by the authenticated user.
     * Requires authentication.
     *
     * @return List of user's earned achievements
     */
    @GetMapping("/achievements/user")
    public ResponseEntity<ApiResponse<List<UserAchievementDTO>>> getUserAchievements() {
        String keycloakId = AuthenticationUtil.getCurrentKeycloakUserId()
                .orElseThrow(() -> new com.syntaxllama.gitgud.backend.exception.UnauthorizedException("User not authenticated"));

        User currentUser = userSyncService.getUserByKeycloakId(keycloakId);
        log.info("GET /api/v1/gamification/achievements/user - user: {}", currentUser.getId());

        List<UserAchievementDTO> userAchievements = achievementService.getUserAchievements(currentUser);
        return ResponseEntity.ok(ApiResponse.success(userAchievements));
    }

    // TODO: Implement remaining endpoints:
    // GET /api/v1/gamification/levels - Get level metadata
    // GET /api/v1/gamification/profile - Get user profile
    // PUT /api/v1/gamification/profile - Update user profile
}
