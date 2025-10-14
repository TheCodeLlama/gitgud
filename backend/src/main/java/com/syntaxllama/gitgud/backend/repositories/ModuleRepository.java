package com.syntaxllama.gitgud.backend.repositories;

import com.syntaxllama.gitgud.backend.models.Module;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository interface for Module entity operations.
 */
@Repository
public interface ModuleRepository extends JpaRepository<Module, UUID> {

    /**
     * Find all published modules ordered by display order.
     */
    List<Module> findByIsPublishedTrueOrderByDisplayOrderAsc();

    /**
     * Find modules by difficulty.
     */
    List<Module> findByDifficultyOrderByDisplayOrderAsc(Module.Difficulty difficulty);

    /**
     * Find modules by required level less than or equal to specified level.
     */
    List<Module> findByRequiredLevelLessThanEqualAndIsPublishedTrueOrderByDisplayOrderAsc(Integer level);
}
