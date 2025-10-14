package com.syntaxllama.gitgud.backend.services.execution;

import com.syntaxllama.gitgud.backend.configs.RabbitMQConfig;
import com.syntaxllama.gitgud.backend.dtos.execution.*;
import com.syntaxllama.gitgud.backend.models.Lesson;
import com.syntaxllama.gitgud.backend.models.Submission;
import com.syntaxllama.gitgud.backend.models.TestCase;
import com.syntaxllama.gitgud.backend.models.User;
import com.syntaxllama.gitgud.backend.repositories.LessonRepository;
import com.syntaxllama.gitgud.backend.repositories.SubmissionRepository;
import com.syntaxllama.gitgud.backend.repositories.TestCaseRepository;
import com.syntaxllama.gitgud.backend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Worker service that processes code execution jobs from RabbitMQ queue.
 * Executes code in Docker containers and stores results in Redis.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CodeExecutionWorker {

    private final DockerExecutorService dockerExecutor;
    private final TestCaseRepository testCaseRepository;
    private final CodeExecutionService executionService;
    private final OutputComparisonService comparisonService;
    private final SubmissionRepository submissionRepository;
    private final UserRepository userRepository;
    private final LessonRepository lessonRepository;

    /**
     * Listen for code execution jobs from RabbitMQ queue.
     * Processes each job by executing the code and validating against test cases.
     */
    @RabbitListener(queues = RabbitMQConfig.CODE_EXECUTION_QUEUE)
    public void processExecutionJob(CodeExecutionJob job) {
        log.info("Processing execution job {} for user {} and lesson {}",
                job.getJobId(), job.getUserId(), job.getLessonId());

        try {
            // Update status to RUNNING
            executionService.updateJobStatus(job.getJobId(), ExecutionStatus.RUNNING);

            // Load test cases
            List<TestCase> testCases = testCaseRepository.findAllById(job.getTestCaseIds());
            if (testCases.isEmpty()) {
                failJob(job.getJobId(), "No test cases found");
                return;
            }

            log.info("Running {} test cases for job {}", testCases.size(), job.getJobId());

            // Execute code against each test case
            List<TestCaseResult> testCaseResults = new ArrayList<>();
            int passedTests = 0;
            long totalExecutionTime = 0;

            for (TestCase testCase : testCases) {
                TestCaseResult result = executeTestCase(job.getSourceCode(), testCase);
                testCaseResults.add(result);

                if (result.getPassed()) {
                    passedTests++;
                }

                totalExecutionTime += result.getExecutionTimeMs();
            }

            // Calculate overall result
            boolean allPassed = passedTests == testCases.size();
            ExecutionStatus status = allPassed ? ExecutionStatus.COMPLETED : ExecutionStatus.FAILED;

            // Calculate XP with partial credit (proportional to tests passed)
            int xpAwarded = calculatePartialCreditXp(passedTests, testCases.size());

            // Build final result
            ExecutionResult result = ExecutionResult.builder()
                    .jobId(job.getJobId())
                    .status(status)
                    .passed(allPassed)
                    .testsPassed(passedTests)
                    .totalTests(testCases.size())
                    .testCaseResults(testCaseResults)
                    .executionTimeMs(totalExecutionTime)
                    .startedAt(job.getSubmittedAt())
                    .completedAt(LocalDateTime.now())
                    .xpAwarded(xpAwarded)
                    .build();

            // Store result in Redis
            executionService.storeResult(job.getJobId(), result);

            // Save submission to database
            saveSubmission(job, result);

            log.info("Job {} completed: {}/{} tests passed", job.getJobId(), passedTests, testCases.size());

        } catch (Exception e) {
            log.error("Error processing job {}", job.getJobId(), e);
            failJob(job.getJobId(), "Execution error: " + e.getMessage());
        }
    }

    /**
     * Execute code against a single test case.
     */
    private TestCaseResult executeTestCase(String sourceCode, TestCase testCase) {
        log.debug("Executing test case {}", testCase.getId());

        long startTime = System.currentTimeMillis();

        try {
            // Execute code with test case input
            DockerExecutorService.ExecutionOutput output =
                    dockerExecutor.executeJavaCode(sourceCode, testCase.getInput());

            long executionTime = System.currentTimeMillis() - startTime;

            // Handle compilation errors
            if (output.isCompilationError()) {
                return TestCaseResult.builder()
                        .testCaseId(testCase.getId())
                        .passed(false)
                        .input(testCase.getInput())
                        .expectedOutput(testCase.getExpectedOutput())
                        .actualOutput("")
                        .errorMessage("Compilation error: " + output.getError())
                        .executionTimeMs(executionTime)
                        .build();
            }

            // Handle timeout
            if (output.isTimeout()) {
                return TestCaseResult.builder()
                        .testCaseId(testCase.getId())
                        .passed(false)
                        .input(testCase.getInput())
                        .expectedOutput(testCase.getExpectedOutput())
                        .actualOutput("")
                        .errorMessage("Execution timed out")
                        .executionTimeMs(executionTime)
                        .build();
            }

            // Handle runtime errors
            if (!output.isSuccess()) {
                return TestCaseResult.builder()
                        .testCaseId(testCase.getId())
                        .passed(false)
                        .input(testCase.getInput())
                        .expectedOutput(testCase.getExpectedOutput())
                        .actualOutput(output.getOutput())
                        .errorMessage("Runtime error: " + output.getError())
                        .executionTimeMs(executionTime)
                        .build();
            }

            // Compare output with expected using whitespace normalization
            String actualOutput = output.getOutput();
            String expectedOutput = testCase.getExpectedOutput();

            // Try whitespace-normalized comparison first
            boolean passed = comparisonService.compareWithWhitespaceNormalization(expectedOutput, actualOutput);

            // If that fails, try numeric tolerance comparison
            if (!passed) {
                passed = comparisonService.compareWithNumericTolerance(expectedOutput, actualOutput);
            }

            return TestCaseResult.builder()
                    .testCaseId(testCase.getId())
                    .passed(passed)
                    .input(testCase.getInput())
                    .expectedOutput(expectedOutput.trim())
                    .actualOutput(actualOutput.trim())
                    .errorMessage(passed ? null : "Output does not match expected")
                    .executionTimeMs(executionTime)
                    .build();

        } catch (Exception e) {
            log.error("Error executing test case {}", testCase.getId(), e);
            return TestCaseResult.builder()
                    .testCaseId(testCase.getId())
                    .passed(false)
                    .input(testCase.getInput())
                    .expectedOutput(testCase.getExpectedOutput())
                    .actualOutput("")
                    .errorMessage("System error: " + e.getMessage())
                    .executionTimeMs(System.currentTimeMillis() - startTime)
                    .build();
        }
    }

    /**
     * Mark job as failed and store error result.
     */
    private void failJob(String jobId, String errorMessage) {
        ExecutionResult result = ExecutionResult.builder()
                .jobId(jobId)
                .status(ExecutionStatus.FAILED)
                .passed(false)
                .errorMessage(errorMessage)
                .completedAt(LocalDateTime.now())
                .build();

        executionService.storeResult(jobId, result);
        log.warn("Job {} failed: {}", jobId, errorMessage);
    }

    /**
     * Save submission to database for historical tracking.
     * Stores user code, execution results, and metadata.
     *
     * @param job The execution job
     * @param result The execution result
     */
    private void saveSubmission(CodeExecutionJob job, ExecutionResult result) {
        try {
            // Fetch user and lesson entities
            User user = userRepository.findById(job.getUserId())
                    .orElse(null);
            Lesson lesson = lessonRepository.findById(job.getLessonId())
                    .orElse(null);

            if (user == null || lesson == null) {
                log.warn("Cannot save submission for job {}: user or lesson not found", job.getJobId());
                return;
            }

            // Determine submission status
            Submission.Status status;
            if (result.getStatus() == ExecutionStatus.COMPLETED && result.getPassed()) {
                status = Submission.Status.PASSED;
            } else if (result.getStatus() == ExecutionStatus.COMPLETED) {
                status = Submission.Status.FAILED;
            } else if (result.getStatus() == ExecutionStatus.FAILED) {
                status = Submission.Status.ERROR;
            } else {
                status = Submission.Status.PENDING;
            }

            // Create submission entity
            Submission submission = new Submission();
            submission.setUser(user);
            submission.setLesson(lesson);
            submission.setCode(job.getSourceCode());
            submission.setStatus(status);
            submission.setPassedTests(result.getTestsPassed());
            submission.setTotalTests(result.getTotalTests());
            submission.setExecutionTimeMs(result.getExecutionTimeMs());
            submission.setErrorMessage(result.getErrorMessage());
            submission.setConsoleOutput(result.getConsoleOutput());
            submission.setXpAwarded(result.getXpAwarded());
            submission.setSubmittedAt(job.getSubmittedAt());

            // Save to database
            submissionRepository.save(submission);

            log.info("Saved submission for user {} and lesson {} (status: {})",
                    user.getId(), lesson.getId(), status);

        } catch (Exception e) {
            // Don't fail the job if submission save fails - result is already in Redis
            log.error("Failed to save submission for job {}: {}", job.getJobId(), e.getMessage(), e);
        }
    }

    /**
     * Calculate XP with partial credit based on proportion of tests passed.
     * Awards proportional XP even if not all tests pass.
     *
     * Formula:
     * - Base XP per test case: 10 XP
     * - Partial credit: (passedTests / totalTests) * totalPossibleXP
     * - Minimum 0 XP if no tests pass
     *
     * Examples:
     * - 5/5 tests passed: 50 XP (100%)
     * - 4/5 tests passed: 40 XP (80%)
     * - 1/5 tests passed: 10 XP (20%)
     * - 0/5 tests passed: 0 XP (0%)
     *
     * @param passedTests Number of tests that passed
     * @param totalTests Total number of tests
     * @return XP awarded (rounded down to nearest integer)
     */
    private Integer calculatePartialCreditXp(int passedTests, int totalTests) {
        if (totalTests == 0 || passedTests == 0) {
            return 0;
        }

        // Base XP: 10 per test case
        int baseXpPerTest = 10;
        int totalPossibleXp = totalTests * baseXpPerTest;

        // Calculate proportional XP
        double proportion = (double) passedTests / totalTests;
        int xpAwarded = (int) Math.floor(proportion * totalPossibleXp);

        log.debug("XP calculation: {}/{} tests passed = {} XP ({}%)",
            passedTests, totalTests, xpAwarded, (int)(proportion * 100));

        return xpAwarded;
    }
}
