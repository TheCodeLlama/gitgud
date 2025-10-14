# Keycloak Frontend Client Configuration

This document describes how to configure the Keycloak frontend client for GitGud with **direct authentication** (no redirects to Keycloak login page).

## Authentication Approach

GitGud uses a hybrid authentication approach:
- **Username/Password**: Direct authentication using Keycloak's Resource Owner Password Credentials flow (no redirect)
- **Google/GitHub OAuth**: Redirects to provider for authentication, then back to app

This provides a seamless user experience where users stay within the GitGud app for email/password authentication.

## Prerequisites

- Keycloak running at `http://localhost:8180`
- GitGud realm already created
- Backend client (`gitgud-backend`) already configured

## Frontend Client Configuration

### 1. Access Keycloak Admin Console

1. Navigate to http://localhost:8180
2. Click "Administration Console"
3. Login with credentials:
   - Username: `admin`
   - Password: `password`

### 2. Create Frontend Client

1. Select the `gitgud` realm from the dropdown
2. Navigate to **Clients** in the left sidebar
3. Click **Create client** button
4. Configure the client:

   **General Settings:**
   - Client ID: `gitgud-frontend`
   - Name: `GitGud Frontend`
   - Description: `React SPA frontend for GitGud platform`
   - Always display in UI: `OFF`
   - Enabled: `ON`
   - Click **Next**

   **Capability config:**
   - Client authentication: `OFF` (this is a public client)
   - Authorization: `OFF`
   - Authentication flow:
     - ✅ Standard flow (Authorization Code Flow) - Required for OAuth
     - ✅ **Direct access grants** - **REQUIRED** for username/password login
     - ❌ Implicit flow (deprecated)
     - ❌ Service accounts roles
     - ❌ OAuth 2.0 Device Authorization Grant
   - Click **Next**

   **IMPORTANT:** `Direct access grants` MUST be enabled for the custom sign-in page to work!

   **Login settings:**
   - Root URL: `http://localhost:5173`
   - Home URL: `http://localhost:5173`
   - Valid redirect URIs:
     - `http://localhost:5173/*`
   - Valid post logout redirect URIs:
     - `http://localhost:5173/*`
   - Web origins:
     - `http://localhost:5173`
     - `+` (this allows all origins from redirect URIs)
   - Admin URL: (leave empty)
   - Click **Save**

### 3. Additional Client Settings

After creating the client, configure these additional settings:

1. Go to **Clients** > **gitgud-frontend** > **Settings** tab

2. **Access settings:**
   - Access Token Lifespan: `5 minutes` (default)
   - Client Session Idle: `30 minutes` (default)
   - Client Session Max: `10 hours` (default)

3. **Advanced settings:**
   - Proof Key for Code Exchange (PKCE):
     - PKCE Code Challenge Method: `S256` (required for public clients)
   - Frontend URL: (leave empty for now)
   - Save changes

4. Go to **Client scopes** tab:
   - Verify that `profile`, `email`, `roles`, and `web-origins` are assigned

### 4. Configure Identity Providers (Optional - Google & GitHub)

If you want to enable social login:

#### Google OAuth Setup:

1. Go to **Identity providers** in left sidebar
2. Click **Add provider** > **Google**
3. Enter Google OAuth credentials:
   - Client ID: (from Google Cloud Console)
   - Client Secret: (from Google Cloud Console)
4. Save

For Google OAuth credentials:
- Go to https://console.cloud.google.com/
- Create a project or select existing
- Enable Google+ API
- Create OAuth 2.0 credentials
- Add authorized redirect URI: `http://localhost:8180/realms/gitgud/broker/google/endpoint`

#### GitHub OAuth Setup:

1. Go to **Identity providers** in left sidebar
2. Click **Add provider** > **GitHub**
3. Enter GitHub OAuth credentials:
   - Client ID: (from GitHub OAuth Apps)
   - Client Secret: (from GitHub OAuth Apps)
4. Save

For GitHub OAuth credentials:
- Go to https://github.com/settings/developers
- Create New OAuth App
- Application name: `GitGud Local Dev`
- Homepage URL: `http://localhost:5173`
- Authorization callback URL: `http://localhost:8180/realms/gitgud/broker/github/endpoint`

### 5. Test User Creation (Optional)

Create a test user for development:

