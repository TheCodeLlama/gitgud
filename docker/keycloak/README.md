# Keycloak Configuration Management

This directory contains Keycloak realm configuration that is version-controlled and automatically imported on container startup.

## Overview

**Problem**: Keycloak configuration stored in Docker volumes is lost when volumes are reset.

**Solution**: Store realm configuration as JSON files in version control and automatically import them on Keycloak startup.

**Benefits**:
- ✅ Configuration survives `docker-compose down -v`
- ✅ Version-controlled (track changes over time)
- ✅ Portable to any environment (dev, staging, prod, cloud)
- ✅ Repeatable deployments with identical configuration
- ✅ Easy to share with team members

## Directory Structure

```
docker/keycloak/
├── README.md                      # This file
├── init-keycloak-db.sql           # PostgreSQL database initialization
└── realms/
    └── gitgud-realm.json          # GitGud realm configuration (auto-imported)
```

## How It Works

1. **Startup**: When Keycloak container starts, it runs with `--import-realm` flag
2. **Import**: Keycloak automatically imports all JSON files from `/opt/keycloak/data/import/` directory
3. **Mount**: Docker Compose mounts `./docker/keycloak/realms` to `/opt/keycloak/data/import`
4. **Idempotent**: Realm import is idempotent - it won't overwrite existing realms, only create missing ones

## Initial Setup

The GitGud realm (`gitgud-realm.json`) includes:

### Clients
1. **gitgud-backend** - Spring Boot resource server (bearer-only)
2. **gitgud-frontend** - React SPA (public client with PKCE)

### Realm Roles
- `user` - Standard user (default role)
- `admin` - Administrator
- `instructor` - Lesson creator/manager

### Security Settings
- Registration enabled
- Email as username
- Brute force protection enabled (5 failed attempts = 15min lockout)
- Access token lifespan: 5 minutes
- SSO session idle: 30 minutes
- SSO session max: 10 hours

## Making Configuration Changes

### Option 1: Edit Realm in Admin Console (Recommended for Complex Changes)

1. **Make changes in Keycloak Admin Console**:
   ```
   http://localhost:8180
   Login: admin / password (from .env)
   ```

2. **Export the realm**:
   ```bash
   # Export realm configuration
   docker exec -it gitgud-keycloak /opt/keycloak/bin/kc.sh export \
     --dir /tmp/export \
     --realm gitgud \
     --users skip

   # Copy exported realm to host
   docker cp gitgud-keycloak:/tmp/export/gitgud-realm.json ./docker/keycloak/realms/
   ```

3. **Clean up exported file** (optional but recommended):
   - Remove sensitive data (client secrets, if any)
   - Remove auto-generated IDs for better diffs
   - Format JSON for readability

4. **Commit changes**:
   ```bash
   git add docker/keycloak/realms/gitgud-realm.json
   git commit -m "Update Keycloak realm configuration"
   ```

### Option 2: Edit JSON Directly (Recommended for Simple Changes)

1. **Edit** `docker/keycloak/realms/gitgud-realm.json` directly
2. **Restart Keycloak** to apply changes:
   ```bash
   docker-compose restart keycloak
   ```
3. **Commit changes**:
   ```bash
   git add docker/keycloak/realms/gitgud-realm.json
   git commit -m "Add new client configuration"
   ```

## Adding OAuth Identity Providers

### Google OAuth2

