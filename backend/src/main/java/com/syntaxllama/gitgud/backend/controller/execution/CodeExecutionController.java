package com.syntaxllama.gitgud.backend.controller.execution;

import com.syntaxllama.gitgud.backend.controller.BaseController;
import com.syntaxllama.gitgud.backend.dto.ApiResponse;
import com.syntaxllama.gitgud.backend.dto.execution.CodeExecutionRequest;
import com.syntaxllama.gitgud.backend.dto.execution.CodeExecutionResponse;
import com.syntaxllama.gitgud.backend.dto.execution.ExecutionResult;
import com.syntaxllama.gitgud.backend.security.AuthenticationUtil;
import com.syntaxllama.gitgud.backend.service.execution.CodeExecutionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for code execution operations.
 * Handles code submission, execution in sandboxed containers, and result retrieval.
 *
 * This API provides asynchronous code execution with the following workflow:
 * 1. Submit code via POST /run (returns job ID)
 * 2. Poll GET /result/{jobId} to check status
 * 3. When status is COMPLETED or FAILED, retrieve final results
 *
 * Security: All code is executed in hardened Docker containers with:
 * - Network isolation (no internet access)
 * - Memory limits (256MB)
 * - CPU limits (0.5 cores)
 * - 5-second timeout
 * - Read-only filesystem
 * - No elevated privileges
 */
@RestController
@RequestMapping("/api/v1/execute")
@RequiredArgsConstructor
@Slf4j
public class CodeExecutionController extends BaseController {

    private final CodeExecutionService executionService;
    private final AuthenticationUtil authenticationUtil;

    /**
     * Submit code for execution.
     * Creates a job, enqueues it to RabbitMQ, and returns a job ID for polling.
     *
     * POST /api/v1/execute/run
     *
     * Request body:
     * {
     *   "language": "java",
     *   "sourceCode": "public class Main { ... }",
     *   "lessonId": "uuid",
     *   "testCaseIds": ["uuid1", "uuid2"] // optional
     * }
     *
     * Response:
     * {
     *   "success": true,
     *   "data": {
     *     "jobId": "job-uuid",
     *     "status": "QUEUED",
     *     "message": "Code submitted successfully"
     *   }
     * }
     *
     * Validation:
     * - Source code must not exceed 50,000 characters
     * - Language must be "java" (only supported language)
     * - Lesson ID must be valid UUID
     *
     * @param request Code execution request with source code, lesson ID, and test case IDs
     * @return Job ID and initial status
     */
    @PostMapping("/run")
    public ResponseEntity<ApiResponse<CodeExecutionResponse>> submitCode(
            @Valid @RequestBody CodeExecutionRequest request) {

        log.info("Received code execution request for lesson {}", request.getLessonId());

        CodeExecutionResponse response = executionService.submitCode(request, authenticationUtil.getCurrentUser());

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get execution results by job ID.
     * Poll this endpoint to check the status and results of a submitted job.
     *
     * GET /api/v1/execute/result/{jobId}
     *
     * Response:
     * {
     *   "success": true,
     *   "data": {
     *     "jobId": "job-uuid",
     *     "status": "COMPLETED",  // QUEUED, RUNNING, COMPLETED, FAILED
     *     "passed": true,
     *     "testsPassed": 5,
     *     "totalTests": 5,
     *     "testCaseResults": [...],
     *     "executionTimeMs": 1234,
     *     "xpAwarded": 50,
     *     "startedAt": "2025-10-13T10:00:00",
     *     "completedAt": "2025-10-13T10:00:01"
     *   }
     * }
     *
     * Polling Strategy:
     * - Poll every 1-2 seconds while status is QUEUED or RUNNING
     * - Stop polling when status is COMPLETED or FAILED
     * - Results are cached in Redis for 1 hour after completion
     *
     * @param jobId Unique job ID returned from /run endpoint
     * @return Execution result with status, test case results, and XP awarded
     */
    @GetMapping("/result/{jobId}")
    public ResponseEntity<ApiResponse<ExecutionResult>> getResult(@PathVariable String jobId) {
        log.debug("Fetching result for job {}", jobId);

        ExecutionResult result = executionService.getExecutionResult(jobId);

        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
