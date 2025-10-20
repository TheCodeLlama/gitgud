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

import com.syntaxllama.gitgud.backend.dtos.execution.SpringTestResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
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

    @Value("${code.execution.spring.test.timeout.seconds:300}")
    private int springTestTimeoutSeconds;

    @Value("${code.execution.max.memory.mb:256}")
    private int maxMemoryMb;

    @Value("${code.execution.docker.image:gitgud-java-executor:latest}")
    private String dockerImage;

    // Maximum output size to prevent memory exhaustion (10KB)
    private static final int MAX_OUTPUT_SIZE = 10 * 1024;

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
     * Execute Spring Boot project tests in a secure Docker container.
     * Writes all project files, runs Maven tests, and parses JUnit XML reports.
     *
     * @param projectFiles Map of file paths to file contents
     * @return SpringTestExecutionOutput with test results
     * @throws ExecutionException if execution fails
     */
    public SpringTestExecutionOutput executeSpringBootTests(Map<String, String> projectFiles) throws ExecutionException {
        String containerId = null;
        long startTime = System.currentTimeMillis();

        try {
            // Create secure container for Maven execution
            containerId = createMavenContainer();
            log.debug("Created Maven container: {}", containerId);

            // Start container
            dockerClient.startContainerCmd(containerId).exec();
            log.debug("Started container: {}", containerId);

            // Write all project files to container
            // (tmpfs mounted with uid=1000,gid=1000, so user can write directly)
            writeProjectFiles(containerId, projectFiles);

            // Run Maven tests with configured timeout (default 5 minutes for dependency download + build + tests)
            MavenExecutionResult mavenResult = runMavenTests(containerId, springTestTimeoutSeconds);

            if (!mavenResult.isSuccess() && mavenResult.isCompilationError()) {
                // Compilation failed
                return SpringTestExecutionOutput.builder()
                        .success(false)
                        .compilationError(true)
                        .testResults(Collections.emptyList())
                        .buildOutput(mavenResult.getBuildOutput())
                        .errorMessage(mavenResult.getErrorMessage())
                        .executionTimeMs(System.currentTimeMillis() - startTime)
                        .build();
            }

            // Read and parse JUnit XML reports
            List<SpringTestResult> testResults = parseJUnitReports(containerId);

            // Calculate overall success (all tests passed)
            long passedTests = testResults.stream().filter(SpringTestResult::getPassed).count();
            boolean allPassed = passedTests == testResults.size() && !testResults.isEmpty();

            return SpringTestExecutionOutput.builder()
                    .success(allPassed)
                    .compilationError(false)
                    .testResults(testResults)
                    .buildOutput(mavenResult.getBuildOutput())
                    .executionTimeMs(System.currentTimeMillis() - startTime)
                    .build();

        } catch (TimeoutException e) {
            log.warn("Spring Boot test execution timed out");
            return SpringTestExecutionOutput.builder()
                    .success(false)
                    .timeout(true)
                    .testResults(Collections.emptyList())
                    .errorMessage("Test execution timed out")
                    .executionTimeMs(System.currentTimeMillis() - startTime)
                    .build();

        } catch (Exception e) {
            log.error("Error executing Spring Boot tests in Docker", e);
            return SpringTestExecutionOutput.builder()
                    .success(false)
                    .testResults(Collections.emptyList())
                    .errorMessage("System error: " + e.getMessage())
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
     * Create a hardened Docker container with Maven support for Spring Boot testing.
     * Similar security constraints as single-file execution but with Maven installed.
     */
    private String createMavenContainer() {
        HostConfig hostConfig = HostConfig.newHostConfig()
                // Network isolation (no external dependencies allowed)
                .withNetworkMode("none")
                // Higher memory limits for Maven (512MB)
                .withMemory(512L * 1024 * 1024)
                .withMemorySwap(512L * 1024 * 1024)
                // CPU limit (1.0 CPU for faster builds)
                .withNanoCPUs(1_000_000_000L)
                // Process limit
                .withPidsLimit(100L)
                // Drop all Linux capabilities
                .withCapDrop(Capability.ALL)
                // Read-only root filesystem
                .withReadonlyRootfs(true)
                // Tmpfs for Maven build and dependencies (1GB for /tmp to hold .m2, 500MB for /project)
                // Mount with uid=1000,gid=1000 so user can write without chown
                .withTmpFs(java.util.Map.of(
                        "/tmp", "rw,noexec,size=1g,uid=1000,gid=1000",
                        "/project", "rw,noexec,size=500m,uid=1000,gid=1000"
                ))
                // Security options
                .withSecurityOpts(java.util.List.of("no-new-privileges"));

        CreateContainerResponse container = dockerClient.createContainerCmd(dockerImage)
                .withHostConfig(hostConfig)
                .withUser("1000:1000")
                .withWorkingDir("/project")
                .withEnv(
                        "MAVEN_OPTS=-Djansi.force=false -Djansi.passthrough=true",
                        "TERM=dumb"
                )
                .withCmd("sleep", "3600")
                .exec();

        return container.getId();
    }

    /**
     * Write all project files to the container using exec with stdin streaming.
     * This works with read-only root filesystems (files written to tmpfs /project).
     */
    private void writeProjectFiles(String containerId, Map<String, String> projectFiles)
            throws InterruptedException, ExecutionException, TimeoutException {
        log.debug("Writing {} project files to container using stdin streaming", projectFiles.size());

        for (Map.Entry<String, String> entry : projectFiles.entrySet()) {
            String filePath = entry.getKey();
            String content = entry.getValue();
            byte[] contentBytes = content.getBytes(StandardCharsets.UTF_8);

            // Create directory structure first
            String directory = getDirectoryPath(filePath);
            if (directory != null && !directory.isEmpty()) {
                String mkdirCommand = "mkdir -p /project/" + directory;
                executeCommandWithOutput(containerId, mkdirCommand, 5);
                log.debug("Created directory: /project/{}", directory);
            }

            // Write file using cat with stdin (no command-line length limits)
            String catCommand = "cat > /project/" + filePath;

            ByteArrayOutputStream stdout = new ByteArrayOutputStream();
            ByteArrayOutputStream stderr = new ByteArrayOutputStream();

            ExecCreateCmdResponse execCreateCmd = dockerClient.execCreateCmd(containerId)
                    .withCmd("sh", "-c", catCommand)
                    .withAttachStdin(true)
                    .withAttachStdout(true)
                    .withAttachStderr(true)
                    .exec();

            ByteArrayInputStream contentStream = new ByteArrayInputStream(contentBytes);

            Future<Integer> future = executorService.submit(() -> {
                try {
                    dockerClient.execStartCmd(execCreateCmd.getId())
                            .withStdIn(contentStream)
                            .exec(new ExecStartResultCallback(stdout, stderr))
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

            Integer exitCode = future.get(10, TimeUnit.SECONDS);

            if (exitCode != 0) {
                String errorOutput = stderr.toString(StandardCharsets.UTF_8);
                String stdOutput = stdout.toString(StandardCharsets.UTF_8);
                log.error("Failed to write file {}: exit code {}, stderr: {}, stdout: {}",
                          filePath, exitCode, errorOutput, stdOutput);
                throw new RuntimeException("Failed to write file " + filePath + ": " + errorOutput);
            }

            log.debug("Wrote file via stdin: {} ({} bytes)", filePath, contentBytes.length);
        }

        log.info("Successfully wrote all {} project files to container", projectFiles.size());

        // Verify files were written by listing directory
        verifyFilesWritten(containerId, projectFiles.keySet());
    }

    /**
     * Verify that files were actually written to the container.
     */
    private void verifyFilesWritten(String containerId, Set<String> expectedFiles)
            throws InterruptedException, ExecutionException, TimeoutException {
        log.debug("Verifying {} files were written to container", expectedFiles.size());

        String lsOutput = executeCommandWithOutput(containerId, "find /project -type f", 10);
        log.debug("Files in /project:\n{}", lsOutput);

        for (String expectedFile : expectedFiles) {
            if (!lsOutput.contains(expectedFile)) {
                log.error("File {} was not found in container!", expectedFile);
                throw new RuntimeException("File verification failed: " + expectedFile + " not found");
            }
        }

        log.info("Verified all {} files exist in container", expectedFiles.size());
    }

    /**
     * Execute command and return stdout output.
     */
    private String executeCommandWithOutput(String containerId, String command, int timeoutSeconds)
            throws InterruptedException, ExecutionException, TimeoutException {

        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        ByteArrayOutputStream stderr = new ByteArrayOutputStream();

        ExecCreateCmdResponse execCreateCmd = dockerClient.execCreateCmd(containerId)
                .withCmd("sh", "-c", command)
                .withAttachStdout(true)
                .withAttachStderr(true)
                .exec();

        Future<Integer> future = executorService.submit(() -> {
            try {
                dockerClient.execStartCmd(execCreateCmd.getId())
                        .exec(new ExecStartResultCallback(stdout, stderr))
                        .awaitCompletion();

                return dockerClient.inspectExecCmd(execCreateCmd.getId())
                        .exec()
                        .getExitCodeLong()
                        .intValue();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        });

        Integer exitCode = future.get(timeoutSeconds, TimeUnit.SECONDS);
        String stdoutStr = stdout.toString(StandardCharsets.UTF_8);
        String stderrStr = stderr.toString(StandardCharsets.UTF_8);

        if (exitCode != 0) {
            log.warn("Command '{}' exited with code {}, stderr: {}", command, exitCode, stderrStr);
        }

        return stdoutStr;
    }


    /**
     * Run Maven tests and capture output.
     */
    private MavenExecutionResult runMavenTests(String containerId, int timeoutSeconds)
            throws InterruptedException, ExecutionException, TimeoutException {
        log.info("Running Maven tests in container {} with timeout {} seconds (first run may take 2-3 minutes to download dependencies)",
                 containerId, timeoutSeconds);

        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        ByteArrayOutputStream stderr = new ByteArrayOutputStream();

        // Run mvn clean test with local repository in /tmp (read-only filesystem workaround)
        ExecCreateCmdResponse execCreateCmd = dockerClient.execCreateCmd(containerId)
                .withCmd("mvn", "clean", "test", "-B",
                        "-Dmaven.repo.local=/tmp/.m2/repository")
                .withAttachStdout(true)
                .withAttachStderr(true)
                .withWorkingDir("/project")
                .exec();

        Future<Integer> future = executorService.submit(() -> {
            try {
                // Use a logging callback to stream output in real-time
                LoggingExecStartResultCallback callback = new LoggingExecStartResultCallback(stdout, stderr);
                dockerClient.execStartCmd(execCreateCmd.getId())
                        .exec(callback)
                        .awaitCompletion();

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

            String buildOutput = stdout.toString(StandardCharsets.UTF_8);
            String errorOutput = stderr.toString(StandardCharsets.UTF_8);

            // Check for compilation errors
            boolean compilationError = buildOutput.contains("[ERROR] COMPILATION ERROR") ||
                                     buildOutput.contains("BUILD FAILURE") && buildOutput.contains("compilation failed");

            return MavenExecutionResult.builder()
                    .success(exitCode == 0)
                    .buildOutput(truncateOutput(buildOutput + "\n" + errorOutput))
                    .errorMessage(compilationError ? "Compilation failed" : null)
                    .compilationError(compilationError)
                    .build();

        } catch (TimeoutException e) {
            future.cancel(true);
            dockerClient.killContainerCmd(containerId).exec();
            throw e;
        }
    }

    /**
     * Parse JUnit XML reports from Surefire output directory.
     * Reads XML files from /project/target/surefire-reports/*.xml
     */
    private List<SpringTestResult> parseJUnitReports(String containerId)
            throws InterruptedException, ExecutionException, TimeoutException {
        List<SpringTestResult> results = new ArrayList<>();

        try {
            // List all XML files in surefire-reports
            String listCommand = "find /project/target/surefire-reports -name 'TEST-*.xml' 2>/dev/null || true";
            ByteArrayOutputStream listOutput = new ByteArrayOutputStream();

            ExecCreateCmdResponse listExec = dockerClient.execCreateCmd(containerId)
                    .withCmd("sh", "-c", listCommand)
                    .withAttachStdout(true)
                    .exec();

            Future<Void> listFuture = executorService.submit(() -> {
                try {
                    dockerClient.execStartCmd(listExec.getId())
                            .exec(new ExecStartResultCallback(listOutput, new ByteArrayOutputStream()))
                            .awaitCompletion();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                }
                return null;
            });

            listFuture.get(10, TimeUnit.SECONDS);

            String xmlFiles = listOutput.toString(StandardCharsets.UTF_8).trim();
            if (xmlFiles.isEmpty()) {
                log.warn("No JUnit XML reports found in container");
                return results;
            }

            // Read and parse each XML file
            String[] files = xmlFiles.split("\n");
            for (String file : files) {
                if (file.trim().isEmpty()) continue;

                String catCommand = "cat " + file.trim();
                ByteArrayOutputStream xmlContent = new ByteArrayOutputStream();

                ExecCreateCmdResponse catExec = dockerClient.execCreateCmd(containerId)
                        .withCmd("sh", "-c", catCommand)
                        .withAttachStdout(true)
                        .exec();

                Future<Void> catFuture = executorService.submit(() -> {
                    try {
                        dockerClient.execStartCmd(catExec.getId())
                                .exec(new ExecStartResultCallback(xmlContent, new ByteArrayOutputStream()))
                                .awaitCompletion();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException(e);
                    }
                    return null;
                });

                catFuture.get(10, TimeUnit.SECONDS);

                // Parse XML
                List<SpringTestResult> fileResults = parseJUnitXml(xmlContent.toByteArray());
                results.addAll(fileResults);
            }

        } catch (Exception e) {
            log.error("Error parsing JUnit reports", e);
        }

        return results;
    }

    /**
     * Parse a single JUnit XML file into test results.
     */
    private List<SpringTestResult> parseJUnitXml(byte[] xmlData) {
        List<SpringTestResult> results = new ArrayList<>();

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new ByteArrayInputStream(xmlData));

            String className = doc.getDocumentElement().getAttribute("name");

            // Parse test cases
            NodeList testCases = doc.getElementsByTagName("testcase");
            for (int i = 0; i < testCases.getLength(); i++) {
                Element testCase = (Element) testCases.item(i);

                String methodName = testCase.getAttribute("name");
                String testClassName = testCase.getAttribute("classname");
                String timeStr = testCase.getAttribute("time");

                long executionTimeMs = 0;
                if (timeStr != null && !timeStr.isEmpty()) {
                    try {
                        executionTimeMs = (long) (Double.parseDouble(timeStr) * 1000);
                    } catch (NumberFormatException e) {
                        // Ignore
                    }
                }

                // Check for failures or errors
                NodeList failures = testCase.getElementsByTagName("failure");
                NodeList errors = testCase.getElementsByTagName("error");

                boolean passed = failures.getLength() == 0 && errors.getLength() == 0;
                String errorMessage = null;
                String stackTrace = null;
                String errorType = null;

                if (!passed) {
                    Element errorElement = failures.getLength() > 0 ?
                            (Element) failures.item(0) : (Element) errors.item(0);

                    errorType = errorElement.getAttribute("type");
                    errorMessage = errorElement.getAttribute("message");
                    stackTrace = errorElement.getTextContent();
                }

                SpringTestResult result = SpringTestResult.builder()
                        .className(testClassName != null && !testClassName.isEmpty() ? testClassName : className)
                        .methodName(methodName)
                        .testName((testClassName != null ? testClassName : className) + "." + methodName)
                        .passed(passed)
                        .executionTimeMs(executionTimeMs)
                        .errorMessage(errorMessage)
                        .stackTrace(stackTrace)
                        .errorType(errorType)
                        .build();

                results.add(result);
            }

        } catch (Exception e) {
            log.error("Error parsing JUnit XML", e);
        }

        return results;
    }

    /**
     * Extract directory path from file path.
     * Example: "src/main/java/com/example/User.java" -> "src/main/java/com/example"
     */
    private String getDirectoryPath(String filePath) {
        int lastSlash = filePath.lastIndexOf('/');
        if (lastSlash > 0) {
            return filePath.substring(0, lastSlash);
        }
        return null;
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
     * - Tmpfs for /tmp (100MB, nosuid - exec allowed for Java compilation)
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
                // Tmpfs for temporary files (100MB, nosuid but exec allowed for Java compilation)
                // Note: We need exec for javac/java but nosuid prevents privilege escalation
                .withTmpFs(java.util.Map.of(
                        "/tmp", "rw,nosuid,size=100m"
                ))
                // Security options (no-new-privileges prevents privilege escalation)
                .withSecurityOpts(java.util.List.of("no-new-privileges"));

        CreateContainerResponse container = dockerClient.createContainerCmd(dockerImage)
                .withHostConfig(hostConfig)
                // Run as non-root user (UID 1000)
                .withUser("1000:1000")
                .withWorkingDir("/tmp")
                .withCmd("sleep", "3600") // Keep container alive
                .exec();

        return container.getId();
    }

    /**
     * Write source code to container using base64 encoding to avoid shell escaping issues.
     */
    private void writeSourceCode(String containerId, String sourceCode) throws InterruptedException, ExecutionException, TimeoutException {
        // Encode source code to base64 to safely pass through shell
        String base64Code = Base64.getEncoder().encodeToString(sourceCode.getBytes(StandardCharsets.UTF_8));

        // Decode and write to Main.java (working directory is /tmp)
        String command = String.format("echo '%s' | base64 -d > Main.java", base64Code);
        executeCommand(containerId, command, 5);

        log.debug("Wrote source code to Main.java in container {}", containerId);
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
                .withWorkingDir("/tmp")
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

        // Create execution command (run from /tmp directory where Main.class is)
        ExecCreateCmdResponse execCreateCmd = dockerClient.execCreateCmd(containerId)
                .withCmd("java", "Main")
                .withAttachStdout(true)
                .withAttachStderr(true)
                .withAttachStdin(input != null && !input.isEmpty())
                .withWorkingDir("/tmp")
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
     * Custom callback that logs Maven output in real-time while also capturing it to ByteArrayOutputStream.
     */
    private class LoggingExecStartResultCallback extends com.github.dockerjava.api.async.ResultCallback.Adapter<com.github.dockerjava.api.model.Frame> {
        private final ByteArrayOutputStream stdout;
        private final ByteArrayOutputStream stderr;
        private StringBuilder lineBuffer = new StringBuilder();
        private boolean lastWasCarriageReturn = false;

        public LoggingExecStartResultCallback(ByteArrayOutputStream stdout, ByteArrayOutputStream stderr) {
            this.stdout = stdout;
            this.stderr = stderr;
        }

        @Override
        public void onNext(com.github.dockerjava.api.model.Frame frame) {
            try {
                byte[] payload = frame.getPayload();
                if (payload == null || payload.length == 0) {
                    return;
                }

                // Write to appropriate stream
                if (frame.getStreamType() == com.github.dockerjava.api.model.StreamType.STDOUT) {
                    stdout.write(payload);
                } else if (frame.getStreamType() == com.github.dockerjava.api.model.StreamType.STDERR) {
                    stderr.write(payload);
                }

                // Log line by line
                String text = new String(payload, StandardCharsets.UTF_8);
                for (char c : text.toCharArray()) {
                    if (c == '\n') {
                        String line = lineBuffer.toString();
                        if (!line.trim().isEmpty()) {
                            // Log important Maven output
                            if (line.contains("Downloading") || line.contains("Downloaded") ||
                                line.contains("Building") || line.contains("SUCCESS") ||
                                line.contains("FAILURE") || line.contains("ERROR") ||
                                line.contains("Tests run:")) {
                                log.info("[Maven] {}", line);
                            } else {
                                log.debug("[Maven] {}", line);
                            }
                        }
                        lineBuffer = new StringBuilder();
                        lastWasCarriageReturn = false;
                    } else if (c == '\r') {
                        // Handle carriage return (for progress indicators)
                        lastWasCarriageReturn = true;
                        String line = lineBuffer.toString();
                        if (!line.trim().isEmpty() && line.contains("Progress")) {
                            log.debug("[Maven] {}", line);
                        }
                        lineBuffer = new StringBuilder();
                    } else {
                        lineBuffer.append(c);
                        lastWasCarriageReturn = false;
                    }
                }
            } catch (Exception e) {
                log.error("Error processing frame", e);
            }
        }

        @Override
        public void onComplete() {
            // Log any remaining buffer
            if (lineBuffer.length() > 0) {
                String line = lineBuffer.toString();
                if (!line.trim().isEmpty()) {
                    log.debug("[Maven] {}", line);
                }
            }
            super.onComplete();
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

    /**
     * Result of Maven execution.
     */
    @lombok.Data
    @lombok.Builder
    public static class MavenExecutionResult {
        private boolean success;
        private String buildOutput;
        private String errorMessage;
        private boolean compilationError;
    }

    /**
     * Result of Spring Boot test execution.
     */
    @lombok.Data
    @lombok.Builder
    public static class SpringTestExecutionOutput {
        private boolean success;
        private boolean compilationError;
        private boolean timeout;
        @lombok.Builder.Default
        private List<SpringTestResult> testResults = new ArrayList<>();
        private String buildOutput;
        private String errorMessage;
        private Long executionTimeMs;
    }
}
