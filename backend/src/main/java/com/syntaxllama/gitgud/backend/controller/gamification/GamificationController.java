package com.syntaxllama.gitgud.backend.controller.gamification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for gamification operations.
 * Handles XP, levels, achievements, and user stats.
 */
@RestController
@RequestMapping("/api/gamification")
@RequiredArgsConstructor
@Slf4j
public class GamificationController {

    // TODO: Implement endpoints:
    // GET /api/gamification/stats/{userId} - Get user stats (XP, level, streak)
    // POST /api/gamification/xp - Award XP to user
    // GET /api/gamification/achievements - List all achievements
    // GET /api/gamification/achievements/{userId} - Get user's achievements
    // GET /api/gamification/levels - Get level metadata
    // GET /api/gamification/profile - Get user profile
    // PUT /api/gamification/profile - Update user profile
}
