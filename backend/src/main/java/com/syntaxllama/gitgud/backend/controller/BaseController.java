package com.syntaxllama.gitgud.backend.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Base controller with API prefix.
 * All API controllers should extend this class.
 */
@RestController
@RequestMapping("/api/v1")
public abstract class BaseController {
}
