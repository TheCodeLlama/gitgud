# MVP Development Guide
## Gamified Java Spring Learning Platform - Phase 1A (Local Development)

---

## Overview

This guide covers the **MVP Phase 1A: Foundation** - building the core platform that runs entirely in a local Docker development environment. This phase excludes cloud deployment and focuses on proving the core concept works.

**Scope**: User authentication, basic lessons, code editor, code execution, and gamification fundamentals - all running locally.

**Timeline Estimate**: 3-4 months (solo developer)

**Success Criteria**:
- All services run via `docker-compose up`
- Users can register, login, and complete Java lessons
- Code executes safely in sandboxed containers
- Basic XP/level system tracks progress
- 5-10 complete Java lessons available

---

## Phase 1A: Foundation Components

### 1. Development Environment Setup

#### 1.1 Prerequisites & Tools
- [x] Install Docker Desktop (latest stable version)
- [x] Install Docker Compose v2.x
- [x] Install Node.js 20+ and npm
- [x] Install Java 25 JDK (for local Spring Boot development/compilation)
- [x] Install Postman or similar API testing tool
- [x] Set up IDE (IntelliJ IDEA for backend, VSCode for frontend)

**Note**: PostgreSQL, Redis, RabbitMQ, Keycloak, and all backend services will run in Docker containers. They will be defined in `docker-compose.yml`.

#### 1.2 Repository Structure
- [x] Initialize git repository with proper .gitignore
- [x] Create monorepo structure:
  ```
  /backend          # Spring Boot services
  /frontend         # React SPA
  /docker           # Docker configs
  /dev-docs         # Documentation
  /scripts          # Utility scripts
  ```
- [x] Set up environment variable templates (.env.example)
- [x] Create README with setup instructions
- [x] Add docker-compose.yml at root level

#### 1.3 Docker Infrastructure
- [x] Create docker-compose.yml with infrastructure services:
  - [x] PostgreSQL container (port 5432)
  - [x] Redis container (port 6379)
  - [x] RabbitMQ container (ports 5672, 15672)
  - [x] Keycloak container (port 8180)
- [x] Configure Docker networks for service isolation
- [x] Set up volume mounts for database persistence
- [x] Create health check endpoints for all services
- [x] Test `docker-compose up` brings up all services
- [x] Document startup order and dependencies

**Note**: Backend Spring Boot services and React frontend will be run locally via IDE/terminal during development (not in docker-compose). This allows for faster development with hot-reload and debugging capabilities.

---

### 2. Backend Foundation (Spring Boot)

#### 2.1 Project Initialization
- [x] Create Spring Boot parent project (Maven/Gradle)
- [ ] Set up multi-module structure (deferred - starting with monolith):
  - [ ] `api-gateway` - Entry point, routing
  - [ ] `auth-service` - Authentication (optional microservice)
  - [ ] `learning-service` - Lessons, modules, progress
  - [ ] `gamification-service` - XP, levels, achievements
  - [ ] `code-execution-service` - Code runner
  - [ ] `common` - Shared DTOs, utilities
- [x] Configure Spring Boot 3.5.6 dependencies
- [x] Set up application.yml with dev profile
- [x] Configure logging (Logback, consistent format)
- [x] Add Spring Boot Actuator for health checks

**Note**: Starting with a monolithic application for MVP. Multi-module microservices structure will be implemented later when the application scales.

#### 2.2 Database Setup
- [x] Design initial database schema (ERD):
  - [x] Users table
  - [x] Modules table
  - [x] Lessons table
  - [x] User_Progress table
  - [x] Achievements table
  - [x] User_Achievements table
  - [x] Submissions table
  - [x] Test_Cases table
- [x] Add seed data class called DevDataBootstrapper for development:
  - [x] Sample lessons
  - [ ] Test users
  - [x] Achievement definitions
- [x] Configure JPA entities matching schema
- [x] Set up Spring Data JPA repositories

#### 2.3 REST Controllers & Configuration
- [x] Create controller package structure:
  - [x] `controller.auth` - User profile endpoints
  - [x] `controller.learning` - Modules, lessons, progress endpoints
  - [x] `controller.gamification` - XP, achievements, stats endpoints
  - [x] `controller.execution` - Code execution endpoints
