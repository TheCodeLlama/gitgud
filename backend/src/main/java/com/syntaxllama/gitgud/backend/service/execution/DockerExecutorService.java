package com.syntaxllama.gitgud.backend.service.execution;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.api.exception.DockerException;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;

/**
 * Service for executing code in hardened Docker containers.
 * Implements security best practices from DockerCodeExecutionResearch.md
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DockerExecutorService {

    private final DockerClient dockerClient;
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    @Value("${code.execution.timeout.seconds:5}")
    private int timeoutSeconds;

    @Value("${code.execution.max.memory.mb:256}")
    private int maxMemoryMb;

    @Value("${code.execution.docker.image:openjdk:25-slim}")
    private String dockerImage;

    /**
     * Execute Java code in a secure Docker container.
     * Returns execution output and errors.
     */
    public ExecutionOutput executeJavaCode(String sourceCode, String input) throws ExecutionException {
        String containerId = null;
        long startTime = System.currentTimeMillis();

        try {
            // Create secure container
            containerId = createSecureContainer();
            log.debug("Created container: {}", containerId);

            // Start container
            dockerClient.startContainerCmd(containerId).exec();
            log.debug("Started container: {}", containerId);

            // Write source code to container
            writeSourceCode(containerId, sourceCode);

            // Compile the code
            CompilationResult compilationResult = compileCode(containerId);
            if (!compilationResult.isSuccess()) {
                return ExecutionOutput.builder()
                        .success(false)
                        .compilationError(true)
                        .output(compilationResult.getOutput())
                        .error(compilationResult.getError())
                        .executionTimeMs(System.currentTimeMillis() - startTime)
                        .build();
            }

            // Execute the code with timeout
            ExecutionOutput output = runCode(containerId, input, timeoutSeconds);
            output.setExecutionTimeMs(System.currentTimeMillis() - startTime);

            return output;

        } catch (TimeoutException e) {
            log.warn("Execution timed out after {} seconds", timeoutSeconds);
            return ExecutionOutput.builder()
                    .success(false)
                    .timeout(true)
                    .error("Execution timed out after " + timeoutSeconds + " seconds")
                    .executionTimeMs(System.currentTimeMillis() - startTime)
                    .build();

        } catch (Exception e) {
            log.error("Error executing code in Docker", e);
            return ExecutionOutput.builder()
                    .success(false)
                    .error("System error: " + e.getMessage())
                    .executionTimeMs(System.currentTimeMillis() - startTime)
                    .build();

        } finally {
            // Always cleanup container
            if (containerId != null) {
                cleanupContainer(containerId);
            }
        }
    }

    /**
     * Create a hardened Docker container with security best practices.
     * Implements recommendations from DockerCodeExecutionResearch.md:
     * - No network access (--network none)
     * - Read-only filesystem (--read-only)
     * - Drop all capabilities (--cap-drop ALL)
     * - Memory limits (256MB)
     * - CPU limits (0.5 CPU)
     * - PID limits (50)
     * - Non-root user (UID 1000)
     * - Tmpfs for /tmp (100MB, noexec, nosuid)
     */
    private String createSecureContainer() {
        HostConfig hostConfig = HostConfig.newHostConfig()
                // Network isolation
                .withNetworkMode("none")
                // Memory limits (no swap)
                .withMemory((long) maxMemoryMb * 1024 * 1024)
                .withMemorySwap((long) maxMemoryMb * 1024 * 1024)
                // CPU limit (0.5 CPU)
                .withNanoCPUs(500_000_000L) // 0.5 CPU = 500,000,000 nanocpus
                // Process limit to prevent fork bombs
                .withPidsLimit(50L)
                // Drop all Linux capabilities
                .withCapDrop(Capability.ALL)
                // Read-only root filesystem
                .withReadonlyRootfs(true)
                // Tmpfs for temporary files (100MB, noexec, nosuid)
                .withTmpFs(java.util.Map.of(
                        "/tmp", "rw,noexec,nosuid,size=100m",
                        "/home/coderunner", "rw,noexec,nosuid,size=50m"
                ));

        CreateContainerResponse container = dockerClient.createContainerCmd(dockerImage)
                .withHostConfig(hostConfig)
                // Run as non-root user (UID 1000)
                .withUser("1000:1000")
                .withWorkingDir("/home/coderunner")
                // Security options
                .withCmd("sleep", "3600") // Keep container alive
                .exec();

        return container.getId();
    }

    /**
     * Write source code to container.
     */
    private void writeSourceCode(String containerId, String sourceCode) throws InterruptedException, ExecutionException, TimeoutException {
        // Create Main.java file with the source code
        String command = String.format("sh -c 'cat > Main.java << EOF\n%s\nEOF'", sourceCode);
        executeCommand(containerId, command, 5);
    }

    /**
     * Compile Java code inside container.
     */
    private CompilationResult compileCode(String containerId) throws InterruptedException, ExecutionException, TimeoutException {
        log.debug("Compiling code in container {}", containerId);

        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        ByteArrayOutputStream stderr = new ByteArrayOutputStream();

        ExecCreateCmdResponse execCreateCmd = dockerClient.execCreateCmd(containerId)
                .withCmd("javac", "Main.java")
                .withAttachStdout(true)
                .withAttachStderr(true)
                .exec();

        Future<Void> future = executorService.submit(() -> {
            try {
                dockerClient.execStartCmd(execCreateCmd.getId())
                        .exec(new ExecStartResultCallback(stdout, stderr))
                        .awaitCompletion();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
            return null;
        });

        future.get(10, TimeUnit.SECONDS);

        String stdoutStr = stdout.toString(StandardCharsets.UTF_8);
        String stderrStr = stderr.toString(StandardCharsets.UTF_8);

        boolean success = stderrStr.isEmpty() || !stderrStr.contains("error:");

        return CompilationResult.builder()
                .success(success)
                .output(stdoutStr)
                .error(stderrStr)
                .build();
    }

    /**
     * Run compiled Java code with input.
     */
    private ExecutionOutput runCode(String containerId, String input, int timeoutSeconds)
            throws InterruptedException, ExecutionException, TimeoutException {
        log.debug("Running code in container {} with timeout {} seconds", containerId, timeoutSeconds);

        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        ByteArrayOutputStream stderr = new ByteArrayOutputStream();

        // Create execution command
        ExecCreateCmdResponse execCreateCmd = dockerClient.execCreateCmd(containerId)
                .withCmd("java", "Main")
                .withAttachStdout(true)
                .withAttachStderr(true)
                .withAttachStdin(input != null && !input.isEmpty())
                .exec();

        // Execute with timeout
        Future<Integer> future = executorService.submit(() -> {
            try {
                ExecStartResultCallback callback = new ExecStartResultCallback(stdout, stderr);

                if (input != null && !input.isEmpty()) {
                    // TODO: Handle stdin input
                }

                dockerClient.execStartCmd(execCreateCmd.getId())
                        .exec(callback)
                        .awaitCompletion();

                // Get exit code
                return dockerClient.inspectExecCmd(execCreateCmd.getId())
                        .exec()
                        .getExitCodeLong()
                        .intValue();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        });

        try {
            Integer exitCode = future.get(timeoutSeconds, TimeUnit.SECONDS);

            String stdoutStr = stdout.toString(StandardCharsets.UTF_8);
            String stderrStr = stderr.toString(StandardCharsets.UTF_8);

            return ExecutionOutput.builder()
                    .success(exitCode == 0)
                    .output(stdoutStr)
                    .error(stderrStr)
                    .exitCode(exitCode)
                    .timeout(false)
                    .compilationError(false)
                    .build();

        } catch (TimeoutException e) {
            future.cancel(true);
            // Kill the container to stop execution
            dockerClient.killContainerCmd(containerId).exec();
            throw e;
        }
    }

    /**
     * Execute a command in container with timeout.
     */
    private void executeCommand(String containerId, String command, int timeoutSeconds)
            throws InterruptedException, ExecutionException, TimeoutException {

        ExecCreateCmdResponse execCreateCmd = dockerClient.execCreateCmd(containerId)
                .withCmd("sh", "-c", command)
                .withAttachStdout(true)
                .withAttachStderr(true)
                .exec();

        Future<Void> future = executorService.submit(() -> {
            try {
                dockerClient.execStartCmd(execCreateCmd.getId())
                        .exec(new ExecStartResultCallback())
                        .awaitCompletion();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
            return null;
        });

        future.get(timeoutSeconds, TimeUnit.SECONDS);
    }

    /**
     * Cleanup and remove container.
     */
    private void cleanupContainer(String containerId) {
        try {
            dockerClient.removeContainerCmd(containerId)
                    .withForce(true)
                    .exec();
            log.debug("Removed container: {}", containerId);
        } catch (DockerException e) {
            log.warn("Failed to remove container {}: {}", containerId, e.getMessage());
        }
    }

    /**
     * Result of code compilation.
     */
    @lombok.Data
    @lombok.Builder
    public static class CompilationResult {
        private boolean success;
        private String output;
        private String error;
    }

    /**
     * Result of code execution.
     */
    @lombok.Data
    @lombok.Builder
    public static class ExecutionOutput {
        private boolean success;
        private String output;
        private String error;
        private Integer exitCode;
        private boolean timeout;
        private boolean compilationError;
        private Long executionTimeMs;
    }
}
