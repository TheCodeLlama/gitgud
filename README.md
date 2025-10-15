# GitGud - Gamified Java Spring Learning Platform

A gamified learning platform that teaches Java Spring development through interactive, step-by-step progression with game-like mechanics.

## Overview

GitGud combines the best aspects of coding challenge platforms with game mechanics to create an engaging learning experience for Java Spring developers. Earn XP, level up, unlock achievements, and progress through a structured curriculum - all while building real-world skills.

## Tech Stack

- **Backend**: Spring Boot 3.5.6, Java 25, Maven
- **Frontend**: React 19, Vite
- **Database**: PostgreSQL 17 (managed with Hibernate DDL)
- **Authentication**: Keycloak 27 (OAuth2/OIDC)
- **Cache**: Redis 7 (execution results, sessions)
- **Message Queue**: RabbitMQ 4 (asynchronous code execution)
- **Code Execution**: Docker containers with security hardening
- **Observability**: Prometheus, Grafana, Loki
- **Containerization**: Docker & Docker Compose

## Architecture

GitGud uses a **monolithic Spring Boot application** for the MVP. The backend contains all services (auth, learning, gamification, code execution) within a single application, with plans to extract into microservices in future phases. Infrastructure services (PostgreSQL, Redis, RabbitMQ, Keycloak) run in Docker containers, while the backend and frontend run locally during development for faster iteration.

## Prerequisites

Before you begin, ensure you have the following installed:

- **Docker Desktop** (latest stable version)
- **Docker Compose** v2.x
- **Node.js** 20+ and npm
- **Java 25 JDK**
- **Maven** (for backend builds)
- **IDE**: IntelliJ IDEA (recommended for backend) and/or VSCode

## Quick Start

### 1. Clone the Repository

```bash
git clone <repository-url>
cd GitGud
```

### 2. Set Up Environment Variables

```bash
cp .env.example .env
```

The `.env` file contains all configuration for your local development environment. The default values work out of the box, but you can customize:
- Database credentials
- Service ports
- OAuth client IDs/secrets
- JWT secrets
- Logging levels

**Important**:
- Docker Compose automatically reads `.env` for all service configurations
- The Spring Boot backend also loads `.env` via the `spring-dotenv` library
- All services use the same credentials from this single file!

### 3. Start Infrastructure Services

Start all infrastructure services (PostgreSQL, Redis, RabbitMQ, Keycloak):

```bash
docker-compose up -d
```

This will start:
- **PostgreSQL** on `localhost:5432`
- **Redis** on `localhost:6379`
- **RabbitMQ** on `localhost:5672` (management UI at `http://localhost:15672`)
- **Keycloak** on `http://localhost:8180` (admin console)
- **Loki** (log aggregation) on `localhost:3100`
- **Prometheus** (metrics collection) on `localhost:9090`
- **Grafana** (observability dashboards) on `http://localhost:3001`
- **Promtail** (log shipper for Loki)

### 4. Verify Services are Running

```bash
docker-compose ps
```

All services should show as "healthy" or "running".

### 5. Start Backend (via IDE)

#### Using IntelliJ IDEA:
1. Open the `backend` directory as a Maven project
2. Let Maven download dependencies
3. Run the `BackendApplication` main class
4. Backend will start on `http://localhost:8080`

#### Using Command Line:
```bash
cd backend
./mvnw spring-boot:run
```

### 6. Start Frontend (via IDE or CLI)

```bash
cd frontend
npm install
npm run dev
```

Frontend will start on `http://localhost:5173`

## Service URLs

| Service | URL | Credentials |
|---------|-----|-------------|
| Frontend | http://localhost:5173 | - |
| Backend API | http://localhost:8080 | - |
| PostgreSQL | localhost:5432 | `admin` / `password` |
| Redis | localhost:6379 | (no auth) |
| RabbitMQ Management | http://localhost:15672 | `admin` / `password` |
| Keycloak Admin | http://localhost:8180 | `admin` / `password` |
| Grafana | http://localhost:3001 | `admin` / `admin` |
| Prometheus | http://localhost:9090 | (no auth) |
| Loki | http://localhost:3100 | (no auth) |

## Project Structure

```
GitGud/
├── backend/                            # Spring Boot monolithic application
│   ├── src/main/java/.../backend/
│   │   ├── controller/                 # REST endpoints
│   │   │   ├── auth/                   # User authentication and profile
│   │   │   ├── learning/               # Modules, lessons, progress
│   │   │   ├── gamification/           # XP, achievements, stats
│   │   │   └── execution/              # Code execution API
│   │   ├── service/                    # Business logic
│   │   │   └── execution/              # Code execution, Docker orchestration, output comparison
│   │   ├── model/                      # JPA entities (10 tables, UUID keys)
│   │   ├── repository/                 # Spring Data JPA repositories
│   │   ├── dto/                        # Request/response DTOs
│   │   │   └── execution/              # Code execution DTOs
│   │   ├── exception/                  # Custom exceptions & GlobalExceptionHandler
│   │   ├── config/                     # Security, CORS, RabbitMQ, Redis, Docker
│   │   └── security/                   # Keycloak JWT converter, AuthenticationUtil
│   ├── src/test/resources/             # Test resources
│   │   └── malicious-code-tests/       # Security test cases
│   └── pom.xml
├── frontend/                           # React SPA with JavaScript
├── docker/                             # Docker configurations
│   ├── java-executor/                  # Hardened Java execution container
│   └── keycloak/                       # Keycloak realm configuration
├── dev-docs/                           # Development documentation
│   ├── MVP-Development-Guide.md        # Full development roadmap
│   ├── CodeExecutionAPI.md             # API reference
│   └── DockerCodeExecutionResearch.md  # Security research
├── observability/                      # Grafana/Prometheus/Loki configs
├── docker-compose.yml                  # Infrastructure services
└── .env.example                        # Environment template
```