1. **Get credentials** from [Google Cloud Console](https://console.cloud.google.com/):
   - Create OAuth 2.0 Client ID
   - Add authorized redirect URI: `http://localhost:8180/realms/gitgud/broker/google/endpoint`

2. **Add to realm JSON** under `identityProviders`:
   ```json
   {
     "alias": "google",
     "providerId": "google",
     "enabled": true,
     "trustEmail": true,
     "firstBrokerLoginFlowAlias": "first broker login",
     "config": {
       "clientId": "YOUR_GOOGLE_CLIENT_ID",
       "clientSecret": "YOUR_GOOGLE_CLIENT_SECRET",
       "hostedDomain": ""
     }
   }
   ```

3. **Never commit secrets** - Use environment variables:
   ```json
   "clientSecret": "${env.GOOGLE_OAUTH_CLIENT_SECRET}"
   ```

### GitHub OAuth2

1. **Get credentials** from [GitHub Developer Settings](https://github.com/settings/developers):
   - Create OAuth App
   - Authorization callback URL: `http://localhost:8180/realms/gitgud/broker/github/endpoint`

2. **Add to realm JSON** under `identityProviders`:
   ```json
   {
     "alias": "github",
     "providerId": "github",
     "enabled": true,
     "trustEmail": true,
     "firstBrokerLoginFlowAlias": "first broker login",
     "config": {
       "clientId": "YOUR_GITHUB_CLIENT_ID",
       "clientSecret": "YOUR_GITHUB_CLIENT_SECRET"
     }
   }
   ```

## Environment-Specific Configuration

### Development (Current)
- Realm file: `docker/keycloak/realms/gitgud-realm.json`
- Redirect URIs: `http://localhost:5173/*`
- Keycloak URL: `http://localhost:8180`

### Staging/Production
When deploying to cloud:

1. **Update redirect URIs** in realm JSON:
   ```json
   "redirectUris": [
     "https://staging.gitgud.com/*",
     "https://staging.gitgud.com/auth/callback"
   ]
   ```

2. **Update web origins**:
   ```json
   "webOrigins": ["https://staging.gitgud.com"]
   ```

3. **Use environment variables** for secrets:
   - Store OAuth secrets in AWS Secrets Manager / Parameter Store
   - Reference with `${env.VARIABLE_NAME}` syntax in realm JSON
   - Set environment variables in ECS/EC2 configuration

4. **SSL Required**:
   ```json
   "sslRequired": "external"  // or "all" for production
   ```

## Resetting Configuration

If you want to completely reset Keycloak:

```bash
# Stop containers and remove volumes
docker-compose down -v

# Start fresh - Keycloak will auto-import realm
docker-compose up -d keycloak

# Check import logs
docker-compose logs -f keycloak
```

## Backup Strategy

### Version Control (Primary)
- All configuration is in Git
- Use meaningful commit messages
- Tag releases: `git tag v1.0.0-keycloak-config`

### Database Backups (Secondary)
For user data (not configuration):
```bash
# Backup Keycloak database
docker exec gitgud-keycloak-postgres pg_dump -U keycloak_admin keycloak > keycloak-backup.sql

# Restore
docker exec -i gitgud-keycloak-postgres psql -U keycloak_admin keycloak < keycloak-backup.sql
```

## Troubleshooting

### Realm not imported
**Check logs**:
```bash
docker-compose logs keycloak | grep -i import
```

**Verify mount**:
```bash
docker exec -it gitgud-keycloak ls -la /opt/keycloak/data/import
```

### Configuration not updating
**Issue**: Realm import won't overwrite existing realms

**Solution**:
1. Delete the realm from Keycloak Admin Console, OR
2. Use partial import feature (Admin Console → Realm Settings → Action → Partial Import)

### Invalid JSON
**Issue**: Keycloak fails to start

**Solution**:
1. Validate JSON: `jq . docker/keycloak/realms/gitgud-realm.json`
2. Check Keycloak logs for specific error
3. Restore from Git history if needed

## Cloud Deployment Checklist

When moving to AWS/GCP/Azure:

- [ ] Update redirect URIs to production domains
- [ ] Change `sslRequired` to `"external"` or `"all"`
- [ ] Use managed secrets service for OAuth credentials
- [ ] Update Keycloak hostname to production URL
- [ ] Configure HTTPS/TLS certificates
- [ ] Set up database backups (RDS automated backups)
- [ ] Configure log aggregation (CloudWatch, Stackdriver, etc.)
- [ ] Update CORS origins in Spring Boot backend
- [ ] Test OAuth flows on production URLs
- [ ] Set up monitoring and alerts

## Best Practices

1. **Never commit secrets** - Use environment variables or secrets managers
2. **Skip user export** - User data should come from database backups, not realm exports
3. **Review diffs** - Before committing realm changes, review the git diff
4. **Test imports** - After making changes, test with `docker-compose down -v && docker-compose up -d`
5. **Document changes** - Add comments in commit messages explaining configuration changes
6. **Use branches** - Test major Keycloak changes in feature branches first

## Additional Resources

- [Keycloak Documentation](https://www.keycloak.org/documentation)
- [Keycloak Server Administration](https://www.keycloak.org/docs/latest/server_admin/)
- [Keycloak Docker Guide](https://www.keycloak.org/server/containers)
- [Realm Export/Import](https://www.keycloak.org/docs/latest/server_admin/#_export_import)
