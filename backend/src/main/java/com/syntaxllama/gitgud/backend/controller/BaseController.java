package com.syntaxllama.gitgud.backend.controller;

import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Base controller with API versioning prefix.
 * All API controllers should extend this class.
 */
@RequestMapping("/api/v1")
public abstract class BaseController {
}
