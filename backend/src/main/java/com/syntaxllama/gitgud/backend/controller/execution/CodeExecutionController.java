package com.syntaxllama.gitgud.backend.controller.execution;

import com.syntaxllama.gitgud.backend.controller.BaseController;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for code execution operations.
 * Handles code submission, execution in sandboxed containers, and result retrieval.
 */
@RestController
@RequestMapping("/execute")
@RequiredArgsConstructor
@Slf4j
public class CodeExecutionController extends BaseController {

    // TODO: Implement endpoints:
    // POST /api/v1/execute/run - Submit code for execution
    // GET /api/v1/execute/result/{jobId} - Poll for execution results
}
