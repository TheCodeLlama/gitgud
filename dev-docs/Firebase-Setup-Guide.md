# Firebase Authentication Setup Guide

This guide will walk you through setting up Firebase Authentication for the GitGud project and obtaining the necessary credentials.

## Prerequisites

- A Google account
- Access to the [Firebase Console](https://console.firebase.google.com/)

## Step 1: Create a Firebase Project

1. Navigate to the [Firebase Console](https://console.firebase.google.com/)
2. Click **"Add project"** or **"Create a project"**
3. Enter project name: `gitgud` (or your preferred name)
4. Optional: Disable Google Analytics (not needed for MVP)
5. Click **"Create project"** and wait for initialization

## Step 2: Register Your Web App

1. In your Firebase project, click the **web icon** (`</>`) to add a web app
2. Register app with nickname: `GitGud Frontend`
3. **DO NOT** check "Also set up Firebase Hosting"
4. Click **"Register app"**
5. You'll see your Firebase config object - **save these values** (we'll use them later):
   ```javascript
   {
     apiKey: "AIza...",
     authDomain: "your-project.firebaseapp.com",
     projectId: "your-project-id",
     storageBucket: "your-project.appspot.com",
     messagingSenderId: "123456789",
     appId: "1:123456789:web:abcdef"
   }
   ```
6. Click **"Continue to console"**

## Step 3: Enable Authentication Methods

### Enable Email/Password Authentication

1. In the Firebase Console, go to **Build** → **Authentication**
2. Click **"Get started"** (if first time)
3. Go to **Sign-in method** tab
4. Click on **"Email/Password"**
5. Toggle **"Enable"** to ON
6. **DO NOT** enable "Email link (passwordless sign-in)"
7. Click **"Save"**

### Enable Google Authentication

1. Still in **Sign-in method** tab, click on **"Google"**
2. Toggle **"Enable"** to ON
3. Select a **Project support email** from the dropdown (use your Google account email)
4. Click **"Save"**

### Enable GitHub Authentication

1. **First, create a GitHub OAuth App:**
   - Go to [GitHub Developer Settings](https://github.com/settings/developers)
   - Click **"New OAuth App"**
   - Fill in details:
     - **Application name**: `GitGud`
     - **Homepage URL**: `http://localhost:5173` (for development)
     - **Authorization callback URL**: `https://your-project-id.firebaseapp.com/__/auth/handler`
       - Replace `your-project-id` with your actual Firebase project ID
   - Click **"Register application"**
   - Copy the **Client ID**
   - Click **"Generate a new client secret"** and copy the **Client Secret**

2. **Configure GitHub in Firebase:**
   - Back in Firebase Console, **Sign-in method** tab
   - Click on **"GitHub"**
   - Toggle **"Enable"** to ON
   - Paste your GitHub **Client ID** and **Client Secret**
   - Copy the **authorization callback URL** shown (you may need to update GitHub OAuth app)
   - Click **"Save"**

## Step 4: Generate Firebase Admin SDK Service Account

The backend needs Firebase Admin SDK credentials to validate ID tokens.

1. In Firebase Console, click the **gear icon** ⚙️ → **Project settings**
2. Go to the **Service accounts** tab
3. Click **"Generate new private key"**
4. A dialog will warn you to keep this key secure - click **"Generate key"**
5. A JSON file will download (e.g., `gitgud-firebase-adminsdk-xxxxx.json`)
6. **IMPORTANT**: Keep this file secure - it grants admin access to your Firebase project

## Step 5: Store Credentials in Your Project

### 5.1 Store Firebase Admin SDK Service Account

You have two options:

**Option A: Store JSON file in project (NOT recommended for production)**

1. Create a directory: `backend/src/main/resources/firebase/`
2. Copy the downloaded JSON file to: `backend/src/main/resources/firebase/service-account.json`
3. Add to `.gitignore` to prevent committing:
   ```
   backend/src/main/resources/firebase/service-account.json
   ```

**Option B: Store as base64 environment variable (recommended)**

1. Convert JSON to base64:
   ```bash
   # macOS/Linux
   base64 -i gitgud-firebase-adminsdk-xxxxx.json | tr -d '\n'

   # Windows PowerShell
   [Convert]::ToBase64String([System.IO.File]::ReadAllBytes("gitgud-firebase-adminsdk-xxxxx.json"))
   ```
2. Copy the base64 string
3. Add to `.env` file (see next section)

### 5.2 Update .env File

Add the following variables to your `.env` file (copy from `.env.example` and fill in):

**Where to get each value:**

From **Step 2** (Register Your Web App), you received a Firebase config object. Map those values to .env variables as follows:

| Firebase Console Value | → | .env Variable (Backend)        | .env Variable (Frontend)            |
|------------------------|---|--------------------------------|-------------------------------------|
| `apiKey`               | → | `FIREBASE_API_KEY`             | `VITE_FIREBASE_API_KEY`             |
| `authDomain`           | → | `FIREBASE_AUTH_DOMAIN`         | `VITE_FIREBASE_AUTH_DOMAIN`         |
| `projectId`            | → | `FIREBASE_PROJECT_ID`          | `VITE_FIREBASE_PROJECT_ID`          |
| `storageBucket`        | → | `FIREBASE_STORAGE_BUCKET`      | `VITE_FIREBASE_STORAGE_BUCKET`      |
| `messagingSenderId`    | → | `FIREBASE_MESSAGING_SENDER_ID` | `VITE_FIREBASE_MESSAGING_SENDER_ID` |
| `appId`                | → | `FIREBASE_APP_ID`              | `VITE_FIREBASE_APP_ID`              |

**Example mapping:**
```javascript
// Firebase Console shows:
{
  apiKey: "AIzaSyABC123xyz...",              // Copy this to FIREBASE_API_KEY
  authDomain: "gitgud-12345.firebaseapp.com", // Copy this to FIREBASE_AUTH_DOMAIN
  projectId: "gitgud-12345",                 // Copy this to FIREBASE_PROJECT_ID
  storageBucket: "gitgud-12345.appspot.com", // Copy this to FIREBASE_STORAGE_BUCKET
  messagingSenderId: "987654321",            // Copy this to FIREBASE_MESSAGING_SENDER_ID
  appId: "1:987654321:web:abcdef123456"      // Copy this to FIREBASE_APP_ID
}
```

**Root .env file (for backend AND frontend shared values):**

```bash
# Firebase Configuration (from Step 2 - Register Your Web App)
FIREBASE_PROJECT_ID=gitgud-12345
FIREBASE_API_KEY=AIzaSyABC123xyz...
FIREBASE_AUTH_DOMAIN=gitgud-12345.firebaseapp.com
FIREBASE_STORAGE_BUCKET=gitgud-12345.appspot.com
FIREBASE_MESSAGING_SENDER_ID=987654321
FIREBASE_APP_ID=1:987654321:web:abcdef123456

# Firebase Admin SDK (Backend only) - Choose ONE option:

# Option A: File path (relative to backend/src/main/resources/)
# Use this if you stored the service account JSON file in your project
FIREBASE_SERVICE_ACCOUNT_PATH=firebase/service-account.json

# Option B: Base64-encoded JSON (recommended for production)
# Use this if you converted the JSON to base64 (see Step 5.1 Option B)
# FIREBASE_SERVICE_ACCOUNT_BASE64=ewogICJ0eXBlIjogInNlcnZpY2VfYWNjb3VudCIsC...
```

### 5.3 Frontend Environment Variables

The frontend reads from **Vite-prefixed** environment variables. Create or update `frontend/.env.local`:

**IMPORTANT**: Vite requires all frontend env variables to start with `VITE_`

```bash
# Copy the SAME values from Step 2, but prefix with VITE_
VITE_FIREBASE_API_KEY=AIzaSyABC123xyz...
VITE_FIREBASE_AUTH_DOMAIN=gitgud-12345.firebaseapp.com
VITE_FIREBASE_PROJECT_ID=gitgud-12345
VITE_FIREBASE_STORAGE_BUCKET=gitgud-12345.appspot.com
VITE_FIREBASE_MESSAGING_SENDER_ID=987654321
VITE_FIREBASE_APP_ID=1:987654321:web:abcdef123456
```

**Note:** You can also add these to the root `.env` file if you want to keep everything in one place. Both the backend and frontend will read from it.

## Step 6: Update Firebase Security Rules (Optional but Recommended)

To prevent unauthorized access to Firebase services:

1. In Firebase Console, go to **Firestore Database** → **Rules** (if you add Firestore later)
2. Or go to **Storage** → **Rules** (if you add Storage later)
3. Use appropriate security rules (not needed for Authentication alone)

## Step 7: Configure Authorized Domains

For production deployment, you'll need to add your domain:

1. In Firebase Console, go to **Build** → **Authentication** → **Settings** tab
2. Scroll to **Authorized domains**
3. Click **"Add domain"**
4. Add your production domain (e.g., `gitgud.com`)
5. `localhost` is already authorized for development

## Security Best Practices

### DO:
- ✅ Keep the service account JSON file secure and never commit to Git
- ✅ Use base64 environment variables for production deployments
- ✅ Rotate service account keys periodically (every 90 days)
- ✅ Use different Firebase projects for dev/staging/prod environments
- ✅ Enable **Firebase App Check** for production (prevents API abuse)
- ✅ Set up Firebase Security Rules if using Firestore/Storage

### DON'T:
- ❌ Never commit service account JSON to version control
- ❌ Never expose service account credentials in client-side code
- ❌ Never share service account keys in Slack/email/public channels
- ❌ Never use production Firebase credentials in development

## Verification

After completing the setup, verify:

1. **Backend can initialize Firebase Admin SDK:**
   - Start the backend: `cd backend && ./mvnw spring-boot:run`
   - Check logs for: `"Firebase Admin SDK initialized successfully"`
   - No errors about missing credentials

2. **Frontend can connect to Firebase:**
   - Start the frontend: `cd frontend && npm run dev`
   - Open browser console (F12)
   - Check for Firebase initialization messages
   - Try signing up with email/password

3. **Authentication works:**
   - Register a new user
   - Check Firebase Console → **Authentication** → **Users** tab
   - Verify user appears in the list

## Troubleshooting

### "Error: Firebase Admin SDK initialization failed"
- **Cause**: Service account file not found or invalid JSON
- **Fix**: Check file path in `.env` or verify base64 encoding

### "API key not valid"
- **Cause**: Wrong API key or not enabled for project
- **Fix**: Double-check API key from Firebase Console → Project settings

### "auth/unauthorized-domain"
- **Cause**: Domain not in authorized domains list
- **Fix**: Add domain to Firebase Console → Authentication → Settings → Authorized domains

### GitHub OAuth not working
- **Cause**: Callback URL mismatch
- **Fix**: Ensure GitHub OAuth app callback URL matches Firebase's exact URL

## Next Steps

Once Firebase is configured:
1. Restart the backend to load new environment variables
2. Restart the frontend
3. Test user registration and login
4. Check Firebase Console to see authenticated users

## Resources

- [Firebase Authentication Documentation](https://firebase.google.com/docs/auth)
- [Firebase Admin SDK Java Documentation](https://firebase.google.com/docs/admin/setup)
- [Firebase Security Best Practices](https://firebase.google.com/docs/rules/security-rules)
