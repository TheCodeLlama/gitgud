package com.syntaxllama.gitgud.backend.config;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Docker client configuration for code execution.
 * Connects to the local Docker daemon to create and manage execution containers.
 */
@Configuration
@Slf4j
public class DockerConfig {

    @Value("${code.execution.docker.host:unix:///var/run/docker.sock}")
    private String dockerHost;

    @Value("${code.execution.docker.timeout:30}")
    private int dockerTimeoutSeconds;

    /**
     * Docker client configuration.
     * Uses local Docker daemon via Unix socket by default.
     */
    @Bean
    public DockerClientConfig dockerClientConfig() {
        return DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost(dockerHost)
                .build();
    }

    /**
     * HTTP client for Docker API communication.
     */
    @Bean
    public DockerHttpClient dockerHttpClient(DockerClientConfig config) {
        return new ApacheDockerHttpClient.Builder()
                .dockerHost(config.getDockerHost())
                .maxConnections(100)
                .connectionTimeout(Duration.ofSeconds(dockerTimeoutSeconds))
                .responseTimeout(Duration.ofSeconds(dockerTimeoutSeconds))
                .build();
    }

    /**
     * Docker client for creating and managing containers.
     * Connection is optional - if Docker is not available, the bean will still be created
     * but operations will fail at runtime.
     */
    @Bean
    public DockerClient dockerClient(DockerClientConfig config, DockerHttpClient httpClient) {
        DockerClient client = DockerClientImpl.getInstance(config, httpClient);

        log.info("Docker client initialized with host: {}", dockerHost);

        // Verify connection by pinging Docker daemon
        try {
            client.pingCmd().exec();
            log.info("Successfully connected to Docker daemon");
        } catch (Exception e) {
            log.warn("Failed to connect to Docker daemon at {}. Code execution will not work until Docker is available.", dockerHost);
            log.warn("Error: {}", e.getMessage());
            // Don't throw - allow app to start without Docker
        }

        return client;
    }
}
