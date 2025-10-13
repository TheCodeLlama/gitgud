package com.syntaxllama.gitgud.backend.controller.execution;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for code execution operations.
 * Handles code submission, execution in sandboxed containers, and result retrieval.
 */
@RestController
@RequestMapping("/api/execute")
@RequiredArgsConstructor
@Slf4j
public class CodeExecutionController {

    // TODO: Implement endpoints:
    // POST /api/execute/run - Submit code for execution
    // GET /api/execute/result/{jobId} - Poll for execution results
}
