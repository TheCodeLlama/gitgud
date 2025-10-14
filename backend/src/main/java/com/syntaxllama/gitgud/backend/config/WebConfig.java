package com.syntaxllama.gitgud.backend.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web configuration for CORS, rate limiting, and other MVC settings.
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final RateLimitInterceptor rateLimitInterceptor;

    /**
     * Configure CORS to allow requests from React frontend during development.
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:5173") // React dev server
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    /**
     * Register rate limiting interceptor for all API endpoints.
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/actuator/**");
    }

    /**
     * Disable default resource handlers to prevent /** pattern from intercepting API requests.
     * This is a REST API backend - we don't serve static content.
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Intentionally empty - disables all default resource handlers including /**
        // This prevents Spring from treating /api/v1/auth/register as a static resource request
    }
}
