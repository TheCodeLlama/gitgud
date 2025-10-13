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
 */
@RestController
@RequestMapping("/execute")
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
