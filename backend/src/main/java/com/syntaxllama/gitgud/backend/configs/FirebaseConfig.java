package com.syntaxllama.gitgud.backend.configs;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

/**
 * Firebase Admin SDK Configuration.
 *
 * Initializes Firebase Admin SDK on application startup using service account credentials.
 * Supports two credential modes:
 * 1. File path: Load from classpath (e.g., firebase/service-account.json)
 * 2. Base64: Load from environment variable (recommended for production)
 */
@Configuration
@Slf4j
public class FirebaseConfig {

    @Value("${firebase.service-account.path:}")
    private String serviceAccountPath;

    @Value("${firebase.service-account.base64:}")
    private String serviceAccountBase64;

    @Value("${firebase.project-id}")
    private String projectId;

    /**
     * Initialize Firebase Admin SDK on application startup.
     * This method runs after the bean is constructed and all @Value fields are injected.
     */
    @PostConstruct
    public void initialize() {
        try {
            // Check if Firebase is already initialized
            if (!FirebaseApp.getApps().isEmpty()) {
                log.info("Firebase Admin SDK already initialized");
                return;
            }

            GoogleCredentials credentials = loadCredentials();

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(credentials)
                    .setProjectId(projectId)
                    .build();

            FirebaseApp.initializeApp(options);

            log.info("Firebase Admin SDK initialized successfully for project: {}", projectId);

        } catch (IOException e) {
            log.error("Failed to initialize Firebase Admin SDK", e);
            throw new IllegalStateException("Firebase Admin SDK initialization failed", e);
        }
    }

    /**
     * Load Google credentials from either file path or base64-encoded string.
     * Priority: base64 > file path
     */
    private GoogleCredentials loadCredentials() throws IOException {
        // Option 1: Load from base64-encoded environment variable (recommended for production)
        if (serviceAccountBase64 != null && !serviceAccountBase64.isBlank()) {
            log.info("Loading Firebase credentials from base64 environment variable");
            byte[] decodedBytes = Base64.getDecoder().decode(serviceAccountBase64);
            try (InputStream serviceAccountStream = new ByteArrayInputStream(decodedBytes)) {
                return GoogleCredentials.fromStream(serviceAccountStream);
            }
        }

        // Option 2: Load from file path (classpath resource)
        if (serviceAccountPath != null && !serviceAccountPath.isBlank()) {
            log.info("Loading Firebase credentials from file: {}", serviceAccountPath);
            ClassPathResource resource = new ClassPathResource(serviceAccountPath);
            if (!resource.exists()) {
                throw new IllegalStateException("Firebase service account file not found: " + serviceAccountPath);
            }
            try (InputStream serviceAccountStream = resource.getInputStream()) {
                return GoogleCredentials.fromStream(serviceAccountStream);
            }
        }

        // No credentials found
        throw new IllegalStateException(
                "Firebase credentials not configured. Set either:\n" +
                "  - firebase.service-account.base64 (recommended)\n" +
                "  - firebase.service-account.path"
        );
    }
}