## Development Workflow

### Starting Work
```bash
# Start infrastructure
docker-compose up -d

# macOS users: Start Docker socket forwarding (in a separate terminal)
socat TCP-LISTEN:2376,range=127.0.0.1/32,reuseaddr,fork UNIX-CLIENT:/var/run/docker.sock

# In separate terminals:
# Terminal 1: Backend
cd backend && ./mvnw spring-boot:run

# Terminal 2: Frontend
cd frontend && npm run dev
```

### Stopping Work
```bash
# Stop infrastructure services
docker-compose down

# Stop backend/frontend (Ctrl+C in their terminals)
```

### Resetting Database
```bash
# Stop all services
docker-compose down

# Remove volumes (WARNING: deletes all data)
docker-compose down -v

# Start fresh
docker-compose up -d
```

### Viewing Logs
```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f postgres
docker-compose logs -f rabbitmq
```

## Observability & Monitoring

GitGud includes a comprehensive observability stack with Prometheus, Grafana, and Loki.

### Accessing Grafana

1. Navigate to http://localhost:3001
2. Login with `admin` / `admin`
3. Navigate to **Connections** → **Data Sources** to verify:
   - Prometheus datasource is configured (metrics)
   - Loki datasource is configured (logs)

### Viewing Logs in Grafana

1. Click **Explore** in the left sidebar
2. Select **Loki** as the data source
3. Use LogQL queries to search logs:
   ```
   {application="gitgud-backend"}
   {application="gitgud-backend", level="ERROR"}
   {application="gitgud-backend"} |= "exception"
   ```

### Viewing Metrics in Grafana

1. Click **Explore** in the left sidebar
2. Select **Prometheus** as the data source
3. Browse available metrics or use PromQL queries:
   ```
   jvm_memory_used_bytes{application="gitgud-backend"}
   http_server_requests_seconds_count{application="gitgud-backend"}
   system_cpu_usage{application="gitgud-backend"}
   ```

### Creating Dashboards

You can create custom dashboards in Grafana or import community dashboards:
- Spring Boot 2.1 System Monitor (ID: 11378)
- JVM (Micrometer) (ID: 4701)

## Database Access

### Using psql CLI
```bash
# Connect to PostgreSQL
docker exec -it gitgud-postgres psql -U admin -d gitgud
```

### Using IntelliJ IDEA Database Tools
Use IntelliJ IDEA's built-in database tools to connect to PostgreSQL:
- **Host**: localhost
- **Port**: 5432
- **Database**: gitgud
- **Username**: admin
- **Password**: password

## Code Execution

GitGud executes user-submitted Java code in hardened Docker containers with comprehensive security measures.

### Quick Start

**Build the execution container:**
```bash
cd docker/java-executor
docker build -t gitgud-java-executor:latest .
```

**macOS Users - Docker Socket Setup:**

If you're on macOS, Docker Desktop uses a Unix socket that may not be accessible via TCP. You'll need to forward the Docker socket to TCP using `socat`:

```bash
# Install socat
brew install socat

# Forward Docker socket to TCP (run in a separate terminal and keep it running)
socat TCP-LISTEN:2376,range=127.0.0.1/32,reuseaddr,fork UNIX-CLIENT:/var/run/docker.sock
```

**Note**: Keep the `socat` command running in a separate terminal while the backend is running. The backend is configured to connect to Docker via `tcp://localhost:2376` (see `application.yml`).

**Submit code for execution:**
```bash
curl -X POST http://localhost:8080/api/v1/execute/run \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "language": "java",
    "sourceCode": "public class Main { public static void main(String[] args) { System.out.println(\"Hello\"); } }",
    "lessonId": "YOUR_LESSON_UUID"
  }'
```

