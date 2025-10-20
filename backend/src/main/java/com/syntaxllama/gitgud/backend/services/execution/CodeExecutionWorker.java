package com.syntaxllama.gitgud.backend.services.execution;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.syntaxllama.gitgud.backend.configs.RabbitMQConfig;
import com.syntaxllama.gitgud.backend.dtos.execution.*;
import com.syntaxllama.gitgud.backend.dtos.gamification.AwardXpRequest;
import com.syntaxllama.gitgud.backend.dtos.gamification.UserAchievementDTO;
import com.syntaxllama.gitgud.backend.dtos.gamification.XpAwardResult;
import com.syntaxllama.gitgud.backend.dtos.learning.UpdateProgressRequest;
import com.syntaxllama.gitgud.backend.models.Lesson;
import com.syntaxllama.gitgud.backend.models.Submission;
import com.syntaxllama.gitgud.backend.models.TestCase;
import com.syntaxllama.gitgud.backend.models.User;
import com.syntaxllama.gitgud.backend.models.UserProgress;
import com.syntaxllama.gitgud.backend.models.UserStats;
import com.syntaxllama.gitgud.backend.repositories.LessonRepository;
import com.syntaxllama.gitgud.backend.repositories.SubmissionRepository;
import com.syntaxllama.gitgud.backend.repositories.TestCaseRepository;
import com.syntaxllama.gitgud.backend.repositories.UserRepository;
import com.syntaxllama.gitgud.backend.repositories.UserProgressRepository;
import com.syntaxllama.gitgud.backend.repositories.UserStatsRepository;
import com.syntaxllama.gitgud.backend.services.gamification.AchievementService;
import com.syntaxllama.gitgud.backend.services.gamification.XpService;
import com.syntaxllama.gitgud.backend.services.learning.ProgressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
    private final UserProgressRepository userProgressRepository;
    private final XpService xpService;
    private final ProgressService progressService;
    private final ObjectMapper objectMapper;
    private final AchievementService achievementService;
    private final UserStatsRepository userStatsRepository;

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

            // Load lesson to get Docker configuration
            Lesson lesson = lessonRepository.findById(job.getLessonId()).orElse(null);
            if (lesson == null) {
                failJob(job.getJobId(), "Lesson not found");
                return;
            }

            // Determine files map (multi-file or backwards-compatible single file)
            java.util.Map<String, String> filesToExecute;
            if (job.getFiles() != null && !job.getFiles().isEmpty()) {
                filesToExecute = job.getFiles();
            } else if (job.getSourceCode() != null) {
                // Backwards compatibility: wrap single source code in map
                filesToExecute = java.util.Map.of("Main.java", job.getSourceCode());
            } else {
                failJob(job.getJobId(), "No source code or files provided");
                return;
            }

            // Execute code against each test case
            List<TestCaseResult> testCaseResults = new ArrayList<>();
            int passedTests = 0;
            long totalExecutionTime = 0;

            for (TestCase testCase : testCases) {
                TestCaseResult result = executeTestCase(
                        filesToExecute,
                        testCase,
                        lesson.getDockerImage(),
                        lesson.getBuildCommand(),
                        lesson.getRunCommand(),
                        lesson.getWorkingDirectory()
                );
                testCaseResults.add(result);

                if (result.getPassed()) {
                    passedTests++;
                }

                totalExecutionTime += result.getExecutionTimeMs();
            }

            // Calculate overall result
            boolean allPassed = passedTests == testCases.size();
            ExecutionStatus status = allPassed ? ExecutionStatus.COMPLETED : ExecutionStatus.FAILED;

            // Load user for XP/progress updates
            User user = userRepository.findById(job.getUserId()).orElse(null);
            if (user == null) {
                failJob(job.getJobId(), "User not found");
                return;
            }

            // Award XP if all tests passed (MUST be done BEFORE updating progress to COMPLETED)
            XpAwardResult xpResult = null;
            int xpAwarded = 0;
            if (allPassed) {
                xpResult = awardXpForCompletion(user, lesson);
                xpAwarded = xpResult.getXpAwarded() != null ? xpResult.getXpAwarded().intValue() : 0;
                log.info("XP award result for job {}: {} XP (achievements will be checked after progress update)",
                        job.getJobId(), xpAwarded);
            }

            // Update user progress status (done AFTER XP award to avoid double-completion check)
            updateUserProgress(user, lesson, allPassed, passedTests, testCases.size());

            // Check and award achievements AFTER progress is updated to COMPLETED
            List<UserAchievementDTO> achievementsEarned = new ArrayList<>();
            if (allPassed) {
                UserStats userStats = userStatsRepository.findByUserId(user.getId()).orElse(null);
                if (userStats != null) {
                    achievementsEarned = achievementService.checkAndAwardAchievements(user, userStats);
                    if (!achievementsEarned.isEmpty()) {
                        log.info("User {} earned {} achievement(s) after completing lesson {}",
                                user.getId(), achievementsEarned.size(), lesson.getId());
                    }
                }
            }

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
                    .achievementsEarned(achievementsEarned)
                    .build();

            log.info("ExecutionResult for job {}: xpAwarded={}, achievements={}, passed={}",
                    job.getJobId(), result.getXpAwarded(),
                    result.getAchievementsEarned() != null ? result.getAchievementsEarned().size() : 0,
                    result.getPassed());

            // Store result in Redis
            executionService.storeResult(job.getJobId(), result);

            // Save submission to database
            saveSubmission(job, result);

            log.info("Job {} completed: {}/{} tests passed, {} XP awarded, {} achievement(s) earned",
                    job.getJobId(), passedTests, testCases.size(), xpAwarded, achievementsEarned.size());

        } catch (Exception e) {
            log.error("Error processing job {}", job.getJobId(), e);
            failJob(job.getJobId(), "Execution error: " + e.getMessage());
        }
    }

    /**
     * Execute code against a single test case with multi-file support.
     */
    private TestCaseResult executeTestCase(
            java.util.Map<String, String> files,
            TestCase testCase,
            String dockerImage,
            String buildCommand,
            String runCommand,
            String workingDirectory
    ) {
        log.debug("Executing test case {} with {} files", testCase.getId(), files.size());

        long startTime = System.currentTimeMillis();

        try {
            // Execute code with test case input using new multi-file method
            DockerExecutorService.ExecutionOutput output =
                    dockerExecutor.executeCode(
                            files,
                            testCase.getInput(),
                            dockerImage,
                            buildCommand,
                            runCommand,
                            workingDirectory
                    );

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

            // Serialize test case results to JSON
            String testCaseResultsJson = null;
            if (result.getTestCaseResults() != null && !result.getTestCaseResults().isEmpty()) {
                try {
                    testCaseResultsJson = objectMapper.writeValueAsString(result.getTestCaseResults());
                } catch (Exception e) {
                    log.warn("Failed to serialize test case results for job {}: {}", job.getJobId(), e.getMessage());
                }
            }

            // Determine code to save (backwards compatible with single file or serialize multi-file)
            String codeToSave;
            if (job.getFiles() != null && !job.getFiles().isEmpty()) {
                // Multi-file: serialize to JSON
                try {
                    codeToSave = objectMapper.writeValueAsString(job.getFiles());
                } catch (Exception e) {
                    log.warn("Failed to serialize files for job {}: {}", job.getJobId(), e.getMessage());
                    codeToSave = job.getFiles().toString();
                }
            } else {
                // Single file: use sourceCode directly
                codeToSave = job.getSourceCode();
            }

            // Create submission entity
            Submission submission = new Submission();
            submission.setUser(user);
            submission.setLesson(lesson);
            submission.setCode(codeToSave);
            submission.setStatus(status);
            submission.setPassedTests(result.getTestsPassed());
            submission.setTotalTests(result.getTotalTests());
            submission.setExecutionTimeMs(result.getExecutionTimeMs());
            submission.setErrorMessage(result.getErrorMessage());
            submission.setConsoleOutput(result.getConsoleOutput());
            submission.setXpAwarded(result.getXpAwarded());
            submission.setTestCaseResultsJson(testCaseResultsJson);
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
     * Update user progress for a lesson based on execution results.
     *
     * @param user The user who submitted the code
     * @param lesson The lesson being attempted
     * @param allPassed Whether all tests passed
     * @param passedTests Number of tests passed
     * @param totalTests Total number of tests
     */
    private void updateUserProgress(User user, Lesson lesson, boolean allPassed, int passedTests, int totalTests) {
        try {
            // Determine the new status
            UserProgress.Status newStatus;
            if (allPassed) {
                newStatus = UserProgress.Status.COMPLETED;
            } else if (passedTests > 0) {
                newStatus = UserProgress.Status.IN_PROGRESS;
            } else {
                newStatus = UserProgress.Status.IN_PROGRESS; // Still mark as started even if all failed
            }

            // Calculate score as percentage
            Integer score = totalTests > 0 ? (passedTests * 100) / totalTests : 0;

            // Update progress via service
            UpdateProgressRequest progressRequest = UpdateProgressRequest.builder()
                    .lessonId(lesson.getId())
                    .status(newStatus)
                    .score(score)
                    .build();

            progressService.updateProgress(user, progressRequest);

            log.info("Updated progress for user {} on lesson {}: status={}, score={}",
                    user.getId(), lesson.getId(), newStatus, score);

        } catch (Exception e) {
            log.error("Failed to update progress for user {} on lesson {}: {}",
                    user.getId(), lesson.getId(), e.getMessage(), e);
            // Don't fail the job if progress update fails
        }
    }

    /**
     * Award XP to user for completing a lesson.
     * Only awards XP on first completion to prevent farming.
     *
     * @param user The user who completed the lesson
     * @param lesson The completed lesson
     * @return XpAwardResult with XP and achievements
     */
    private XpAwardResult awardXpForCompletion(User user, Lesson lesson) {
        try {
            // Check if user has already completed this lesson
            Optional<UserProgress> existingProgress =
                    userProgressRepository.findByUserIdAndLessonId(user.getId(), lesson.getId());

            // Only award XP if this is first completion
            boolean firstCompletion = existingProgress.isEmpty() ||
                    existingProgress.get().getStatus() != UserProgress.Status.COMPLETED;

            if (!firstCompletion) {
                log.info("User {} has already completed lesson {}. No XP awarded.",
                        user.getId(), lesson.getId());
                return XpAwardResult.builder()
                        .xpAwarded(0L)
                        .achievementsEarned(new ArrayList<>())
                        .build();
            }

            // Award XP via XpService (includes difficulty multiplier, streak bonus, etc.)
            AwardXpRequest xpRequest = AwardXpRequest.builder()
                    .lessonId(lesson.getId())
                    .baseXp(lesson.getXpReward())
                    .difficulty(lesson.getDifficulty().name())
                    .firstAttempt(true) // This is their first completion
                    .build();

            XpAwardResult xpResult = xpService.awardXp(user, xpRequest);

            log.info("Awarded {} XP to user {} for completing lesson {} (level: {}, leveledUp: {}, achievements: {})",
                    xpResult.getXpAwarded(), user.getId(), lesson.getId(),
                    xpResult.getCurrentLevel(), xpResult.getLeveledUp(),
                    xpResult.getAchievementsEarned() != null ? xpResult.getAchievementsEarned().size() : 0);

            return xpResult;

        } catch (Exception e) {
            log.error("Failed to award XP to user {} for lesson {}: {}",
                    user.getId(), lesson.getId(), e.getMessage(), e);
            // Return empty result instead of failing the job
            return XpAwardResult.builder()
                    .xpAwarded(0L)
                    .achievementsEarned(new ArrayList<>())
                    .build();
        }
    }
}
