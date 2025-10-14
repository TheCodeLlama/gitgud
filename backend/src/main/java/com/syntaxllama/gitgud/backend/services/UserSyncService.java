package com.syntaxllama.gitgud.backend.services;

import com.syntaxllama.gitgud.backend.models.User;
import com.syntaxllama.gitgud.backend.models.UserProfile;
import com.syntaxllama.gitgud.backend.models.UserStats;
import com.syntaxllama.gitgud.backend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Service for synchronizing Keycloak users with the local User entity.
 *
 * When a user logs in via Keycloak, we create a local User record if one doesn't exist,
 * or update the existing record with the latest information from Keycloak.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserSyncService {

    private final UserRepository userRepository;

    /**
     * Sync a Keycloak user to the local database.
     * Creates a new User if one doesn't exist, or updates the existing one.
     *
     * @param keycloakId The Keycloak user ID (from JWT "sub" claim)
     * @param email The user's email address
     * @param username The user's username (from JWT "preferred_username" claim)
     * @return The synced User entity
     */
    @Transactional
    public User syncUser(String keycloakId, String email, String username) {
        log.debug("Syncing user: keycloakId={}, email={}, username={}", keycloakId, email, username);

        return userRepository.findByKeycloakId(keycloakId)
                .map(existingUser -> updateExistingUser(existingUser, email, username))
                .orElseGet(() -> createNewUser(keycloakId, email, username));
    }

    /**
     * Update an existing user's information.
     * Updates email and username in case they were changed in Keycloak.
     */
    private User updateExistingUser(User user, String email, String username) {
        log.debug("Updating existing user: id={}, keycloakId={}", user.getId(), user.getKeycloakId());

        boolean updated = false;

        // Update email if changed
        if (!user.getEmail().equals(email)) {
            log.info("Updating email for user {}: {} -> {}", user.getId(), user.getEmail(), email);
            user.setEmail(email);
            updated = true;
        }

        // Update username if changed
        if (!user.getUsername().equals(username)) {
            log.info("Updating username for user {}: {} -> {}", user.getId(), user.getUsername(), username);
            user.setUsername(username);
            updated = true;
        }

        if (updated) {
            user = userRepository.save(user);
            log.debug("User updated: id={}", user.getId());
        }

        return user;
    }

    /**
     * Create a new user record from Keycloak information.
     * Also initializes UserStats and UserProfile with default values.
     */
    private User createNewUser(String keycloakId, String email, String username) {
        log.info("Creating new user: keycloakId={}, email={}, username={}", keycloakId, email, username);

        // Create user
        User user = new User();
        user.setKeycloakId(keycloakId);
        user.setEmail(email);
        user.setUsername(username);

        // Initialize UserStats with default values
        UserStats stats = new UserStats();
        stats.setUser(user);
        stats.setTotalXp(Integer.toUnsignedLong(0));
        stats.setCurrentLevel(1);
        stats.setXpToNextLevel(Integer.toUnsignedLong(100)); // First level requires 100 XP
        stats.setCurrentStreakDays(0);
        stats.setLongestStreakDays(0);
        stats.setLastActivityDate(LocalDate.from(LocalDateTime.now()));
        user.setStats(stats);

        // Initialize UserProfile with default values
        UserProfile profile = new UserProfile();
        profile.setUser(user);
        profile.setDisplayName(username); // Default to username
        profile.setAvatarUrl(null); // No avatar by default
        profile.setBio(null); // No bio by default
        user.setProfile(profile);

        // Save user (cascades to stats and profile)
        user = userRepository.save(user);

        log.info("New user created: id={}, keycloakId={}", user.getId(), user.getKeycloakId());

        return user;
    }

    /**
     * Get a user by their Keycloak ID.
     *
     * @param keycloakId The Keycloak user ID
     * @return The User entity, or empty if not found
     */
    public User getUserByKeycloakId(String keycloakId) {
        return userRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new RuntimeException("User not found with keycloakId: " + keycloakId));
    }
}
