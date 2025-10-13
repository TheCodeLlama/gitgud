package com.syntaxllama.gitgud.backend.controller.gamification;

import com.syntaxllama.gitgud.backend.controller.BaseController;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for gamification operations.
 * Handles XP, levels, achievements, and user stats.
 */
@RestController
@RequestMapping("/gamification")
@RequiredArgsConstructor
@Slf4j
public class GamificationController extends BaseController {

    // TODO: Implement endpoints:
    // GET /api/v1/gamification/stats/{userId} - Get user stats (XP, level, streak)
    // POST /api/v1/gamification/xp - Award XP to user
    // GET /api/v1/gamification/achievements - List all achievements
    // GET /api/v1/gamification/achievements/{userId} - Get user's achievements
    // GET /api/v1/gamification/levels - Get level metadata
    // GET /api/v1/gamification/profile - Get user profile
    // PUT /api/v1/gamification/profile - Update user profile
}