- [x] Add CORS configuration for React frontend (http://localhost:5173)
- [x] Implement global exception handler (@ControllerAdvice)
- [x] Add request/response logging filter
- [x] Configure rate limiting (basic, in-memory with Bucket4j)
- [x] Set up API versioning strategy (URL path: /api/v1/)
- [x] Create common response wrapper DTOs

#### 2.4 Authentication (Keycloak Integration)
- [ ] Configure Keycloak realm:
  - [ ] Create GitGud realm in Keycloak admin console
  - [ ] Configure client application (Spring Boot backend)
  - [ ] Set up OAuth2/OIDC settings
  - [ ] Enable user registration
- [ ] Add Spring Security dependencies:
  - [ ] spring-boot-starter-security
  - [ ] spring-boot-starter-oauth2-resource-server
  - [ ] spring-boot-starter-oauth2-client
- [ ] Configure Spring Security for Keycloak:
  - [ ] Add OAuth2 resource server configuration
  - [ ] Configure JWT token validation
  - [ ] Set up CORS for frontend
  - [ ] Configure public vs protected endpoints
- [ ] Implement user synchronization:
  - [ ] Create endpoint to sync Keycloak user to local User entity
  - [ ] Extract user info from JWT (keycloak_id, email, username)
  - [ ] Create User record on first login if not exists
  - [ ] Update User record on subsequent logins
- [ ] Configure OAuth2 providers in Keycloak:
  - [ ] Google OAuth2 identity provider
  - [ ] GitHub OAuth2 identity provider
- [ ] Implement "me" endpoint (GET /api/auth/me):
  - [ ] Extract user from JWT token
  - [ ] Return user profile data
  - [ ] Include stats and progress summary
- [ ] Create authentication utility:
  - [ ] Helper to get current authenticated user
  - [ ] Extract user ID from security context
- [ ] Write unit tests for auth integration
- [ ] Test OAuth2 login flows (Google, GitHub)

**Note**: Keycloak handles all password hashing, token generation, and OAuth2 flows. The backend only validates tokens and syncs user data.

---

### 3. Learning Service (Modules, Lessons, Progress)

**Note**: In the monolithic architecture, this "service" is implemented as a set of controllers (`LearningController`), service classes, and repositories within the single Spring Boot application, not as a separate microservice.

#### 3.1 Module Management
- [ ] Create Module entity and repository
- [ ] Implement GET /api/learning/modules (list all)
- [ ] Implement GET /api/learning/modules/{id} (single module)
- [ ] Add module metadata:
  - [ ] Title, description, difficulty
  - [ ] Required XP level
  - [ ] Estimated time
  - [ ] Prerequisites
- [ ] Implement module ordering/sequencing logic
- [ ] Add API for "recommended next module"

#### 3.2 Lesson Management
- [ ] Create Lesson entity with fields:
  - [ ] Title, description, content (Markdown/HTML)
  - [ ] Lesson type (tutorial, challenge, project)
  - [ ] XP reward
  - [ ] Difficulty rating
  - [ ] Starter code template
  - [ ] Solution code (hidden)
- [ ] Implement GET /api/learning/lessons/{id}
- [ ] Create lesson content renderer (support Markdown)
- [ ] Implement GET /api/learning/modules/{moduleId}/lessons
- [ ] Add lesson completion criteria definition
- [ ] Support multi-file lesson templates (future-proof structure)

#### 3.3 Progress Tracking
- [ ] Create UserProgress entity:
  - [ ] User ID, Lesson ID
  - [ ] Status (not_started, in_progress, completed)
  - [ ] Started_at, completed_at timestamps
  - [ ] Attempts count
  - [ ] Best score
- [ ] Implement POST /api/learning/progress (update progress)
- [ ] Implement GET /api/learning/progress (user's overall progress)
- [ ] Implement GET /api/learning/progress/{lessonId} (specific lesson)
- [ ] Add streak calculation logic (consecutive days)
- [ ] Implement progress percentage calculation
- [ ] Create endpoint for "continue where you left off"

#### 3.4 Test Cases & Validation
- [ ] Create TestCase entity:
  - [ ] Lesson ID
  - [ ] Input/expected output
  - [ ] Hidden flag (visible to user or not)
  - [ ] Weight/points
- [ ] Seed test cases for sample lessons
- [ ] Create DTO for test case responses (hide expected output)
- [ ] Implement test case retrieval logic
- [ ] Design test case format (JSON structure)

---

### 4. Gamification Service (XP, Levels, Achievements)

**Note**: In the monolithic architecture, this "service" is implemented as controllers (`GamificationController`), service classes, and repositories within the single Spring Boot application, not as a separate microservice.

#### 4.1 XP System
- [ ] Create UserStats entity:
  - [ ] User ID
  - [ ] Total XP
  - [ ] Current level
  - [ ] XP to next level
  - [ ] Current streak days
  - [ ] Longest streak
- [ ] Implement XP calculation formula:
  - [ ] Base XP per lesson
  - [ ] Difficulty multiplier
  - [ ] Streak bonus
  - [ ] First-attempt bonus
- [ ] Create POST /api/gamification/xp (award XP)
- [ ] Implement level-up logic:
  - [ ] XP thresholds per level
  - [ ] Level-up notifications
- [ ] Add GET /api/gamification/stats/{userId}
- [ ] Implement daily streak tracking:
  - [ ] Check last activity date
  - [ ] Increment or reset streak
  - [ ] Store in database

#### 4.2 Achievement System
- [ ] Create Achievement entity:
  - [ ] Name, description, icon
  - [ ] Criteria (JSON/enum)
  - [ ] XP reward
  - [ ] Rarity tier
- [ ] Create UserAchievement join table
- [ ] Seed initial achievements:
  - [ ] "First Steps" - Complete first lesson
  - [ ] "Week Warrior" - 7-day streak
  - [ ] "Fast Learner" - Complete lesson in one attempt
  - [ ] "Spring Initiate" - Complete Spring Boot basics
  - [ ] "Code Warrior" - Complete 10 challenges
- [ ] Implement achievement checking service:
  - [ ] Event-based triggers (lesson complete, streak update)
  - [ ] Batch check all potential achievements
  - [ ] Award achievements (idempotent)
- [ ] Create GET /api/gamification/achievements (all achievements)
- [ ] Create GET /api/gamification/achievements/{userId} (user's achievements)
- [ ] Implement achievement notification system (return on award)

#### 4.3 Level System
- [ ] Define level progression formula (exponential):
  - [ ] Level 1: 0 XP
  - [ ] Level 2: 100 XP
  - [ ] Level 3: 250 XP
  - [ ] Level N: formula or lookup table
- [ ] Create level metadata (titles, badges):
  - [ ] Level 1-5: "Novice"
  - [ ] Level 6-10: "Apprentice"
  - [ ] Level 11-20: "Developer"
  - [ ] Level 21+: "Expert"
- [ ] Implement GET /api/gamification/levels (level info)
- [ ] Add level-based unlocks (modules locked by level)
- [ ] Create visual level progress bar data (frontend use)

#### 4.4 Character/Avatar System (Basic)
- [ ] Create UserProfile entity:
  - [ ] User ID
  - [ ] Avatar URL/type
  - [ ] Display name
  - [ ] Bio
  - [ ] Public profile flag
- [ ] Implement GET/PUT /api/gamification/profile
- [ ] Add basic avatar selection (5-10 preset avatars)
- [ ] Store avatar choice in profile
- [ ] Return profile with stats in single endpoint

---

### 5. Code Execution Module (Sandboxed Code Runner)

**Note**: In the monolithic architecture, this "service" is implemented as controllers (`CodeExecutionController`), service classes, and worker components within the single Spring Boot application, not as a separate microservice. Code execution uses Docker containers for sandboxing, but the orchestration is handled within the monolith.

#### 5.1 Code Execution Infrastructure
- [ ] Research Docker-in-Docker security (or Docker socket mounting)
- [ ] Create execution request DTO:
  - [ ] Language (Java 25)
  - [ ] Source code
  - [ ] Lesson ID
  - [ ] User ID
  - [ ] Test case IDs
- [ ] Set up RabbitMQ or in-memory queue for execution jobs:
  - [ ] Execution request queue
  - [ ] Result queue
- [ ] Create execution worker service:
  - [ ] Listen for execution requests
  - [ ] Spin up Docker container
  - [ ] Compile and run code
  - [ ] Capture output and errors
  - [ ] Compare against test cases
  - [ ] Return results

#### 5.2 Docker Sandbox
- [ ] Create Java execution Dockerfile:
  - [ ] Base: openjdk:25-slim
  - [ ] Security: non-root user, no network
  - [ ] Resource limits: CPU, memory
  - [ ] Timeout enforcement
- [ ] Build and test Docker image locally
- [ ] Implement container lifecycle management:
  - [ ] Create container with code volume
  - [ ] Execute with timeout
  - [ ] Cleanup container after execution
- [ ] Add resource limits:
  - [ ] Max execution time: 5 seconds
  - [ ] Max memory: 256MB
  - [ ] No network access
  - [ ] No file system writes outside /tmp
- [ ] Test malicious code scenarios:
  - [ ] Infinite loops
  - [ ] High memory usage
  - [ ] File system access attempts
  - [ ] Network calls
- [ ] Implement output truncation (prevent huge outputs)

#### 5.3 Code Execution API
- [ ] Create POST /api/execute/run endpoint:
  - [ ] Accept code, lesson ID, test case IDs
  - [ ] Validate request (code length limits)
  - [ ] Enqueue execution job
  - [ ] Return job ID
- [ ] Create GET /api/execute/result/{jobId} (polling):
  - [ ] Return status: pending, running, completed, failed
  - [ ] Return results when ready
- [ ] Implement WebSocket for real-time results (optional):
  - [ ] Connect to /api/execute/ws
  - [ ] Stream output as it happens
- [ ] Create execution result DTO:
  - [ ] Passed/failed status
  - [ ] Test case results (individual)
  - [ ] Execution time
  - [ ] Error messages
  - [ ] Console output
  - [ ] XP awarded

#### 5.4 Test Case Execution
- [ ] Implement test runner in Java:
  - [ ] Load user code
  - [ ] Run against each test case
  - [ ] Capture assertion results
  - [ ] Handle exceptions gracefully
- [ ] Create test case output comparison:
  - [ ] Exact match
  - [ ] Whitespace normalization
  - [ ] Numeric tolerance (for floats)
- [ ] Implement partial credit logic:
  - [ ] Award XP for passed test cases
  - [ ] Calculate percentage complete
- [ ] Add code quality checks (optional for MVP):
  - [ ] Checkstyle integration
  - [ ] Basic complexity analysis
- [ ] Store submission in database:
  - [ ] User ID, lesson ID, code, result, timestamp

---

### 6. Frontend (React SPA)

#### 6.1 Project Setup
- [ ] Create React app with Vite:
  ```bash
  npm create vite@latest frontend -- --template react-ts
  ```
- [ ] Install dependencies:
  - [ ] React Router v6
  - [ ] Tailwind CSS
  - [ ] Axios (API client)
  - [ ] React Query
  - [ ] Monaco Editor (@monaco-editor/react)
  - [ ] Zustand or Redux (state management)
  - [ ] React Hook Form (forms)
  - [ ] Zod (validation)
- [ ] Configure Tailwind CSS with custom theme:
  - [ ] Define color palette (gamified aesthetic)
  - [ ] Add font imports
  - [ ] Configure responsive breakpoints
- [ ] Set up React Router with routes structure
- [ ] Create axios instance with base URL and interceptors
- [ ] Configure React Query client

#### 6.2 Authentication Flow
- [ ] Create login page:
  - [ ] Email/password form
  - [ ] OAuth buttons (Google, GitHub)
  - [ ] Link to registration
  - [ ] Form validation
- [ ] Create registration page:
  - [ ] Email, username, password fields
  - [ ] Password strength indicator
  - [ ] Terms acceptance
  - [ ] Form validation
- [ ] Implement AuthContext/hook:
  - [ ] Store JWT token in localStorage
  - [ ] Axios interceptor to add token to requests
  - [ ] Auto-logout on 401 responses
  - [ ] Refresh token logic
- [ ] Create ProtectedRoute component
- [ ] Implement OAuth callback handling
- [ ] Add "Forgot Password" page (stub)
- [ ] Create user profile dropdown (logout, settings)

#### 6.3 Layout & Navigation
- [ ] Create main layout component:
  - [ ] Top navigation bar
  - [ ] Sidebar (collapsible)
  - [ ] Main content area
  - [ ] Footer
- [ ] Build navigation sidebar:
  - [ ] Dashboard link
  - [ ] Modules list
  - [ ] Profile link
  - [ ] Achievements link
  - [ ] Progress stats widget
- [ ] Implement top navbar:
  - [ ] Logo/branding
  - [ ] XP/Level display
  - [ ] Streak indicator
  - [ ] User avatar dropdown
  - [ ] Notifications icon (future)
- [ ] Make layout responsive (mobile, tablet, desktop)
- [ ] Add loading states and skeletons

#### 6.4 Dashboard Page
- [ ] Create dashboard route (`/dashboard`)
- [ ] Display user stats card:
  - [ ] Current level and XP
  - [ ] Progress to next level (progress bar)
  - [ ] Current streak
  - [ ] Total lessons completed
- [ ] Show "Continue Learning" section:
  - [ ] Last lesson in progress
  - [ ] Suggested next lesson
- [ ] Display recent achievements (3-5 latest)
- [ ] Add quick stats:
  - [ ] Total XP earned
  - [ ] Lessons completed this week
  - [ ] Current rank (if leaderboard exists)
- [ ] Create "Featured Modules" section
- [ ] Add motivational messages/tips

#### 6.5 Module & Lesson Browser
- [ ] Create modules list page (`/modules`):
  - [ ] Display all modules as cards
  - [ ] Show module progress (X/Y lessons)
  - [ ] Display difficulty badges
  - [ ] Show lock icon for unavailable modules
  - [ ] Filter/search functionality
- [ ] Create module detail page (`/modules/:moduleId`):
  - [ ] Module description and metadata
  - [ ] List all lessons in module
  - [ ] Show completion status per lesson
  - [ ] Display XP rewards
  - [ ] "Start Next Lesson" button
- [ ] Implement lesson card component:
  - [ ] Lesson title and description
  - [ ] Difficulty indicator
  - [ ] Completion status icon
  - [ ] XP reward badge
  - [ ] Lesson type icon (tutorial/challenge)

#### 6.6 Lesson Page (Core Experience)
- [ ] Create lesson page route (`/lessons/:lessonId`)
- [ ] Build lesson layout (split view):
  - [ ] Left panel: Instructions
  - [ ] Right panel: Code editor
  - [ ] Bottom panel: Console output (toggle)
- [ ] Integrate Monaco Editor:
  - [ ] Syntax highlighting for Java
  - [ ] Auto-completion
  - [ ] Configurable theme
  - [ ] Load starter code from API
  - [ ] Save code to localStorage (auto-save)
- [ ] Create instructions panel:
  - [ ] Render lesson content (Markdown)
  - [ ] Show test cases (visible ones)
  - [ ] Display expected behavior
  - [ ] Add hint sections (expandable)
- [ ] Build action buttons:
  - [ ] "Run Code" button
  - [ ] "Submit" button (runs against all tests)
  - [ ] "Reset Code" button
  - [ ] "Get Hint" button (future)
- [ ] Implement code execution flow:
  - [ ] POST to `/api/execute/run`
  - [ ] Poll for results or use WebSocket
  - [ ] Display loading state
  - [ ] Show results in console panel
- [ ] Create results display:
  - [ ] Test case results (passed/failed)
  - [ ] Execution time
  - [ ] Error messages with line numbers
  - [ ] XP awarded animation
  - [ ] Success/failure message
- [ ] Add navigation:
  - [ ] Previous/Next lesson buttons
  - [ ] Back to module button
  - [ ] Progress indicator (lesson X of Y)
- [ ] Implement "lesson complete" modal:
  - [ ] Celebration animation
  - [ ] XP awarded
  - [ ] Achievements unlocked
  - [ ] Next lesson suggestion

#### 6.7 Profile & Achievements
- [ ] Create profile page (`/profile`):
  - [ ] Display user info (avatar, name, bio)
  - [ ] Show total stats (XP, level, streak)
  - [ ] List all achievements (with locked states)
  - [ ] Progress chart (XP over time)
  - [ ] Edit profile button
- [ ] Build achievement showcase:
  - [ ] Grid of achievement badges
  - [ ] Show locked/unlocked states
  - [ ] Display achievement descriptions on hover
  - [ ] Sort by rarity or date earned
- [ ] Create profile edit form:
  - [ ] Update display name
  - [ ] Change avatar
  - [ ] Edit bio
  - [ ] Save changes to API
- [ ] Add settings page:
  - [ ] Change password
  - [ ] Notification preferences
  - [ ] Theme selection
  - [ ] Delete account (future)

#### 6.8 Gamification UI Elements
- [ ] Create XP progress bar component:
  - [ ] Animated fill
  - [ ] Show current/needed XP
  - [ ] Level up animation
- [ ] Build achievement toast/notification:
  - [ ] Slide in from top/corner
  - [ ] Show achievement icon and name
  - [ ] Auto-dismiss after 5 seconds
  - [ ] Click to view achievement details
- [ ] Create streak indicator component:
  - [ ] Fire icon with streak number
  - [ ] Visual feedback for maintaining streak
  - [ ] Warning if streak is at risk
- [ ] Implement level-up modal:
  - [ ] Celebration animation
  - [ ] New level display
  - [ ] Unlocked content
  - [ ] Shareable image (future)
- [ ] Add XP gain animations:
  - [ ] +XP floating text
  - [ ] Particle effects (optional)
  - [ ] Sound effects (optional, user toggle)

---

### 7. Content Creation

#### 7.1 Java Fundamentals Module (Free Trial)
- [ ] Lesson 1: "Hello, Java!"
  - [ ] Write and run first Java program
  - [ ] Test case: Output "Hello, World!"
  - [ ] Starter code: Empty main method
  - [ ] Introduce print statements
- [ ] Lesson 2: "Variables and Types"
  - [ ] Declare and use variables
  - [ ] Test cases: String, int, boolean operations
  - [ ] Cover primitive types
- [ ] Lesson 3: "Basic Math Operations"
  - [ ] Arithmetic operators
  - [ ] Test cases: Addition, subtraction, etc.
  - [ ] Introduce operator precedence
- [ ] Lesson 4: "Conditional Logic (if/else)"
  - [ ] Write conditional statements
  - [ ] Test cases: Different branches
  - [ ] Cover boolean expressions
- [ ] Lesson 5: "Loops (for/while)"
  - [ ] Iterate with loops
  - [ ] Test cases: Loop outputs
  - [ ] Introduce loop control (break/continue)
- [ ] Lesson 6: "Arrays Basics"
  - [ ] Create and manipulate arrays
  - [ ] Test cases: Array operations
  - [ ] Cover array iteration
- [ ] Lesson 7: "Methods"
  - [ ] Define and call methods
  - [ ] Test cases: Method return values
  - [ ] Introduce parameters and return types
- [ ] Lesson 8: "Classes and Objects (Intro)"
  - [ ] Create simple class
  - [ ] Test cases: Object state
  - [ ] Introduce constructors
- [ ] Lesson 9: "Collections (ArrayList)"
  - [ ] Use ArrayList
  - [ ] Test cases: List operations
  - [ ] Cover basic collection methods
- [ ] Lesson 10: "Capstone: Build a Simple Calculator"
  - [ ] Multi-method project
  - [ ] Test cases: Calculator operations
  - [ ] Combine previous concepts

#### 7.2 Spring Boot Basics Module (5 Lessons for MVP)
- [ ] Lesson 1: "Spring Boot Intro"
  - [ ] Understand Spring Boot structure
  - [ ] Create a @SpringBootApplication
  - [ ] Test case: Application starts successfully
- [ ] Lesson 2: "REST Controller Basics"
  - [ ] Create simple @RestController
  - [ ] @GetMapping that returns String
  - [ ] Test case: Endpoint returns expected response
- [ ] Lesson 3: "Request Parameters"
  - [ ] Use @RequestParam
  - [ ] Test cases: Different parameter values
  - [ ] Introduce validation
- [ ] Lesson 4: "Path Variables"
  - [ ] Use @PathVariable
  - [ ] Test cases: Dynamic URL segments
  - [ ] Cover RESTful design
- [ ] Lesson 5: "POST Requests and DTOs"
  - [ ] Handle @PostMapping
  - [ ] Use request body DTOs
  - [ ] Test cases: POST with JSON

#### 7.3 Achievement Definitions
- [ ] Define 10-15 initial achievements with criteria:
  - [ ] First lesson complete
  - [ ] Module complete
  - [ ] Perfect score (all tests pass first try)
  - [ ] Streak milestones (3, 7, 14, 30 days)
  - [ ] Speed achievements (complete lesson under time)
  - [ ] XP milestones (100, 500, 1000 XP)
  - [ ] Level milestones (Level 5, 10, 15)
- [ ] Create achievement icons/badges (or use placeholders)
- [ ] Write achievement descriptions
- [ ] Implement achievement rarity tiers (common, rare, epic)

---

### 8. Integration & Testing

#### 8.1 API Integration Testing
- [ ] Test authentication flow end-to-end:
  - [ ] Registration → Login → Access protected endpoint
  - [ ] OAuth flow (Google, GitHub)
  - [ ] Token refresh
- [ ] Test learning service integration:
  - [ ] Fetch modules → Select lesson → Update progress
  - [ ] Verify progress persists
- [ ] Test gamification integration:
  - [ ] Complete lesson → Receive XP → Level up
  - [ ] Unlock achievement
  - [ ] Check streak updates
- [ ] Test code execution integration:
  - [ ] Submit code → Execute → Receive results
  - [ ] Verify test case evaluation
  - [ ] Confirm XP awarded on success

#### 8.2 Unit Testing
- [ ] Write unit tests for backend services:
  - [ ] Auth service (token generation, validation)
  - [ ] Learning service (progress calculation)
  - [ ] Gamification service (XP/level logic)
  - [ ] Code execution service (Docker orchestration)
- [ ] Achieve >70% code coverage
- [ ] Test edge cases and error handling
- [ ] Mock external dependencies (database, Redis)

#### 8.3 Frontend Testing
- [ ] Write component tests (React Testing Library):
  - [ ] Authentication forms
  - [ ] Lesson page interactions
  - [ ] Dashboard displays
- [ ] Test custom hooks:
  - [ ] useAuth
  - [ ] useLessonProgress
  - [ ] useCodeExecution
- [ ] Test API integration with mock server (MSW)
- [ ] Implement E2E tests (Playwright or Cypress):
  - [ ] User registration and login
  - [ ] Complete a lesson end-to-end
  - [ ] Achievement unlocking

#### 8.4 Security Testing
- [ ] Test code execution sandbox:
  - [ ] Verify resource limits enforced
  - [ ] Confirm network isolation
  - [ ] Test file system restrictions
  - [ ] Attempt container escape (ethical)
- [ ] Test authentication security:
  - [ ] Verify password hashing
  - [ ] Test JWT expiration
  - [ ] Attempt SQL injection
  - [ ] Test CSRF protection
- [ ] Perform basic penetration testing:
  - [ ] API endpoint fuzzing
  - [ ] XSS attempts
  - [ ] Authentication bypass attempts
- [ ] Review and fix security findings

---

### 9. Performance Optimization

#### 9.1 Backend Optimization
- [ ] Add Redis caching for:
  - [ ] User sessions
  - [ ] Frequently accessed lessons
  - [ ] Achievement definitions
  - [ ] Leaderboard data (future)
- [ ] Optimize database queries:
  - [ ] Add indexes on foreign keys
  - [ ] Use JOIN FETCH for N+1 queries
  - [ ] Implement pagination for large lists
- [ ] Implement connection pooling (HikariCP)
- [ ] Add database query logging and analysis
- [ ] Profile API response times (aim for <200ms)

#### 9.2 Frontend Optimization
- [ ] Implement code splitting:
  - [ ] Lazy load routes
  - [ ] Split Monaco Editor into separate chunk
  - [ ] Split large components
- [ ] Optimize bundle size:
  - [ ] Analyze with Vite bundle analyzer
  - [ ] Remove unused dependencies
  - [ ] Tree-shake libraries
- [ ] Add React Query caching:
  - [ ] Cache lesson data
  - [ ] Cache user stats
  - [ ] Implement stale-while-revalidate
- [ ] Optimize images:
  - [ ] Use WebP format
  - [ ] Implement lazy loading
  - [ ] Add responsive images
- [ ] Implement virtual scrolling for long lists (if needed)

#### 9.3 Code Execution Optimization
- [ ] Implement container pooling:
  - [ ] Pre-warm containers
  - [ ] Reuse containers for multiple executions
  - [ ] Clean up idle containers
- [ ] Add execution result caching:
  - [ ] Cache identical code submissions
  - [ ] Invalidate cache on lesson updates
- [ ] Optimize Docker image size:
  - [ ] Use slim base images
  - [ ] Remove unnecessary layers
- [ ] Implement execution queue prioritization:
  - [ ] Prioritize first-time users
  - [ ] Throttle heavy users

---

### 10. Documentation & DevOps

#### 10.1 Code Documentation
- [ ] Write JavaDoc for all public APIs
- [ ] Document React components with JSDoc
- [ ] Create API documentation (Swagger/OpenAPI):
  - [ ] All endpoints documented
  - [ ] Request/response examples
  - [ ] Authentication requirements
- [ ] Write inline code comments for complex logic
- [ ] Create architecture decision records (ADRs)

#### 10.2 User Documentation
- [ ] Create user guide:
  - [ ] How to get started
  - [ ] How lessons work
  - [ ] XP and leveling system
  - [ ] Achievements guide
- [ ] Write FAQ document
- [ ] Create video tutorial (optional):
  - [ ] Platform walkthrough
  - [ ] First lesson completion

#### 10.3 Developer Documentation
- [ ] Write comprehensive README.md:
  - [ ] Project overview
  - [ ] Architecture diagram
  - [ ] Setup instructions
  - [ ] Docker commands
  - [ ] Troubleshooting guide
- [ ] Document database schema (ERD diagram)
- [ ] Create API integration guide
- [ ] Write contribution guidelines (future)

#### 10.4 Local Development Scripts
- [ ] Create npm/make scripts:
  - [ ] `make start` - Start all services
  - [ ] `make stop` - Stop all services
  - [ ] `make reset` - Reset database
  - [ ] `make seed` - Seed sample data
  - [ ] `make test` - Run all tests
  - [ ] `make logs` - Tail logs
- [ ] Add database migration scripts
- [ ] Create backup/restore scripts
- [ ] Write debugging helper scripts

---

### 11. Polish & User Experience

#### 11.1 Error Handling
- [ ] Implement global error boundaries in React
- [ ] Add user-friendly error messages:
  - [ ] Network errors
  - [ ] Authentication failures
  - [ ] Code execution errors
  - [ ] Validation errors
- [ ] Create error pages:
  - [ ] 404 Not Found
  - [ ] 500 Server Error
  - [ ] Unauthorized
- [ ] Log errors to console (dev) or service (prod)
- [ ] Add retry mechanisms for transient failures

#### 11.2 Loading States & Feedback
- [ ] Add loading spinners for:
  - [ ] Page transitions
  - [ ] API calls
  - [ ] Code execution
- [ ] Implement skeleton screens:
  - [ ] Module list
  - [ ] Lesson content
  - [ ] Dashboard widgets
- [ ] Add progress indicators:
  - [ ] Long-running operations
  - [ ] File uploads (future)
- [ ] Show toast notifications for:
  - [ ] Success actions
  - [ ] Errors
  - [ ] Achievements unlocked

#### 11.3 Accessibility
- [ ] Ensure keyboard navigation works:
  - [ ] Tab order logical
  - [ ] Focus indicators visible
  - [ ] Modal trapping
- [ ] Add ARIA labels and roles:
  - [ ] Buttons and links
  - [ ] Form inputs
  - [ ] Dynamic content
- [ ] Test with screen reader (VoiceOver/NVDA)
- [ ] Ensure color contrast meets WCAG AA
- [ ] Add alt text to images
- [ ] Support reduced motion preference

#### 11.4 Responsive Design
- [ ] Test on mobile devices (iOS, Android)
- [ ] Optimize lesson page for mobile:
  - [ ] Stacked layout (instructions above editor)
  - [ ] Touch-friendly buttons
  - [ ] Mobile keyboard support
- [ ] Test on tablets
- [ ] Ensure desktop experience is optimal
- [ ] Add responsive navigation (hamburger menu)

---

### 12. Beta Testing & Feedback

#### 12.1 Internal Testing
- [ ] Complete full user journey yourself:
  - [ ] Register, complete lessons, earn achievements
  - [ ] Identify pain points
- [ ] Have 2-3 friends/colleagues test:
  - [ ] Observe their experience
  - [ ] Gather qualitative feedback
- [ ] Test on different browsers:
  - [ ] Chrome, Firefox, Safari, Edge
- [ ] Test on different OS:
  - [ ] macOS, Windows, Linux
- [ ] Create bug tracking system (GitHub Issues)

#### 12.2 Beta Program
- [ ] Recruit 10-20 beta testers:
  - [ ] Friends, colleagues, online communities
  - [ ] Mix of skill levels
- [ ] Create beta feedback form:
  - [ ] What worked well?
  - [ ] What was confusing?
  - [ ] What features are missing?
  - [ ] Rate overall experience (1-10)
- [ ] Add in-app feedback button
- [ ] Monitor user behavior:
  - [ ] Where do users drop off?
  - [ ] Which lessons are too hard?
  - [ ] Average completion time
- [ ] Iterate based on feedback:
  - [ ] Fix critical bugs
  - [ ] Improve confusing UX
  - [ ] Adjust lesson difficulty

#### 12.3 Performance Testing
- [ ] Load test backend APIs:
  - [ ] Simulate 50 concurrent users
  - [ ] Measure response times
  - [ ] Identify bottlenecks
- [ ] Test code execution under load:
  - [ ] Queue 20 simultaneous executions
  - [ ] Verify timeout handling
  - [ ] Check container cleanup
- [ ] Monitor resource usage:
  - [ ] CPU and memory per service
  - [ ] Database connection pool
  - [ ] Docker container limits
- [ ] Optimize based on findings

---

### 13. Pre-Launch Checklist

#### 13.1 Functionality Verification
- [ ] All MVP features implemented and working
- [ ] 10+ Java lessons available
- [ ] 5+ Spring Boot lessons available
- [ ] XP and leveling system functioning
- [ ] Achievements unlocking correctly
- [ ] Code execution safe and reliable
- [ ] Authentication flows working (email + OAuth)
- [ ] Progress tracking accurate
- [ ] All critical bugs fixed
- [ ] No data loss issues

#### 13.2 Quality Assurance
- [ ] Code review completed for all services
- [ ] Test coverage >70%
- [ ] Security review completed
- [ ] Performance benchmarks met:
  - [ ] Page load <2s
  - [ ] API response <200ms
  - [ ] Code execution <5s
- [ ] Accessibility checks passed
- [ ] Cross-browser testing done
- [ ] Mobile responsiveness verified

#### 13.3 Documentation
- [ ] README complete and tested by fresh user
- [ ] API documentation complete
- [ ] User guide available
- [ ] Troubleshooting guide written
- [ ] All scripts documented
- [ ] Architecture diagrams up-to-date

#### 13.4 Legal & Compliance
- [ ] Terms of Service drafted (even for MVP)
- [ ] Privacy Policy written
- [ ] Cookie consent (if applicable)
- [ ] Copyright notices added
- [ ] License file included (MIT/Apache/etc.)
- [ ] Third-party licenses documented

---

## Post-Phase 1A: Next Steps

Once Phase 1A is complete and working locally, you'll move to:

### Phase 1B: Environment Deployment
- Set up AWS infrastructure (EC2, RDS, S3)
- Create development environment in cloud
- Implement CI/CD pipeline (GitLab CI)
- Deploy to staging environment
- Deploy to production environment

### Phase 1C: Core Learning Expansion
- Add more Java lessons (20+ total)
- Complete Spring Boot module (15+ lessons)
- Add Spring Data JPA module
- Implement project-based challenges

### Phase 1D: Enhanced Gamification
- Add leaderboards (global, friends)
- Implement social features (friend system)
- Add daily challenges
- Create seasonal events

### Phase 1E: Payment & Launch
- Integrate Stripe for subscriptions
- Implement subscription tiers
- Add email notification system
- Launch marketing website
- Public beta launch

---

## Success Metrics for Phase 1A

At the end of Phase 1A, you should have:

- [ ] **Technical**: All services run with `docker-compose up` on any machine
- [ ] **Content**: 15+ complete interactive lessons (10 Java + 5 Spring)
- [ ] **User Experience**: A new user can register and complete a lesson in <10 minutes
- [ ] **Gamification**: XP, levels, and achievements working end-to-end
- [ ] **Security**: Code execution is sandboxed and safe
- [ ] **Quality**: >70% test coverage, no critical bugs
- [ ] **Documentation**: Another developer can set up and run the project
- [ ] **Feedback**: 5+ beta users have completed at least one module

---

## Timeline Estimate (Solo Developer)

| Phase | Duration | Description |
|-------|----------|-------------|
| **Weeks 1-2** | Setup & Infrastructure | Docker, databases, project structure |
| **Weeks 3-4** | Backend Foundation | Keycloak integration, REST controllers, config |
| **Weeks 5-6** | Learning Module | Modules, lessons, progress tracking |
| **Weeks 7-8** | Gamification Module | XP, levels, achievements |
| **Weeks 9-10** | Code Execution | Docker sandbox, test runner |
| **Weeks 11-12** | Frontend Setup | React app, routing, layout |
| **Weeks 13-14** | Core UI Components | Dashboard, module browser, profile |
| **Weeks 15-16** | Lesson Page | Monaco editor, execution, results |
| **Weeks 17-18** | Content Creation | Write 15+ lessons with test cases |
| **Weeks 19-20** | Integration & Testing | End-to-end tests, bug fixes |
| **Weeks 21-22** | Polish & Optimization | Performance, UX improvements |
| **Weeks 23-24** | Beta Testing & Iteration | Gather feedback, final fixes |

**Total: ~6 months (24 weeks)**

---

## Tips for Success

1. **Start Small**: Don't try to build everything at once. Get one feature working end-to-end before moving on.

2. **Test Continuously**: Don't wait until the end to test. Test each component as you build it.

3. **Document as You Go**: Write documentation while the decisions are fresh in your mind.

4. **Prioritize Security**: Code execution must be secure from day one. Don't compromise here.

5. **User Feedback Early**: Get real users testing as soon as you have a basic flow working.

6. **Keep It Simple**: MVP means minimum viable product. Save complex features for later phases.

7. **Track Progress**: Use this checklist! Update it weekly to see your progress.

8. **Take Breaks**: Burnout is real. Pace yourself for the long haul.

9. **Iterate**: Your first implementation won't be perfect. Plan to refactor.

10. **Have Fun**: Remember why you're building this - to make learning fun!

---

## Resources

### Learning Resources
- Spring Boot Documentation: https://spring.io/projects/spring-boot
- React Documentation: https://react.dev
- Docker Documentation: https://docs.docker.com
- Monaco Editor: https://microsoft.github.io/monaco-editor/

### Tools
- Docker Desktop: https://www.docker.com/products/docker-desktop
- Postman: https://www.postman.com
- IntelliJ IDEA: https://www.jetbrains.com/idea/
- VSCode: https://code.visualstudio.com

### Community
- Stack Overflow
- Reddit: r/springboot, r/reactjs
- Discord: Spring Boot & React communities

---

**Good luck building your MVP! Remember: progress over perfection.**
