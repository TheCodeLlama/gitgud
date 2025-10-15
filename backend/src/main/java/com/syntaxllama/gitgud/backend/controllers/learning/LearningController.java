package com.syntaxllama.gitgud.backend.controllers.learning;

import com.syntaxllama.gitgud.backend.dtos.ApiResponse;
import com.syntaxllama.gitgud.backend.dtos.learning.LessonDTO;
import com.syntaxllama.gitgud.backend.dtos.learning.ModuleDTO;
import com.syntaxllama.gitgud.backend.dtos.learning.ModuleDetailDTO;
import com.syntaxllama.gitgud.backend.dtos.learning.OverallProgressDTO;
import com.syntaxllama.gitgud.backend.dtos.learning.TestCaseDTO;
import com.syntaxllama.gitgud.backend.dtos.learning.UpdateProgressRequest;
import com.syntaxllama.gitgud.backend.dtos.learning.UserProgressDTO;
import com.syntaxllama.gitgud.backend.models.Module;
import com.syntaxllama.gitgud.backend.models.User;
import com.syntaxllama.gitgud.backend.security.AuthenticationUtil;
import com.syntaxllama.gitgud.backend.services.learning.LessonService;
import com.syntaxllama.gitgud.backend.services.learning.ModuleService;
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
@RequestMapping("/api/v1/learning")
@RequiredArgsConstructor
@Slf4j
public class LearningController {

    private final ModuleService moduleService;
    private final LessonService lessonService;
    private final com.syntaxllama.gitgud.backend.services.learning.ProgressService progressService;
    private final com.syntaxllama.gitgud.backend.services.UserSyncService userSyncService;

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
        String firebaseUid = AuthenticationUtil.getCurrentFirebaseUid()
                .orElseThrow(() -> new com.syntaxllama.gitgud.backend.exceptions.UnauthorizedException("User not authenticated"));

        User currentUser = userSyncService.getUserByFirebaseUid(firebaseUid);
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

    /**
     * Get visible test cases for a lesson.
     * Public endpoint - no authentication required.
     * Returns only non-hidden test cases without expected output for security.
     *
     * @param lessonId Lesson ID
     * @return List of visible test cases (expected output hidden)
     */
    @GetMapping("/lessons/{lessonId}/testcases")
    public ResponseEntity<ApiResponse<List<TestCaseDTO>>> getTestCasesForLesson(@PathVariable UUID lessonId) {
        log.info("GET /api/v1/learning/lessons/{}/testcases", lessonId);

        List<TestCaseDTO> testCases = lessonService.getVisibleTestCasesForLesson(lessonId);
        return ResponseEntity.ok(ApiResponse.success(testCases));
    }

    /**
     * Update user progress for a lesson.
     * Requires authentication.
     *
     * @param request Progress update request
     * @return Updated progress
     */
    @PostMapping("/progress")
    public ResponseEntity<ApiResponse<UserProgressDTO>> updateProgress(@RequestBody UpdateProgressRequest request) {
        String firebaseUid = AuthenticationUtil.getCurrentFirebaseUid()
                .orElseThrow(() -> new com.syntaxllama.gitgud.backend.exceptions.UnauthorizedException("User not authenticated"));

        User currentUser = userSyncService.getUserByFirebaseUid(firebaseUid);
        log.info("POST /api/v1/learning/progress - user: {}, lesson: {}", currentUser.getId(), request.getLessonId());

        UserProgressDTO progress = progressService.updateProgress(currentUser, request);
        return ResponseEntity.ok(ApiResponse.success(progress));
    }

    /**
     * Get user's overall progress summary.
     * Requires authentication.
     *
     * @return Overall progress with statistics
     */
    @GetMapping("/progress")
    public ResponseEntity<ApiResponse<OverallProgressDTO>> getOverallProgress() {
        String firebaseUid = AuthenticationUtil.getCurrentFirebaseUid()
                .orElseThrow(() -> new com.syntaxllama.gitgud.backend.exceptions.UnauthorizedException("User not authenticated"));

        User currentUser = userSyncService.getUserByFirebaseUid(firebaseUid);
        log.info("GET /api/v1/learning/progress - user: {}", currentUser.getId());

        OverallProgressDTO progress = progressService.getOverallProgress(currentUser);
        return ResponseEntity.ok(ApiResponse.success(progress));
    }

    /**
     * Get progress for a specific lesson.
     * Requires authentication.
     *
     * @param lessonId Lesson ID
     * @return User's progress for this lesson, or null if no progress exists
     */
    @GetMapping("/progress/{lessonId}")
    public ResponseEntity<ApiResponse<UserProgressDTO>> getProgressForLesson(@PathVariable UUID lessonId) {
        String firebaseUid = AuthenticationUtil.getCurrentFirebaseUid()
                .orElseThrow(() -> new com.syntaxllama.gitgud.backend.exceptions.UnauthorizedException("User not authenticated"));

        User currentUser = userSyncService.getUserByFirebaseUid(firebaseUid);
        log.info("GET /api/v1/learning/progress/{} - user: {}", lessonId, currentUser.getId());

        UserProgressDTO progress = progressService.getProgressForLesson(currentUser, lessonId);
        return ResponseEntity.ok(ApiResponse.success(progress));
    }

    /**
     * Get the lesson where user should continue learning.
     * Requires authentication.
     *
     * @return Lesson ID to continue, or null if no lesson found
     */
    @GetMapping("/continue")
    public ResponseEntity<ApiResponse<UUID>> getContinueLesson() {
        String firebaseUid = AuthenticationUtil.getCurrentFirebaseUid()
                .orElseThrow(() -> new com.syntaxllama.gitgud.backend.exceptions.UnauthorizedException("User not authenticated"));

        User currentUser = userSyncService.getUserByFirebaseUid(firebaseUid);
        log.info("GET /api/v1/learning/continue - user: {}", currentUser.getId());

        UUID lessonId = progressService.getContinueLesson(currentUser);
        return ResponseEntity.ok(ApiResponse.success(lessonId));
    }
}
