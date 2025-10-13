package com.syntaxllama.gitgud.backend.service.execution;

import com.syntaxllama.gitgud.backend.config.RabbitMQConfig;
import com.syntaxllama.gitgud.backend.dto.execution.*;
import com.syntaxllama.gitgud.backend.model.TestCase;
import com.syntaxllama.gitgud.backend.repository.TestCaseRepository;
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
                    .xpAwarded(allPassed ? calculateXp(testCases.size()) : 0)
                    .build();

            // Store result in Redis
            executionService.storeResult(job.getJobId(), result);

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

            // Compare output with expected
            String actualOutput = output.getOutput().trim();
            String expectedOutput = testCase.getExpectedOutput().trim();
            boolean passed = actualOutput.equals(expectedOutput);

            return TestCaseResult.builder()
                    .testCaseId(testCase.getId())
                    .passed(passed)
                    .input(testCase.getInput())
                    .expectedOutput(expectedOutput)
                    .actualOutput(actualOutput)
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
     * Calculate XP awarded based on number of test cases passed.
     * Basic formula for MVP - can be enhanced later.
     */
    private Integer calculateXp(int testCaseCount) {
        // Base XP: 10 per test case
        return testCaseCount * 10;
    }
}
