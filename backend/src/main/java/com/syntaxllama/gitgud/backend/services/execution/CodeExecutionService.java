package com.syntaxllama.gitgud.backend.services.execution;

import com.syntaxllama.gitgud.backend.configs.RabbitMQConfig;
import com.syntaxllama.gitgud.backend.dtos.execution.*;
import com.syntaxllama.gitgud.backend.exceptions.BadRequestException;
import com.syntaxllama.gitgud.backend.exceptions.ResourceNotFoundException;
import com.syntaxllama.gitgud.backend.models.Lesson;
import com.syntaxllama.gitgud.backend.models.TestCase;
import com.syntaxllama.gitgud.backend.models.User;
import com.syntaxllama.gitgud.backend.repositories.LessonRepository;
import com.syntaxllama.gitgud.backend.repositories.TestCaseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Service for managing code execution jobs.
 * Handles job submission, status tracking, and result retrieval.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CodeExecutionService {

    private final RabbitTemplate rabbitTemplate;
    private final RedisTemplate<String, Object> redisTemplate;
    private final LessonRepository lessonRepository;
    private final TestCaseRepository testCaseRepository;

    private static final String RESULT_KEY_PREFIX = "execution:result:";
    private static final long RESULT_TTL_HOURS = 1;

    /**
     * Submit code for execution.
     * Creates a job, enqueues it to RabbitMQ, and stores initial status in Redis.
     */
    public CodeExecutionResponse submitCode(CodeExecutionRequest request, User user) {
        log.info("Submitting code execution for user {} and lesson {}", user.getId(), request.getLessonId());

        // Validate lesson exists
        Lesson lesson = lessonRepository.findById(request.getLessonId())
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found with id: " + request.getLessonId()));

        // Validate language
        if (!"java".equalsIgnoreCase(request.getLanguage())) {
            throw new BadRequestException("Unsupported language: " + request.getLanguage() + ". Only Java is currently supported.");
        }

        // Get test case IDs (use all if not specified)
        List<UUID> testCaseIds = request.getTestCaseIds();
        if (testCaseIds == null || testCaseIds.isEmpty()) {
            testCaseIds = testCaseRepository.findByLessonIdOrderByDisplayOrderAsc(request.getLessonId())
                    .stream()
                    .map(TestCase::getId)
                    .toList();

            if (testCaseIds.isEmpty()) {
                throw new BadRequestException("No test cases found for lesson: " + request.getLessonId());
            }
        }

        // Create job with unique ID
        String jobId = UUID.randomUUID().toString();
        CodeExecutionJob job = CodeExecutionJob.builder()
                .jobId(jobId)
                .userId(user.getId())
                .lessonId(request.getLessonId())
                .language(request.getLanguage().toLowerCase())
                .sourceCode(request.getSourceCode())
                .testCaseIds(testCaseIds)
                .submittedAt(LocalDateTime.now())
                .build();

        // Store initial status in Redis
        ExecutionResult initialResult = ExecutionResult.builder()
                .jobId(jobId)
                .status(ExecutionStatus.QUEUED)
                .startedAt(LocalDateTime.now())
                .build();
        storeResult(jobId, initialResult);

        // Send job to RabbitMQ queue
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.CODE_EXECUTION_EXCHANGE,
                    RabbitMQConfig.CODE_EXECUTION_ROUTING_KEY,
                    job
            );
            log.info("Job {} submitted to queue for user {}", jobId, user.getId());
        } catch (Exception e) {
            log.error("Failed to submit job {} to queue", jobId, e);
            throw new RuntimeException("Failed to submit code execution job", e);
        }

        return CodeExecutionResponse.builder()
                .jobId(jobId)
                .status(ExecutionStatus.QUEUED)
                .message("Code submitted successfully. Use job ID to check status.")
                .build();
    }

    /**
     * Get execution result by job ID.
     * Polls Redis for the current status and results.
     */
    public ExecutionResult getExecutionResult(String jobId) {
        log.debug("Retrieving execution result for job {}", jobId);

        String key = RESULT_KEY_PREFIX + jobId;
        Object result = redisTemplate.opsForValue().get(key);

        if (result == null) {
            throw new ResourceNotFoundException("No execution found with job ID: " + jobId);
        }

        return (ExecutionResult) result;
    }

    /**
     * Store execution result in Redis with TTL.
     */
    public void storeResult(String jobId, ExecutionResult result) {
        String key = RESULT_KEY_PREFIX + jobId;
        redisTemplate.opsForValue().set(key, result, RESULT_TTL_HOURS, TimeUnit.HOURS);
        log.debug("Stored result for job {} with status {}", jobId, result.getStatus());
    }

    /**
     * Update job status in Redis.
     */
    public void updateJobStatus(String jobId, ExecutionStatus status) {
        ExecutionResult result = getExecutionResult(jobId);
        result.setStatus(status);

        if (status == ExecutionStatus.RUNNING && result.getStartedAt() == null) {
            result.setStartedAt(LocalDateTime.now());
        }

        storeResult(jobId, result);
    }
}
