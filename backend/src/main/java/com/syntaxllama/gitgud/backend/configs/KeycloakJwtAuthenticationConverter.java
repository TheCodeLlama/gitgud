package com.syntaxllama.gitgud.backend.configs;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Custom JWT Authentication Converter for Keycloak JWTs.
 *
 * Keycloak stores roles in a nested structure:
 * {
 *   "realm_access": {
 *     "roles": ["user", "admin"]
 *   },
 *   "resource_access": {
 *     "gitgud-backend": {
 *       "roles": ["backend-role"]
 *     }
 *   }
 * }
 *
 * This converter extracts both realm and resource roles and converts them to Spring Security authorities.
 */
public class KeycloakJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final JwtGrantedAuthoritiesConverter defaultGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Collection<GrantedAuthority> authorities = Stream.concat(
                defaultGrantedAuthoritiesConverter.convert(jwt).stream(),
                extractResourceRoles(jwt).stream()
        ).collect(Collectors.toSet());

        return new JwtAuthenticationToken(jwt, authorities);
    }

    /**
     * Extract roles from Keycloak's realm_access and resource_access claims.
     */
    private Collection<GrantedAuthority> extractResourceRoles(Jwt jwt) {
        // Extract realm roles
        Collection<GrantedAuthority> realmRoles = extractRealmRoles(jwt);

        // Extract resource roles (optional, if needed)
        // Collection<GrantedAuthority> resourceRoles = extractClientRoles(jwt, "gitgud-backend");

        return realmRoles;
    }

    /**
     * Extract roles from realm_access.roles claim.
     */
    @SuppressWarnings("unchecked")
    private Collection<GrantedAuthority> extractRealmRoles(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess == null || realmAccess.get("roles") == null) {
            return List.of();
        }

        Collection<String> roles = (Collection<String>) realmAccess.get("roles");
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                .collect(Collectors.toList());
    }

    /**
     * Extract roles from resource_access.{clientId}.roles claim.
     * Uncomment and use if you need client-specific roles.
     */
    @SuppressWarnings("unchecked")
    private Collection<GrantedAuthority> extractClientRoles(Jwt jwt, String clientId) {
        Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
        if (resourceAccess == null) {
            return List.of();
        }

        Map<String, Object> clientAccess = (Map<String, Object>) resourceAccess.get(clientId);
        if (clientAccess == null || clientAccess.get("roles") == null) {
            return List.of();
        }

        Collection<String> roles = (Collection<String>) clientAccess.get("roles");
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                .collect(Collectors.toList());
    }
}
