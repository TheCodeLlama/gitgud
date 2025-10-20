package com.syntaxllama.gitgud.backend.services.execution;

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
import java.util.Base64;
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

    @Value("${code.execution.timeout.seconds:300}")
    private int timeoutSeconds;

    @Value("${code.execution.max.memory.mb:256}")
    private int maxMemoryMb;

    @Value("${code.execution.docker.image:gitgud-java-executor:latest}")
    private String dockerImage;

    // Maximum output size to prevent memory exhaustion (10KB)
    private static final int MAX_OUTPUT_SIZE = 10 * 1024;

    /**
     * Execute Java code in a secure Docker container (single-file legacy method).
     * @deprecated Use executeCode() with file map for multi-file support.
     */
    @Deprecated
    public ExecutionOutput executeJavaCode(String sourceCode, String input) throws ExecutionException {
        // Backwards compatibility: wrap single file in a map
        return executeCode(
                java.util.Map.of("Main.java", sourceCode),
                input,
                dockerImage,
                null, // Will default to "javac Main.java"
                null, // Will default to "java Main"
                "/tmp"
        );
    }

    /**
     * Execute code in a secure Docker container with multi-file support.
     *
     * @param files Map of file paths to file contents
     * @param input Input data for the program
     * @param dockerImageName Docker image to use for execution
     * @param buildCommand Custom build command (e.g., "mvn compile"). If null, defaults to javac for single .java file.
     * @param runCommand Custom run command (e.g., "mvn spring-boot:run"). If null, defaults to "java Main".
     * @param workingDir Working directory in container where files are placed
     * @return ExecutionOutput with results
     * @throws ExecutionException If execution fails
     */
    public ExecutionOutput executeCode(
            java.util.Map<String, String> files,
            String input,
            String dockerImageName,
            String buildCommand,
            String runCommand,
            String workingDir
    ) throws ExecutionException {
        String containerId = null;
        long startTime = System.currentTimeMillis();

        // Use provided docker image or fall back to default
        String imageToUse = dockerImageName != null ? dockerImageName : dockerImage;
        String workingDirectory = workingDir != null ? workingDir : "/tmp";

        // Determine build and run commands
        String finalBuildCommand = determineBuildCommand(files, buildCommand);
        String finalRunCommand = determineRunCommand(files, runCommand);

        try {
            // Create secure container with specified image
            containerId = createSecureContainer(imageToUse, workingDirectory);
            log.debug("Created container {} with image {}", containerId, imageToUse);

            // Start container
            dockerClient.startContainerCmd(containerId).exec();
            log.debug("Started container: {}", containerId);

            // Write all files to container
            writeFiles(containerId, files, workingDirectory);

            // Compile the code if build command provided
            if (finalBuildCommand != null && !finalBuildCommand.isEmpty()) {
                CompilationResult compilationResult = compileCode(containerId, finalBuildCommand, workingDirectory);
                if (!compilationResult.isSuccess()) {
                    return ExecutionOutput.builder()
                            .success(false)
                            .compilationError(true)
                            .output(compilationResult.getOutput())
                            .error(compilationResult.getError())
                            .executionTimeMs(System.currentTimeMillis() - startTime)
                            .build();
                }
            }

            // Execute the code with timeout
            ExecutionOutput output = runCode(containerId, input, timeoutSeconds, finalRunCommand, workingDirectory);
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
     * Determine build command based on files and provided command.
     */
    private String determineBuildCommand(java.util.Map<String, String> files, String buildCommand) {
        if (buildCommand != null && !buildCommand.isEmpty()) {
            return buildCommand;
        }

        // For single Java file, default to javac
        if (files.size() == 1 && files.keySet().iterator().next().endsWith(".java")) {
            String filename = files.keySet().iterator().next();
            return "javac " + filename;
        }

        // For multi-file projects, assume build command is provided or not needed
        return null;
    }

    /**
     * Determine run command based on files and provided command.
     */
    private String determineRunCommand(java.util.Map<String, String> files, String runCommand) {
        if (runCommand != null && !runCommand.isEmpty()) {
            return runCommand;
        }

        // Default to "java Main" for backwards compatibility
        return "java Main";
    }

    /**
     * Write multiple files to container.
     */
    private void writeFiles(String containerId, java.util.Map<String, String> files, String workingDirectory)
            throws InterruptedException, ExecutionException, TimeoutException {
        for (java.util.Map.Entry<String, String> entry : files.entrySet()) {
            String filePath = entry.getKey();
            String content = entry.getValue();

            // Create directory structure if needed
            String dirPath = filePath.contains("/") ?
                    filePath.substring(0, filePath.lastIndexOf("/")) : "";
            if (!dirPath.isEmpty()) {
                executeCommand(containerId, "mkdir -p " + dirPath, 5, workingDirectory);
            }

            // Write file using base64 encoding to avoid shell escaping issues
            String base64Content = Base64.getEncoder().encodeToString(content.getBytes(StandardCharsets.UTF_8));
            String command = String.format("echo '%s' | base64 -d > %s", base64Content, filePath);
            executeCommand(containerId, command, 5, workingDirectory);

            log.debug("Wrote file {} to container {}", filePath, containerId);
        }
    }

    /**
     * Create a container with resource limits.
     * Security features:
     * - Network access allowed (temporary - for Maven dependency downloads)
     * - Memory limits (256MB, no swap)
     * - CPU limits (0.5 CPU)
     * - PID limits (50 - prevents fork bombs)
     * - Non-root user (UID 1000)
     */
    private String createSecureContainer(String image, String workingDir) {
        HostConfig hostConfig = HostConfig.newHostConfig()
                // Network access (bridge mode for Maven downloads)
                .withNetworkMode("bridge")
                // Memory limits (no swap)
                .withMemory((long) maxMemoryMb * 1024 * 1024)
                .withMemorySwap((long) maxMemoryMb * 1024 * 1024)
                // CPU limit (0.5 CPU)
                .withNanoCPUs(500_000_000L) // 0.5 CPU = 500,000,000 nanocpus
                // Process limit to prevent fork bombs
                .withPidsLimit(50L);

        CreateContainerResponse container = dockerClient.createContainerCmd(image)
                .withHostConfig(hostConfig)
                // Run as non-root user (UID 1000)
                .withUser("1000:1000")
                .withWorkingDir(workingDir)
                .withCmd("sleep", "3600") // Keep container alive
                .exec();

        return container.getId();
    }

    /**
     * Write source code to container using base64 encoding to avoid shell escaping issues.
     * @deprecated Use writeFiles() instead for multi-file support.
     */
    @Deprecated
    private void writeSourceCode(String containerId, String sourceCode) throws InterruptedException, ExecutionException, TimeoutException {
        // Encode source code to base64 to safely pass through shell
        String base64Code = Base64.getEncoder().encodeToString(sourceCode.getBytes(StandardCharsets.UTF_8));

        // Decode and write to Main.java (working directory is /tmp)
        String command = String.format("echo '%s' | base64 -d > Main.java", base64Code);
        executeCommand(containerId, command, 5, "/tmp");

        log.debug("Wrote source code to Main.java in container {}", containerId);
    }

    /**
     * Compile Java code inside container with custom command.
     */
    private CompilationResult compileCode(String containerId, String compileCommand, String workingDir)
            throws InterruptedException, ExecutionException, TimeoutException {
        log.debug("Compiling code in container {} with command: {} (timeout: {}s)", containerId, compileCommand, timeoutSeconds);

        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        ByteArrayOutputStream stderr = new ByteArrayOutputStream();

        // Parse command into tokens (simple split by space - more complex commands may need shell)
        String[] cmdTokens = compileCommand.split("\\s+");

        ExecCreateCmdResponse execCreateCmd = dockerClient.execCreateCmd(containerId)
                .withCmd(cmdTokens)
                .withAttachStdout(true)
                .withAttachStderr(true)
                .withWorkingDir(workingDir)
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

        future.get(timeoutSeconds, TimeUnit.SECONDS);

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
     * Run compiled Java code with input and custom command.
     */
    private ExecutionOutput runCode(String containerId, String input, int timeoutSeconds, String runCommand, String workingDir)
            throws InterruptedException, ExecutionException, TimeoutException {
        log.debug("Running code in container {} with command: {} (timeout: {} seconds)", containerId, runCommand, timeoutSeconds);

        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        ByteArrayOutputStream stderr = new ByteArrayOutputStream();

        // Parse run command into tokens
        String[] cmdTokens = runCommand.split("\\s+");

        // Create execution command
        ExecCreateCmdResponse execCreateCmd = dockerClient.execCreateCmd(containerId)
                .withCmd(cmdTokens)
                .withAttachStdout(true)
                .withAttachStderr(true)
                .withAttachStdin(input != null && !input.isEmpty())
                .withWorkingDir(workingDir)
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

            // Truncate output if too large to prevent memory issues
            stdoutStr = truncateOutput(stdoutStr);
            stderrStr = truncateOutput(stderrStr);

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
     * Execute a command in container with timeout and working directory.
     */
    private void executeCommand(String containerId, String command, int timeoutSeconds, String workingDir)
            throws InterruptedException, ExecutionException, TimeoutException {

        ExecCreateCmdResponse execCreateCmd = dockerClient.execCreateCmd(containerId)
                .withCmd("sh", "-c", command)
                .withAttachStdout(true)
                .withAttachStderr(true)
                .withWorkingDir(workingDir)
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
     * Truncate output to prevent memory exhaustion from huge outputs.
     * Limits output to MAX_OUTPUT_SIZE bytes.
     */
    private String truncateOutput(String output) {
        if (output == null) {
            return "";
        }
        if (output.length() <= MAX_OUTPUT_SIZE) {
            return output;
        }
        return output.substring(0, MAX_OUTPUT_SIZE) + "\n... (output truncated, exceeded " + MAX_OUTPUT_SIZE + " bytes)";
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
