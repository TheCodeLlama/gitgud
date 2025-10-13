# Keycloak OAuth2 Provider Setup Guide

This guide explains how to configure Google and GitHub OAuth2 providers in Keycloak for GitGud.

## Overview

OAuth2 providers allow users to sign in using their existing Google or GitHub accounts instead of creating a new password. Keycloak handles all the OAuth2 flows, and our backend simply validates the JWT tokens.

## Prerequisites

- Keycloak is running (`docker-compose up -d keycloak`)
- You have Keycloak admin access (http://localhost:8180, admin/password)
- You can access Google Cloud Console and GitHub Developer Settings

---

## 1. Google OAuth2 Setup

### Step 1.1: Create Google OAuth2 Credentials

1. **Go to Google Cloud Console**: https://console.cloud.google.com/
2. **Create a new project** (or select existing):
   - Click "Select a project" → "New Project"
   - Name: "GitGud" (or your preference)
   - Click "Create"

3. **Enable Google+ API**:
   - Navigate to "APIs & Services" → "Library"
   - Search for "Google+ API"
   - Click "Enable"

4. **Create OAuth 2.0 Credentials**:
   - Go to "APIs & Services" → "Credentials"
   - Click "Create Credentials" → "OAuth client ID"
   - Configure consent screen if prompted:
     - User type: External
     - App name: GitGud
     - User support email: your email
     - Developer contact: your email
     - Save and continue through all screens

5. **Configure OAuth Client**:
   - Application type: "Web application"
   - Name: "GitGud - Keycloak"
   - Authorized redirect URIs:
     ```
     http://localhost:8180/realms/gitgud/broker/google/endpoint
     ```
   - Click "Create"

6. **Save Credentials**:
   - Copy the **Client ID** and **Client Secret**
   - Store them in your `.env` file:
     ```bash
     OAUTH_GOOGLE_CLIENT_ID=your-google-client-id-here
     OAUTH_GOOGLE_CLIENT_SECRET=your-google-client-secret-here
     ```

### Step 1.2: Configure Google Provider in Keycloak

**Option A: Via Admin Console (Recommended for first-time setup)**

1. Open Keycloak Admin Console: http://localhost:8180
2. Select the **GitGud** realm
3. Navigate to **Identity Providers** in the left menu
4. Click **Add provider** → **Google**
5. Configure:
   - **Alias**: `google` (must match)
   - **Enabled**: ON
   - **Trust Email**: ON
   - **Client ID**: Paste Google Client ID
   - **Client Secret**: Paste Google Client Secret
   - **First Login Flow**: first broker login (default)
6. Click **Save**

**Option B: Via Realm JSON (For version control)**

1. Open `docker/keycloak/realms/gitgud-realm.json`
2. Add Google provider to the `identityProviders` array:
   ```json
   {
     "alias": "google",
     "providerId": "google",
     "enabled": true,
     "updateProfileFirstLoginMode": "on",
     "trustEmail": true,
     "storeToken": false,
     "addReadTokenRoleOnCreate": false,
     "authenticateByDefault": false,
     "linkOnly": false,
     "firstBrokerLoginFlowAlias": "first broker login",
     "config": {
       "clientId": "${env.OAUTH_GOOGLE_CLIENT_ID}",
       "clientSecret": "${env.OAUTH_GOOGLE_CLIENT_SECRET}",
       "defaultScope": "openid profile email"
     }
   }
   ```
3. Restart Keycloak: `docker-compose restart keycloak`

---

## 2. GitHub OAuth2 Setup

### Step 2.1: Create GitHub OAuth App

1. **Go to GitHub Developer Settings**: https://github.com/settings/developers
2. **Create OAuth App**:
   - Click "New OAuth App"
   - **Application name**: GitGud
   - **Homepage URL**: `http://localhost:5173`
   - **Authorization callback URL**:
     ```
     http://localhost:8180/realms/gitgud/broker/github/endpoint
     ```
   - Click "Register application"

3. **Generate Client Secret**:
   - After creation, click "Generate a new client secret"
   - Copy the secret immediately (it won't be shown again)

4. **Save Credentials**:
   - Copy the **Client ID** and **Client Secret**
   - Store them in your `.env` file:
     ```bash
     OAUTH_GITHUB_CLIENT_ID=your-github-client-id-here
     OAUTH_GITHUB_CLIENT_SECRET=your-github-client-secret-here
     ```

### Step 2.2: Configure GitHub Provider in Keycloak

**Option A: Via Admin Console**

1. Open Keycloak Admin Console: http://localhost:8180
2. Select the **GitGud** realm
3. Navigate to **Identity Providers**
4. Click **Add provider** → **GitHub**
5. Configure:
   - **Alias**: `github` (must match)
   - **Enabled**: ON
   - **Trust Email**: ON
   - **Client ID**: Paste GitHub Client ID
   - **Client Secret**: Paste GitHub Client Secret
   - **First Login Flow**: first broker login (default)
6. Click **Save**

**Option B: Via Realm JSON**

1. Open `docker/keycloak/realms/gitgud-realm.json`
2. Add GitHub provider to the `identityProviders` array:
   ```json
   {
     "alias": "github",
     "providerId": "github",
     "enabled": true,
     "updateProfileFirstLoginMode": "on",
     "trustEmail": true,
     "storeToken": false,
     "addReadTokenRoleOnCreate": false,
     "authenticateByDefault": false,
     "linkOnly": false,
     "firstBrokerLoginFlowAlias": "first broker login",
     "config": {
       "clientId": "${env.OAUTH_GITHUB_CLIENT_ID}",
       "clientSecret": "${env.OAUTH_GITHUB_CLIENT_SECRET}",
       "defaultScope": "read:user user:email"
     }
   }
   ```
3. Restart Keycloak: `docker-compose restart keycloak`

---

## 3. Testing OAuth2 Login

### Test Google Login

1. Start Keycloak: `docker-compose up -d keycloak`
2. Go to the Keycloak account console: http://localhost:8180/realms/gitgud/account
3. You should see a "Sign in with Google" button
4. Click it and sign in with your Google account
5. After successful login, you'll be redirected to the account page

### Test GitHub Login

1. Same as Google, but click "Sign in with GitHub"
2. Authorize the GitGud application
3. You'll be redirected back after successful login

### Test in Frontend (Once implemented)

The frontend will use the Keycloak JS adapter to initiate OAuth2 flows. Users will see:
- "Sign in with Google" button → Redirects to Google → Returns with JWT
- "Sign in with GitHub" button → Redirects to GitHub → Returns with JWT

---

## 4. Updating Production URLs

When deploying to production, you'll need to update the redirect URIs:

### Google Cloud Console
1. Go to your OAuth2 credentials
2. Add production redirect URI:
   ```
   https://your-domain.com/realms/gitgud/broker/google/endpoint
   ```

### GitHub OAuth App
1. Go to your OAuth App settings
2. Add production callback URL:
   ```
   https://your-domain.com/realms/gitgud/broker/github/endpoint
   ```

### Keycloak Realm Configuration
- Update the `docker/keycloak/realms/gitgud-realm.json` if using JSON import
- Or update in the Keycloak admin console and export the updated realm

---

## 5. Troubleshooting

### "Invalid redirect_uri" error
- Check that the redirect URI in Google/GitHub matches exactly:
  ```
  http://localhost:8180/realms/gitgud/broker/{provider}/endpoint
  ```
- No trailing slashes
- Correct realm name (gitgud)
- Correct provider alias (google or github)

### "User not found" error in backend
- The user needs to hit `/api/auth/sync` after OAuth login
- The frontend should automatically call this endpoint after successful Keycloak login
- Check that JWT token contains `sub`, `email`, and `preferred_username` claims

### Email not provided by OAuth provider
- Some users may not have email verified or public
- Google: Usually provides email if user consents
- GitHub: User must have a public email or grant email scope

### Keycloak providers not showing
- Verify the provider is enabled in Keycloak admin console
- Check Keycloak logs: `docker-compose logs keycloak`
- Restart Keycloak: `docker-compose restart keycloak`

---

## 6. Security Best Practices

1. **Never commit OAuth secrets to Git**
   - Use environment variables
   - Add `.env` to `.gitignore`
   - Use secrets managers in production (AWS Secrets Manager, etc.)

2. **Use HTTPS in production**
   - OAuth providers require HTTPS for redirect URIs (except localhost)
   - Configure SSL certificates for your domain

3. **Limit OAuth scopes**
   - Only request the minimum scopes needed (profile, email)
   - Don't request write permissions unless necessary

4. **Rotate secrets regularly**
   - Change OAuth client secrets periodically
   - Update them in both the provider console and Keycloak

5. **Monitor OAuth usage**
   - Check Keycloak logs for failed login attempts
   - Monitor for unusual patterns

---

## 7. Next Steps

After setting up OAuth2 providers:

1. **Export Keycloak realm configuration**:
   ```bash
   ./scripts/keycloak-export.sh
   ```

2. **Commit the updated realm**:
   ```bash
   git add docker/keycloak/realms/gitgud-realm.json
   git commit -m "Add Google and GitHub OAuth providers"
   ```

3. **Update documentation** with your specific OAuth app URLs

4. **Test end-to-end** with the React frontend once implemented

---

## Additional Resources

- [Keycloak Identity Broker Documentation](https://www.keycloak.org/docs/latest/server_admin/#_identity_broker)
- [Google OAuth 2.0 Documentation](https://developers.google.com/identity/protocols/oauth2)
- [GitHub OAuth Apps Documentation](https://docs.github.com/en/developers/apps/building-oauth-apps)
