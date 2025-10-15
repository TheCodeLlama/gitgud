package com.syntaxllama.gitgud.backend.configs;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Firebase Authentication Filter.
 *
 * This filter runs on every HTTP request to validate Firebase ID tokens.
 * It extracts the Bearer token from the Authorization header, verifies it with Firebase,
 * and sets the authentication in the Spring Security context.
 *
 * Token validation includes:
 * - Signature verification
 * - Expiration check
 * - Issuer check
 * - Audience check
 */
@Component
@Slf4j
public class FirebaseAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    /**
     * Process each request to extract and validate Firebase ID token.
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            // Extract token from Authorization header
            String token = extractToken(request);

            if (token != null) {
                // Verify Firebase ID token
                FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(token);

                // Extract user information
                String uid = decodedToken.getUid();
                String email = decodedToken.getEmail();
                String name = decodedToken.getName();
                Map<String, Object> claims = decodedToken.getClaims();

                // Extract roles from custom claims (if set via Firebase Admin SDK)
                List<SimpleGrantedAuthority> authorities = extractAuthorities(claims);

                // Create authentication token
                FirebaseAuthenticationToken authToken = new FirebaseAuthenticationToken(
                        uid,
                        email,
                        name,
                        claims,
                        authorities
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Set authentication in security context
                SecurityContextHolder.getContext().setAuthentication(authToken);

                log.debug("Firebase authentication successful for user: {} ({})", email, uid);
            }

        } catch (FirebaseAuthException e) {
            log.warn("Firebase token validation failed: {}", e.getMessage());
            // Clear security context on authentication failure
            SecurityContextHolder.clearContext();
        } catch (Exception e) {
            log.error("Unexpected error during Firebase authentication", e);
            SecurityContextHolder.clearContext();
        }

        // Continue filter chain
        filterChain.doFilter(request, response);
    }

    /**
     * Extract Bearer token from Authorization header.
     * Returns null if header is missing or doesn't start with "Bearer ".
     */
    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader(AUTHORIZATION_HEADER);

        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            return authHeader.substring(BEARER_PREFIX.length());
        }

        return null;
    }

    /**
     * Extract authorities/roles from Firebase custom claims.
     * Firebase allows setting custom claims like { "role": "admin", "roles": ["user", "moderator"] }
     *
     * This method looks for:
     * 1. "role" claim (single role string)
     * 2. "roles" claim (array of role strings)
     */
    @SuppressWarnings("unchecked")
    private List<SimpleGrantedAuthority> extractAuthorities(Map<String, Object> claims) {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();

        // Default role for all authenticated users
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));

        // Extract single role claim
        if (claims.containsKey("role")) {
            String role = (String) claims.get("role");
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));
        }

        // Extract roles array claim
        if (claims.containsKey("roles")) {
            Object rolesObj = claims.get("roles");
            if (rolesObj instanceof List) {
                List<String> roles = (List<String>) rolesObj;
                roles.forEach(role ->
                        authorities.add(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                );
            }
        }

        return authorities;
    }

    /**
     * Custom Authentication Token that holds Firebase-specific information.
     * Extends UsernamePasswordAuthenticationToken for compatibility with Spring Security.
     */
    public static class FirebaseAuthenticationToken extends UsernamePasswordAuthenticationToken {
        private final String uid;
        private final String email;
        private final String name;
        private final Map<String, Object> claims;

        public FirebaseAuthenticationToken(
                String uid,
                String email,
                String name,
                Map<String, Object> claims,
                List<SimpleGrantedAuthority> authorities
        ) {
            super(email, null, authorities);
            this.uid = uid;
            this.email = email;
            this.name = name;
            this.claims = claims;
            // Token is automatically authenticated when authorities are provided to super()
        }

        public String getUid() {
            return uid;
        }

        public String getEmail() {
            return email;
        }

        public String getName() {
            return name;
        }

        public Map<String, Object> getClaims() {
            return claims;
        }
    }
}