1. Go to **Users** in left sidebar
2. Click **Add user**
3. Fill in details:
   - Username: `testuser`
   - Email: `test@gitgud.com`
   - First name: `Test`
   - Last name: `User`
   - Email verified: `ON`
   - Enabled: `ON`
4. Click **Create**
5. Go to **Credentials** tab
6. Click **Set password**
   - Password: `password`
   - Temporary: `OFF`
7. Click **Save password**

## Testing the Integration

### 1. Start All Services

```bash
# Terminal 1: Start infrastructure
docker-compose up -d

# Terminal 2: Start backend
./backend/mvnw spring-boot:run

# Terminal 3: Start frontend
cd frontend && npm run dev
```

### 2. Test Authentication Flow

#### Email/Password Sign In (Direct):
1. Navigate to `http://localhost:5173/signin`
2. Enter test user credentials:
   - Email/Username: `testuser` or `test@gitgud.com`
   - Password: `password`
3. Click "Sign In"
4. You should be logged in and redirected to dashboard WITHOUT leaving the app
5. Verify user info is displayed
6. Click "Sign Out"

#### Sign Up (Registration):
1. Navigate to `http://localhost:5173/signup`
2. Fill in the registration form
3. Click "Create Account"
4. Backend should create user in Keycloak and auto-login
5. You should be redirected to dashboard

#### OAuth Sign In (Google/GitHub):
1. Navigate to `http://localhost:5173/signin`
2. Click "Continue with Google" or "Continue with GitHub"
3. You will be redirected to the OAuth provider
4. After authentication, you'll be redirected back to dashboard
5. Verify user info is displayed

### 3. Test Protected Routes

1. While logged out, try to navigate to `http://localhost:5173/dashboard`
2. You should be redirected to `/signin`
3. After logging in, you should be able to access the dashboard

## Troubleshooting

### Issue: "Invalid redirect URI"

**Solution:** Check that the frontend URL is properly configured in Keycloak:
- Verify `http://localhost:5173/*` is in Valid redirect URIs
- Verify `http://localhost:5173` is in Web origins

### Issue: CORS errors

**Solution:**
- Verify Web origins includes `http://localhost:5173` or `+`
- Check backend CORS configuration in Spring Boot

### Issue: "Failed to initialize Keycloak"

**Solution:**
- Verify Keycloak is running: `docker-compose ps`
- Check Keycloak URL in `.env`: `VITE_KEYCLOAK_URL=http://localhost:8180`
- Verify realm name: `VITE_KEYCLOAK_REALM=gitgud`
- Verify client ID: `VITE_KEYCLOAK_CLIENT_ID=gitgud-frontend`

### Issue: Token not being sent to backend

**Solution:**
- Check browser dev tools > Network tab
- Verify `Authorization: Bearer <token>` header is present in API requests
- Check AuthContext is properly setting the token

### Issue: "Client not found" error

**Solution:**
- Verify the client `gitgud-frontend` exists in Keycloak
- Verify client ID matches exactly in `.env` file
- Restart frontend after changing .env

### Issue: "Invalid grant" or "Unauthorized" on login

**Solution:**
- Verify "Direct access grants" is enabled in Keycloak client settings
- Go to Clients > gitgud-frontend > Settings > Capability config
- Ensure "Direct access grants" is checked
- Save and try again

### Issue: Registration fails with "User already exists"

**Solution:**
- This username/email is already registered
- Try a different username/email
- Or use the existing credentials to sign in

## Security Notes

**For Production:**

1. **HTTPS Required:**
   - Keycloak should be served over HTTPS
   - Frontend should be served over HTTPS
   - Update all URLs to use `https://`

2. **Client Configuration:**
   - Set proper redirect URIs (no wildcards)
   - Restrict Web origins to specific domains
   - Set appropriate token lifespans

3. **Environment Variables:**
   - Never commit `.env` with production credentials
   - Use environment-specific configuration
   - Rotate secrets regularly

4. **OAuth Providers:**
   - Use production OAuth credentials
   - Update callback URLs to production domains
   - Follow each provider's security best practices

## Additional Resources

- [Keycloak Documentation](https://www.keycloak.org/documentation)
- [Keycloak JavaScript Adapter](https://www.keycloak.org/docs/latest/securing_apps/#_javascript_adapter)
- [OAuth 2.0 PKCE](https://oauth.net/2/pkce/)
