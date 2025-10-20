# Multi-File Spring Boot Project Testing Guide

This guide walks you through testing the complete multi-file Spring Boot project feature end-to-end.

## Table of Contents
1. [Prerequisites](#prerequisites)
2. [Setup](#setup)
3. [Building the Docker Image](#building-the-docker-image)
4. [Starting the Application](#starting-the-application)
5. [Testing the Feature](#testing-the-feature)
6. [Verifying Database Changes](#verifying-database-changes)
7. [Testing the Frontend](#testing-the-frontend)
8. [Testing Code Execution](#testing-code-execution)
9. [Troubleshooting](#troubleshooting)

---

## Prerequisites

Ensure you have the following installed:
- **Docker** (20.10 or higher)
- **Docker Compose** (2.0 or higher)
- **Java 25** (for backend development)
- **Node.js 18+** (for frontend development)
- **Maven 3.9+** (optional - backend uses Maven wrapper)

## Setup

### 1. Clone or Pull Latest Changes

```bash
cd /Users/heath/Repos/personal/gitgud
git pull origin dev  # or your current branch
```

### 2. Verify Environment Configuration

Ensure your `.env` file exists at the repository root:

```bash
# Copy from example if needed
cp .env.example .env

# Verify contents
cat .env
```

Key variables to check:
- `DATABASE_URL` should point to `localhost:5432`
- `ACTIVE_PROFILE=dev`
- `BACKEND_PORT=8080`

---

## Building the Docker Image

The Java executor Docker image now includes Maven for Spring Boot project execution.

### Build the Updated Image

```bash
# Navigate to the docker directory
cd docker/java-executor

# Build the image with Maven support
docker build -t gitgud-java-executor:latest .

# Verify Maven is installed
docker run --rm gitgud-java-executor:latest mvn --version
```

**Expected output:**
```
Apache Maven 3.9.6
Maven home: /usr/share/maven
Java version: 25, vendor: Oracle Corporation
```

### Return to Project Root

```bash
cd ../..
```

---

## Starting the Application

### 1. Start Infrastructure Services

```bash
# Start PostgreSQL, Redis, RabbitMQ, Keycloak, and observability stack
docker-compose up -d

# Verify all services are running
docker-compose ps
```

All services should show status `Up`.

### 2. Start the Backend

The backend will automatically seed the database with the new multi-file Spring Boot lesson.

```bash
cd backend

# Clean and build
./mvnw clean install -DskipTests

# Run the application (dev profile)
./mvnw spring-boot:run
```

**Watch for these log messages:**
```
[DevDataBootstrapper] Seeding modules and lessons...
[DevDataBootstrapper] Created multi-file Spring Boot lesson: Build a User REST API
[DevDataBootstrapper] Modules and lessons seeded successfully!
[DevDataBootstrapper] Development data bootstrapping completed successfully!
```

### 3. Start the Frontend

Open a **new terminal**:

```bash
cd frontend

# Install dependencies (if not already done)
npm install

# Start dev server
npm run dev
```

Frontend should be available at: **http://localhost:5173**

---

## Verifying Database Changes

### Connect to PostgreSQL

```bash
docker exec -it gitgud-postgres psql -U admin -d gitgud
```

### Verify the Lesson

```sql
-- Check the new Spring Boot lesson
SELECT id, title, project_type, lesson_type, xp_reward
FROM lessons
WHERE title = 'Build a User REST API';
```

**Expected output:**
- `project_type` should be `SPRING_BOOT`
- `lesson_type` should be `CHALLENGE`
- `xp_reward` should be `30`

### Verify Project Files

```sql
-- Count project files for the lesson
SELECT COUNT(*) FROM project_files
WHERE lesson_id = (SELECT id FROM lessons WHERE title = 'Build a User REST API');
```

**Expected output:** 7 files

```sql
-- List all project files
SELECT path, file_type, is_editable, is_visible
FROM project_files
WHERE lesson_id = (SELECT id FROM lessons WHERE title = 'Build a User REST API')
ORDER BY display_order;
```

**Expected files:**
1. `pom.xml` (CONFIG, not editable, visible)
2. `src/main/java/com/example/demo/DemoApplication.java` (SOURCE, not editable, visible)
3. `src/main/java/com/example/demo/model/User.java` (SOURCE, not editable, visible)
4. `src/main/java/com/example/demo/service/UserService.java` (SOURCE, **editable**, visible)
5. `src/main/java/com/example/demo/controller/UserController.java` (SOURCE, **editable**, visible)
6. `src/main/resources/application.properties` (CONFIG, not editable, visible)
7. `src/test/java/com/example/demo/controller/UserControllerTest.java` (TEST, not editable, **hidden**)

Exit PostgreSQL:
```sql
\q
```

---

## Testing the Frontend

### 1. Register/Login

1. Navigate to **http://localhost:5173**
2. Click **Sign Up** and create a test account
3. Login with your credentials

### 2. Navigate to the Spring Boot Module

1. Click **Modules** or **Browse Modules**
2. Find **Spring Boot Basics** module
3. Click to view lessons
4. Find **Build a User REST API** (should be lesson #2)

### 3. Verify Multi-File Editor Loads

When you open the lesson, verify:

**Left Side - Instructions Panel:**
- ✅ Lesson title: "Build a User REST API"
- ✅ Comprehensive instructions with project structure diagram
- ✅ Step-by-step guide for UserService and UserController

**Right Side - Multi-File Editor:**

**File Tree (Left Sidebar):**
- ✅ VS Code-style hierarchical file tree
- ✅ Folders are collapsible (src, main, java, etc.)
- ✅ 6 visible files (test file is hidden)
- ✅ File icons displayed (☕ for .java, 📋 for .xml, etc.)

**Tab Bar:**
- ✅ First source file auto-opens
- ✅ Tabs show file names with icons
- ✅ Close buttons visible on hover

**Editor:**
- ✅ Monaco editor with Java syntax highlighting
- ✅ Code displays correctly
- ✅ Read-only files cannot be edited (try editing DemoApplication.java)
- ✅ Editable files can be modified (UserService.java, UserController.java)

### 4. Test File Operations

**Open Multiple Files:**
1. Click on `UserService.java` in the file tree
2. Click on `UserController.java` in the file tree
3. Both should open in separate tabs
4. Switch between tabs by clicking
5. Close a tab using the X button

**Test Dirty State:**
1. Open `UserService.java`
2. Make a change to the code
3. ✅ Verify a **dirty indicator** (orange dot) appears on the tab
4. ✅ Verify **"1 unsaved"** appears in the header
5. ✅ Verify **Save All** button appears

**Test Save:**
1. Click **Save All** or press `Cmd/Ctrl + S`
2. ✅ Dirty indicator disappears
3. ✅ Unsaved count disappears

---

## Testing Code Execution

### 1. Incomplete Code (Should Fail)

Leave the starter code as-is (with TODOs):

1. Click **Run Tests** button
2. Wait for execution (may take 30-60 seconds on first run - Maven downloads dependencies)

**Console Output Should Show:**
- ✅ Status: "Some Tests Failed"
- ✅ Compilation errors OR test failures
- ✅ Spring Test Results component displays
- ✅ Tests grouped by class: `UserControllerTest`
- ✅ Individual test methods shown
- ✅ Failed tests show error messages and stack traces

### 2. Complete the Service Layer

Open `UserService.java` and implement the methods:

```java
public List<User> getAllUsers() {
    return users;
}

public User createUser(User user) {
    users.add(user);
    return user;
}
```

**Run Tests Again:**
- ✅ Some tests should now pass
- ✅ Controller tests still fail (UserController not implemented)
- ✅ Test results update in console

### 3. Complete the Controller Layer

Open `UserController.java` and implement:

```java
@RestController
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/api/users")
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @PostMapping("/api/users")
    public User createUser(@RequestBody User user) {
        return userService.createUser(user);
    }
}
```

### 4. Submit the Solution

1. Click **Submit** button
2. Wait for execution

**Expected Success:**
- ✅ Status: "All Tests Passed!" (green)
- ✅ Test Results: 3/3 tests passed
- ✅ XP Awarded: +30 XP displayed
- ✅ Spring Test Results show:
  - `testGetAllUsersInitiallyEmpty` ✅ Passed
  - `testCreateUser` ✅ Passed
  - `testGetAllUsersAfterCreation` ✅ Passed
- ✅ Execution time displayed
- ✅ Build output available in console

**Level Up Modal:**
- ✅ Modal appears showing XP gained
- ✅ Achievements earned (if any)
- ✅ Option to continue to next lesson

### 5. Verify Progress Saved

1. Navigate away from the lesson
2. Navigate back to the same lesson
3. ✅ Code should be restored (auto-loaded from latest submission)
4. ✅ Console should show previous test results
5. ✅ Lesson should be marked as completed in module view

---

## Verifying Backend Execution

### Check RabbitMQ Queue

1. Open RabbitMQ Management: **http://localhost:15672**
2. Login: `admin` / `password`
3. Go to **Queues** tab
4. ✅ `code.execution.queue` should exist
5. During execution, message count should increase briefly then return to 0

### Check Redis Cache

```bash
# Connect to Redis
docker exec -it gitgud-redis redis-cli

# List all execution result keys
KEYS execution:result:*

# View a specific result (replace <job-id> with actual ID from submission)
GET execution:result:<job-id>

# Exit Redis
exit
```

### Check Docker Container Logs

While a test is running:

```bash
# List running containers
docker ps

# You should see a container from gitgud-java-executor:latest
# Get the container ID and view logs
docker logs <container-id>
```

---

## API Testing (Optional)

You can also test the backend APIs directly using curl or Postman.

### 1. Register and Login

```bash
# Register
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "Test123!@#",
    "email": "test@example.com"
  }'

# Login (save the token)
TOKEN=$(curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "Test123!@#"
  }' | jq -r '.data.accessToken')
```

### 2. Get the Spring Boot Lesson

```bash
# List all modules
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/v1/learning/modules | jq

# Get Spring Boot Basics module (replace <module-id> with actual ID)
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/v1/learning/modules/<module-id> | jq

# Get lessons in module
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/v1/learning/modules/<module-id>/lessons | jq
```

### 3. Get Project Files

```bash
# Get all project files for the lesson (replace <lesson-id>)
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/v1/learning/lessons/<lesson-id>/files | jq

# This should return 6 visible files (test file is hidden)
```

### 4. Submit Multi-File Code

```bash
# Submit the complete solution
curl -X POST http://localhost:8080/api/v1/execute/run \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "language": "java",
    "lessonId": "<lesson-id>",
    "projectFiles": {
      "pom.xml": "...",
      "src/main/java/com/example/demo/DemoApplication.java": "...",
      "src/main/java/com/example/demo/model/User.java": "...",
      "src/main/java/com/example/demo/service/UserService.java": "...",
      "src/main/java/com/example/demo/controller/UserController.java": "...",
      "src/main/resources/application.properties": "...",
      "src/test/java/com/example/demo/controller/UserControllerTest.java": "..."
    }
  }'

# Response will contain jobId
```

### 5. Poll for Results

```bash
# Poll with the job ID from previous response
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/v1/execute/result/<job-id> | jq

# Keep polling until status is COMPLETED or FAILED
```

---

## Troubleshooting

### Issue: Maven Container Fails to Start

**Symptom:** Tests fail with "Failed to create container" error

**Solution:**
```bash
# Rebuild the Docker image
cd docker/java-executor
docker build -t gitgud-java-executor:latest .

# Verify Maven is installed
docker run --rm gitgud-java-executor:latest mvn --version
```

### Issue: Tests Timeout

**Symptom:** "Execution timed out" error

**Cause:** First Maven execution downloads dependencies (can take 1-2 minutes)

**Solution:**
- Increase timeout in `application.yml` (currently 30 seconds for multi-file)
- Or wait for dependencies to cache (subsequent runs will be faster)

### Issue: File Tree Not Loading

**Symptom:** "No files in this project" message

**Solution:**
```bash
# Check if files were seeded
docker exec -it gitgud-postgres psql -U admin -d gitgud

SELECT COUNT(*) FROM project_files
WHERE lesson_id = (SELECT id FROM lessons WHERE title = 'Build a User REST API');

# Should return 7
```

If 0 files, restart backend to re-run DevDataBootstrapper.

### Issue: Frontend Can't Connect to Backend

**Symptom:** Network errors in browser console

**Solution:**
```bash
# Verify backend is running
curl http://localhost:8080/actuator/health

# Check CORS configuration in backend
# Should allow http://localhost:5173

# Verify .env has correct VITE_API_BASE_URL
cat frontend/.env
```

### Issue: Tests Always Fail

**Symptom:** All tests fail even with correct code

**Solution:**
```bash
# Check Docker logs for the Maven container
docker ps  # Find container ID
docker logs <container-id>

# Look for compilation errors or missing dependencies
```

### Issue: Changes Not Saved

**Symptom:** Code reverts after refresh

**Solution:**
- Ensure you clicked **Save All** or pressed `Cmd/Ctrl + S`
- Check browser console for API errors
- Verify files are editable (check `isEditable` in database)

---

## Expected Performance Metrics

### First Test Run (Cold Start)
- **Time:** 60-90 seconds
- **Reason:** Maven downloads all Spring Boot dependencies
- **Container logs:** You'll see Maven downloading artifacts

### Subsequent Test Runs (Warm)
- **Time:** 15-25 seconds
- **Reason:** Dependencies are cached in tmpfs
- **Container logs:** Minimal Maven output, mostly compilation and test execution

### Memory Usage
- **Container:** ~512MB (configured in DockerExecutorService)
- **Backend:** ~300-500MB
- **Frontend:** ~50-100MB
- **PostgreSQL:** ~50MB
- **Redis:** ~10MB
- **RabbitMQ:** ~100MB

---

## Success Checklist

Mark each item as you complete testing:

### Infrastructure
- [ ] Docker services all running (`docker-compose ps`)
- [ ] Backend started successfully
- [ ] Frontend accessible at http://localhost:5173
- [ ] Maven installed in Docker image (`docker run --rm gitgud-java-executor:latest mvn --version`)

### Database
- [ ] Spring Boot lesson exists in database
- [ ] 7 project files created for the lesson
- [ ] Files have correct permissions (editable, visible, etc.)

### Frontend - Multi-File Editor
- [ ] File tree displays hierarchically
- [ ] Folders can collapse/expand
- [ ] Files can be opened in tabs
- [ ] Multiple tabs can be open simultaneously
- [ ] Tab switching works
- [ ] Tabs can be closed
- [ ] Dirty state indicators work
- [ ] Save functionality works
- [ ] Read-only files cannot be edited
- [ ] Editable files can be modified

### Frontend - Lesson Flow
- [ ] Lesson instructions display correctly
- [ ] Code editor loads with starter code
- [ ] Run Tests button works
- [ ] Submit button works
- [ ] Console panel shows execution results
- [ ] Spring Test Results component displays
- [ ] Test results grouped by class
- [ ] Individual test methods shown
- [ ] Error messages and stack traces visible
- [ ] XP award displayed on success
- [ ] Level up modal appears

### Backend - Code Execution
- [ ] Multi-file submissions accepted
- [ ] Maven compilation succeeds
- [ ] Spring Boot tests execute
- [ ] JUnit XML reports parsed correctly
- [ ] Test results returned to frontend
- [ ] Results cached in Redis
- [ ] Submissions saved to database
- [ ] XP awarded for passing tests
- [ ] Progress updated

### End-to-End Flow
- [ ] Incomplete code fails tests appropriately
- [ ] Partial implementation shows partial results
- [ ] Complete solution passes all tests
- [ ] XP and achievements awarded
- [ ] Code persists across page refreshes
- [ ] Lesson marked as completed

---

## Next Steps

After successful testing:

1. **Review Logs:** Check backend logs for any warnings or errors
2. **Performance Testing:** Try submitting multiple times rapidly
3. **Edge Cases:** Test with intentionally broken code, syntax errors, infinite loops
4. **Security Testing:** Verify container isolation, resource limits, timeouts
5. **Browser Testing:** Test on Chrome, Firefox, Safari
6. **Mobile Testing:** Test responsive design on mobile devices

---

## Support

If you encounter issues not covered in this guide:

1. Check backend logs: `backend/logs/application.log`
2. Check browser console for frontend errors
3. Check Docker logs: `docker-compose logs <service-name>`
4. Review database state using psql
5. Verify network connectivity between services

For development questions, refer to:
- `dev-docs/MVP-Development-Guide.md`
- `CLAUDE.md` (project documentation)
- `dev-docs/CodeExecutionAPI.md` (API documentation)
