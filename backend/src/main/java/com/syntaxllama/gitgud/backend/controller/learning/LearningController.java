package com.syntaxllama.gitgud.backend.controller.learning;

import com.syntaxllama.gitgud.backend.controller.BaseController;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for learning-related operations.
 * Handles modules, lessons, and user progress tracking.
 */
@RestController
@RequestMapping("/learning")
@RequiredArgsConstructor
@Slf4j
public class LearningController extends BaseController {

    // TODO: Implement endpoints:
    // GET /api/v1/learning/modules - List all modules
    // GET /api/v1/learning/modules/{id} - Get module by ID
    // GET /api/v1/learning/modules/{moduleId}/lessons - List lessons in module
    // GET /api/v1/learning/lessons/{id} - Get lesson by ID
    // GET /api/v1/learning/progress - Get user's overall progress
    // GET /api/v1/learning/progress/{lessonId} - Get progress for specific lesson
    // POST /api/v1/learning/progress - Update user progress
}
