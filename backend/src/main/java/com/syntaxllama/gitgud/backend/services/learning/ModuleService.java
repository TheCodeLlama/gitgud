package com.syntaxllama.gitgud.backend.services.learning;

import com.syntaxllama.gitgud.backend.dtos.learning.ModuleDTO;
import com.syntaxllama.gitgud.backend.dtos.learning.ModuleDetailDTO;
import com.syntaxllama.gitgud.backend.exceptions.ResourceNotFoundException;
import com.syntaxllama.gitgud.backend.models.Module;
import com.syntaxllama.gitgud.backend.models.User;
import com.syntaxllama.gitgud.backend.repositories.ModuleRepository;
import com.syntaxllama.gitgud.backend.repositories.UserProgressRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing learning modules.
 * Handles module retrieval, ordering, and recommendations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ModuleService {

    private final ModuleRepository moduleRepository;
    private final UserProgressRepository userProgressRepository;

    /**
     * Get all published modules, ordered by display order.
     *
     * @return List of module DTOs
     */
    @Transactional(readOnly = true)
    public List<ModuleDTO> getAllModules() {
        log.debug("Fetching all published modules");
        List<Module> modules = moduleRepository.findByIsPublishedTrueOrderByDisplayOrderAsc();

        return modules.stream()
                .map(ModuleDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get modules available for a user based on their level.
     *
     * @param userLevel The user's current level
     * @return List of modules the user can access
     */
    @Transactional(readOnly = true)
    public List<ModuleDTO> getModulesForUserLevel(Integer userLevel) {
        log.debug("Fetching modules for user level: {}", userLevel);
        List<Module> modules = moduleRepository
                .findByRequiredLevelLessThanEqualAndIsPublishedTrueOrderByDisplayOrderAsc(userLevel);

        return modules.stream()
                .map(ModuleDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get modules by difficulty level.
     *
     * @param difficulty The difficulty level
     * @return List of modules with specified difficulty
     */
    @Transactional(readOnly = true)
    public List<ModuleDTO> getModulesByDifficulty(Module.Difficulty difficulty) {
        log.debug("Fetching modules with difficulty: {}", difficulty);
        List<Module> modules = moduleRepository.findByDifficultyOrderByDisplayOrderAsc(difficulty);

        return modules.stream()
                .map(ModuleDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get a single module by ID with full details including lessons.
     *
     * @param moduleId The module ID
     * @return Detailed module DTO
     * @throws ResourceNotFoundException if module not found
     */
    @Transactional(readOnly = true)
    public ModuleDetailDTO getModuleById(UUID moduleId) {
        log.debug("Fetching module with ID: {}", moduleId);
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Module not found with id: " + moduleId));

        return ModuleDetailDTO.fromEntity(module);
    }

    /**
     * Get recommended next module for a user.
     * Logic: Find the first module where the user hasn't completed all lessons,
     * prioritizing by display order.
     *
     * @param user The current user
     * @return Recommended module DTO, or null if all modules completed
     */
    @Transactional(readOnly = true)
    public ModuleDTO getRecommendedNextModule(User user) {
        log.debug("Finding recommended next module for user: {}", user.getId());

        // Get all modules available for user's level
        List<Module> availableModules = moduleRepository
                .findByRequiredLevelLessThanEqualAndIsPublishedTrueOrderByDisplayOrderAsc(
                        user.getStats().getCurrentLevel()
                );

        // Find first module with incomplete lessons
        for (Module module : availableModules) {
            long totalLessons = module.getLessons().stream()
                    .filter(lesson -> lesson.getIsPublished())
                    .count();

            long completedLessons = userProgressRepository
                    .countCompletedLessonsByUserAndModule(user.getId(), module.getId());

            if (completedLessons < totalLessons) {
                log.debug("Recommended module for user {}: {} (completed {}/{} lessons)",
                        user.getId(), module.getTitle(), completedLessons, totalLessons);
                return ModuleDTO.fromEntity(module);
            }
        }

        log.debug("No recommended module found - user {} has completed all available modules", user.getId());
        return null;
    }
}