**Poll for results:**
```bash
curl http://localhost:8080/api/v1/execute/result/JOB_ID \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Security Features

All code runs in isolated Docker containers with:
- **Network isolation**: No internet access
- **Memory limits**: 256MB max (no swap)
- **CPU limits**: 0.5 cores
- **Timeout**: 5 seconds hard limit
- **Read-only filesystem**: Except /tmp (100MB tmpfs)
- **Process limits**: 50 max (prevents fork bombs)
- **No privileges**: All capabilities dropped, no-new-privileges
- **Non-root execution**: Runs as UID 1000

### Test Cases

Security test cases are provided in `backend/src/test/resources/malicious-code-tests/`:
- Infinite loops (timeout test)
- Memory bombs (memory limit test)
- Fork bombs (PID limit test)
- Network access attempts (network isolation test)
- File system writes (read-only filesystem test)
- Huge output (output truncation test)

See `dev-docs/CodeExecutionAPI.md` for complete API documentation.

## Keycloak Setup

After starting Keycloak for the first time:

1. Access admin console: http://localhost:8180
2. Login with `admin` / `admin`
3. Create a new realm called `gitgud`
4. Create a client called `gitgud-backend`
5. Configure OAuth2 settings (detailed guide TBD)

## Troubleshooting

### Port Already in Use
If you see port conflict errors:
```bash
# Check what's using the port (example for 5432)
lsof -i :5432

# Kill the process or change the port in docker-compose.yml
```

### Services Not Starting
```bash
# Check logs
docker-compose logs

# Restart specific service
docker-compose restart postgres

# Rebuild and restart
docker-compose up -d --force-recreate
```

### Backend Can't Connect to Database
1. Ensure PostgreSQL is healthy: `docker-compose ps`
2. Check connection string in `application.properties`
3. Verify database credentials match `.env` file

### Frontend Can't Connect to Backend
1. Ensure backend is running on port 8080
2. Check `VITE_API_BASE_URL` in `.env`
3. Verify CORS configuration in backend

## Docker Compose Commands Reference

```bash
# Start all services
docker-compose up -d

# Stop all services
docker-compose down

# Stop and remove volumes (deletes data)
docker-compose down -v

# View running services
docker-compose ps

# View logs
docker-compose logs -f

# Restart a service
docker-compose restart <service-name>

# Rebuild a service
docker-compose up -d --build <service-name>

# Execute command in container
docker-compose exec <service-name> <command>
```

## Development Status

**Current Phase**: MVP Phase 1A - Foundation (Local Development)

### ✅ Completed (Steps 1-5)

#### Infrastructure & Backend Foundation (Steps 1-2)
- ✅ Docker Compose with PostgreSQL, Redis, RabbitMQ, Keycloak
- ✅ Observability stack (Prometheus, Grafana, Loki)
- ✅ Database schema with 10 JPA entities using UUID keys
- ✅ Spring Boot monolithic application structure
- ✅ REST API with controllers, DTOs, global exception handling
- ✅ CORS configuration and request logging
- ✅ Rate limiting with Bucket4j
- ✅ Keycloak OAuth2/OIDC authentication integration
- ✅ User synchronization (Keycloak → local database)
- ✅ JWT token validation and user context management

#### Learning Module (Step 3)
- ✅ Module and Lesson management APIs
- ✅ User progress tracking
- ✅ Test case system with hidden/visible test cases
- ✅ DevDataBootstrapper for sample data seeding

#### Gamification System (Step 4)
- ✅ XP and leveling system
- ✅ Achievement system with criteria checking
- ✅ User stats tracking (streak, total XP, level)
- ✅ User profiles with avatar selection
- ✅ Level-based progression and unlocks

#### Code Execution Module (Step 5) ⭐️
- ✅ **Secure Docker sandbox** with comprehensive hardening:
  - Network isolation (--network none)
  - Memory limits (256MB, no swap)
  - CPU limits (0.5 cores)
  - PID limits (50 processes to prevent fork bombs)
  - Read-only filesystem with tmpfs
  - Non-root user execution
  - 5-second timeout enforcement
  - All capabilities dropped
- ✅ **RabbitMQ-based asynchronous job processing**
- ✅ **Redis caching** for execution results (1-hour TTL)
- ✅ **Advanced output comparison**:
  - Whitespace normalization (line endings, spacing)
  - Numeric tolerance for floating-point values
  - Exact match mode
- ✅ **Partial credit XP system** (proportional to tests passed)
- ✅ **Compilation and runtime error handling**
- ✅ **Timeout and resource limit enforcement**
- ✅ **Output truncation** (10KB limit to prevent memory exhaustion)
- ✅ **Database submission tracking** (all code submissions persisted)
- ✅ **Security testing** (infinite loops, memory bombs, network attempts, etc.)
- ✅ **REST API** with job submission and polling

### 📚 Documentation
- ✅ Complete API documentation (`dev-docs/CodeExecutionAPI.md`)
- ✅ Docker security research and hardening guide
- ✅ MVP development guide with detailed task checklist
- ✅ Malicious code security test cases with README

### 🔜 Next Steps (Step 6+)
- Frontend React application
- Lesson content creation (Java fundamentals, Spring Boot basics)
- Integration testing
- User interface and Monaco editor integration

See `dev-docs/MVP-Development-Guide.md` for detailed development roadmap.
See `dev-docs/CodeExecutionAPI.md` for code execution API reference.

## Contributing

This project is currently in early development. Contribution guidelines will be added soon.

## License

TBD

## Contact

TBD
