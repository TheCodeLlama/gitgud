package com.syntaxllama.gitgud.backend.controller.learning;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for learning-related operations.
 * Handles modules, lessons, and user progress tracking.
 */
@RestController
@RequestMapping("/api/learning")
@RequiredArgsConstructor
@Slf4j
public class LearningController {

    // TODO: Implement endpoints:
    // GET /api/learning/modules - List all modules
    // GET /api/learning/modules/{id} - Get module by ID
    // GET /api/learning/modules/{moduleId}/lessons - List lessons in module
    // GET /api/learning/lessons/{id} - Get lesson by ID
    // GET /api/learning/progress - Get user's overall progress
    // GET /api/learning/progress/{lessonId} - Get progress for specific lesson
    // POST /api/learning/progress - Update user progress
}
