package com.syntaxllama.gitgud.backend.controller.learning;

import com.syntaxllama.gitgud.backend.controller.BaseController;
import com.syntaxllama.gitgud.backend.dto.ApiResponse;
import com.syntaxllama.gitgud.backend.dto.learning.LessonDTO;
import com.syntaxllama.gitgud.backend.dto.learning.ModuleDTO;
import com.syntaxllama.gitgud.backend.dto.learning.ModuleDetailDTO;
import com.syntaxllama.gitgud.backend.model.Module;
import com.syntaxllama.gitgud.backend.model.User;
import com.syntaxllama.gitgud.backend.security.AuthenticationUtil;
import com.syntaxllama.gitgud.backend.service.learning.LessonService;
import com.syntaxllama.gitgud.backend.service.learning.ModuleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controller for learning-related operations.
 * Handles modules, lessons, and user progress tracking.
 */
@RestController
@RequestMapping("/learning")
@RequiredArgsConstructor
@Slf4j
public class LearningController extends BaseController {

    private final ModuleService moduleService;
    private final LessonService lessonService;
    private final com.syntaxllama.gitgud.backend.service.UserSyncService userSyncService;

    /**
     * Get all published modules.
     * Public endpoint - no authentication required.
     *
     * @param difficulty Optional filter by difficulty level
     * @param userLevel Optional filter by user level (returns modules available for that level)
     * @return List of modules
     */
    @GetMapping("/modules")
    public ResponseEntity<ApiResponse<List<ModuleDTO>>> getAllModules(
            @RequestParam(required = false) Module.Difficulty difficulty,
            @RequestParam(required = false) Integer userLevel) {

        log.info("GET /api/v1/learning/modules - difficulty: {}, userLevel: {}", difficulty, userLevel);

        List<ModuleDTO> modules;

        if (difficulty != null) {
            modules = moduleService.getModulesByDifficulty(difficulty);
        } else if (userLevel != null) {
            modules = moduleService.getModulesForUserLevel(userLevel);
        } else {
            modules = moduleService.getAllModules();
        }

        return ResponseEntity.ok(ApiResponse.success(modules));
    }

    /**
     * Get a single module by ID with full details including lessons.
     * Public endpoint - no authentication required.
     *
     * @param id Module ID
     * @return Module details
     */
    @GetMapping("/modules/{id}")
    public ResponseEntity<ApiResponse<ModuleDetailDTO>> getModuleById(@PathVariable UUID id) {
        log.info("GET /api/v1/learning/modules/{}", id);

        ModuleDetailDTO module = moduleService.getModuleById(id);
        return ResponseEntity.ok(ApiResponse.success(module));
    }

    /**
     * Get recommended next module for the authenticated user.
     * Requires authentication.
     *
     * @return Recommended module or null if all completed
     */
    @GetMapping("/modules/recommended")
    public ResponseEntity<ApiResponse<ModuleDTO>> getRecommendedModule() {
        String keycloakId = AuthenticationUtil.getCurrentKeycloakUserId()
                .orElseThrow(() -> new com.syntaxllama.gitgud.backend.exception.UnauthorizedException("User not authenticated"));

        User currentUser = userSyncService.getUserByKeycloakId(keycloakId);
        log.info("GET /api/v1/learning/modules/recommended - user: {}", currentUser.getId());

        ModuleDTO recommendedModule = moduleService.getRecommendedNextModule(currentUser);

        if (recommendedModule == null) {
            return ResponseEntity.ok(ApiResponse.success("All modules completed!", null));
        }

        return ResponseEntity.ok(ApiResponse.success(recommendedModule));
    }

    /**
     * Get all lessons in a module.
     * Public endpoint - no authentication required.
     *
     * @param moduleId Module ID
     * @return List of lessons in module
     */
    @GetMapping("/modules/{moduleId}/lessons")
    public ResponseEntity<ApiResponse<List<LessonDTO>>> getLessonsByModule(@PathVariable UUID moduleId) {
        log.info("GET /api/v1/learning/modules/{}/lessons", moduleId);

        List<LessonDTO> lessons = lessonService.getLessonsByModule(moduleId);
        return ResponseEntity.ok(ApiResponse.success(lessons));
    }

    /**
     * Get a single lesson by ID with full content.
     * Public endpoint - no authentication required.
     *
     * @param id Lesson ID
     * @return Lesson details with content and starter code
     */
    @GetMapping("/lessons/{id}")
    public ResponseEntity<ApiResponse<LessonDTO>> getLessonById(@PathVariable UUID id) {
        log.info("GET /api/v1/learning/lessons/{}", id);

        LessonDTO lesson = lessonService.getLessonById(id);
        return ResponseEntity.ok(ApiResponse.success(lesson));
    }

    // TODO: Implement remaining endpoints:
    // GET /api/v1/learning/progress - Get user's overall progress
    // GET /api/v1/learning/progress/{lessonId} - Get progress for specific lesson
    // POST /api/v1/learning/progress - Update user progress
}
