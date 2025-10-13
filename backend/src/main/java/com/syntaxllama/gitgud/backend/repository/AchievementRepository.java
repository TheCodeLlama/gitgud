package com.syntaxllama.gitgud.backend.repository;

import com.syntaxllama.gitgud.backend.model.Achievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Achievement entity operations.
 */
@Repository
public interface AchievementRepository extends JpaRepository<Achievement, UUID> {

    /**
     * Find achievement by name.
     */
    Optional<Achievement> findByName(String name);

    /**
     * Find achievements by rarity.
     */
    List<Achievement> findByRarity(Achievement.Rarity rarity);

    /**
     * Check if achievement exists by name.
     */
    boolean existsByName(String name);
}
