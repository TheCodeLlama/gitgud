/**
 * Keycloak configuration for GitGud frontend
 */
const keycloakConfig = {
  url: import.meta.env.VITE_KEYCLOAK_URL || 'http://localhost:8180',
  realm: import.meta.env.VITE_KEYCLOAK_REALM || 'gitgud',
  clientId: import.meta.env.VITE_KEYCLOAK_CLIENT_ID || 'gitgud-frontend',
};

export default keycloakConfig;
