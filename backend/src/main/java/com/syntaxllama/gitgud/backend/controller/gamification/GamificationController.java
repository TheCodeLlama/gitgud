package com.syntaxllama.gitgud.backend.controller.gamification;

import com.syntaxllama.gitgud.backend.controller.BaseController;
import com.syntaxllama.gitgud.backend.dto.ApiResponse;
import com.syntaxllama.gitgud.backend.dto.gamification.AwardXpRequest;
import com.syntaxllama.gitgud.backend.dto.gamification.UserStatsDTO;
import com.syntaxllama.gitgud.backend.dto.gamification.XpAwardResult;
import com.syntaxllama.gitgud.backend.model.User;
import com.syntaxllama.gitgud.backend.security.AuthenticationUtil;
import com.syntaxllama.gitgud.backend.service.UserSyncService;
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

    // TODO: Implement remaining endpoints:
    // GET /api/v1/gamification/achievements - List all achievements
    // GET /api/v1/gamification/achievements/{userId} - Get user's achievements
    // GET /api/v1/gamification/levels - Get level metadata
    // GET /api/v1/gamification/profile - Get user profile
    // PUT /api/v1/gamification/profile - Update user profile
}
