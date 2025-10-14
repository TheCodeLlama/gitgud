package com.syntaxllama.gitgud.backend.services;

import com.syntaxllama.gitgud.backend.dtos.auth.RegisterRequest;
import com.syntaxllama.gitgud.backend.exceptions.BadRequestException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.ws.rs.core.Response;

/**
 * Service for managing users in Keycloak using the Admin API.
 */
@Service
@Slf4j
public class KeycloakAdminService {

    @Value("${keycloak.admin.server-url}")
    private String serverUrl;

    @Value("${keycloak.admin.realm}")
    private String realm;

    @Value("${keycloak.admin.client-id}")
    private String clientId;

    @Value("${keycloak.admin.username}")
    private String adminUsername;

    @Value("${keycloak.admin.password}")
    private String adminPassword;

    private Keycloak keycloak;

    /**
     * Initialize Keycloak admin client after properties are injected.
     */
    @PostConstruct
    public void init() {
        log.info("Initializing Keycloak admin client for realm: {}", realm);
        this.keycloak = KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm("master") // Admin credentials are in master realm
                .clientId("admin-cli")
                .username(adminUsername)
                .password(adminPassword)
                .build();
    }

    /**
     * Create a new user in Keycloak.
     *
     * @param request The registration request containing user details
     * @return The Keycloak user ID
     * @throws BadRequestException if user creation fails
     */
    public String createUser(RegisterRequest request) {
        log.info("Creating user in Keycloak: username={}, email={}", request.getUsername(), request.getEmail());

        try {
            RealmResource realmResource = keycloak.realm(realm);
            UsersResource usersResource = realmResource.users();

            // Check if username already exists
            if (!usersResource.search(request.getUsername()).isEmpty()) {
                throw new BadRequestException("Username already exists");
            }

            // Check if email already exists
            if (!usersResource.searchByEmail(request.getEmail(), true).isEmpty()) {
                throw new BadRequestException("Email already exists");
            }

            // Create user representation
            UserRepresentation user = new UserRepresentation();
            user.setEnabled(true);
            user.setUsername(request.getUsername());
            user.setEmail(request.getEmail());
            user.setFirstName(request.getFirstName());
            user.setLastName(request.getLastName());
            user.setEmailVerified(false); // User should verify email (implement later)

            // Create user
            Response response = usersResource.create(user);

            if (response.getStatus() != 201) {
                String errorMessage = response.readEntity(String.class);
                log.error("Failed to create user in Keycloak. Status: {}, Error: {}",
                        response.getStatus(), errorMessage);
                throw new BadRequestException("Failed to create user: " + errorMessage);
            }

            // Extract user ID from location header
            String locationHeader = response.getLocation().getPath();
            String userId = locationHeader.substring(locationHeader.lastIndexOf('/') + 1);

            log.info("User created successfully in Keycloak: userId={}", userId);

            // Set user password
            setUserPassword(userId, request.getPassword());

            response.close();

            return userId;

        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error creating user in Keycloak", e);
            throw new BadRequestException("Failed to create user: " + e.getMessage());
        }
    }

    /**
     * Set password for a Keycloak user.
     *
     * @param userId   The Keycloak user ID
     * @param password The password to set
     */
    private void setUserPassword(String userId, String password) {
        log.debug("Setting password for user: {}", userId);

        try {
            RealmResource realmResource = keycloak.realm(realm);
            UsersResource usersResource = realmResource.users();

            // Create password credential
            CredentialRepresentation credential = new CredentialRepresentation();
            credential.setType(CredentialRepresentation.PASSWORD);
            credential.setValue(password);
            credential.setTemporary(false); // Password is not temporary

            // Set password
            usersResource.get(userId).resetPassword(credential);

            log.debug("Password set successfully for user: {}", userId);

        } catch (Exception e) {
            log.error("Error setting password for user: {}", userId, e);
            throw new BadRequestException("Failed to set user password: " + e.getMessage());
        }
    }

    /**
     * Get user by username from Keycloak.
     *
     * @param username The username to search for
     * @return The UserRepresentation if found, null otherwise
     */
    public UserRepresentation getUserByUsername(String username) {
        log.debug("Getting user by username: {}", username);

        try {
            RealmResource realmResource = keycloak.realm(realm);
            UsersResource usersResource = realmResource.users();

            var users = usersResource.search(username, true);
            if (users.isEmpty()) {
                return null;
            }

            return users.get(0);

        } catch (Exception e) {
            log.error("Error getting user by username: {}", username, e);
            return null;
        }
    }

    /**
     * Delete user from Keycloak (for testing/admin purposes).
     *
     * @param userId The Keycloak user ID to delete
     */
    public void deleteUser(String userId) {
        log.info("Deleting user from Keycloak: userId={}", userId);

        try {
            RealmResource realmResource = keycloak.realm(realm);
            UsersResource usersResource = realmResource.users();

            usersResource.delete(userId);

            log.info("User deleted successfully from Keycloak: userId={}", userId);

        } catch (Exception e) {
            log.error("Error deleting user from Keycloak: {}", userId, e);
            throw new BadRequestException("Failed to delete user: " + e.getMessage());
        }
    }
}
