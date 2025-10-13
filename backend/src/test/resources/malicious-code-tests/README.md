# Malicious Code Security Tests

This directory contains test cases to verify that the Docker sandbox properly handles malicious code attempts.

## Test Cases

### 1. InfiniteLoop.java
**Attack**: Infinite loop to consume CPU indefinitely
**Expected**: Execution terminates after 5-second timeout
**Security Measure**: Timeout enforcement via Docker API

### 2. MemoryBomb.java
**Attack**: Allocate excessive memory to exhaust system resources
**Expected**: Process killed when exceeding 256MB memory limit
**Security Measure**: Memory limit (--memory=256m --memory-swap=256m)

### 3. ForkBomb.java
**Attack**: Create many processes to exhaust system resources
**Expected**: Process creation fails after 50 processes
**Security Measure**: PID limit (--pids-limit=50)

### 4. NetworkAccess.java
**Attack**: Attempt to access external network
**Expected**: Network connection fails (no route to host)
**Security Measure**: Network isolation (--network none)

### 5. FileWrite.java
**Attack**: Attempt to write to filesystem outside /tmp
**Expected**: Write to /etc fails (read-only), write to /tmp succeeds
**Security Measure**: Read-only filesystem (--read-only) with tmpfs for /tmp

### 6. HugeOutput.java
**Attack**: Generate massive output to exhaust memory
**Expected**: Output truncated to 10KB with truncation message
**Security Measure**: Output truncation in DockerExecutorService

## How to Run Tests

These tests can be run manually or integrated into automated tests:

### Manual Testing

```bash
# Build the Docker image first
cd /Users/heath/Repos/personal/GitGud/docker/java-executor
docker build -t gitgud-java-executor:latest .

# Test each scenario using DockerExecutorService through the API
# Start the backend application
./mvnw spring-boot:run

# Submit code via API (requires authentication)
# POST /api/v1/execute/run
# Then poll GET /api/v1/execute/result/{jobId}
```

### Automated Testing

Create integration tests in `DockerExecutorServiceTest.java`:

```java
@SpringBootTest
class DockerExecutorServiceTest {
    @Autowired
    private DockerExecutorService executorService;

    @Test
    void testInfiniteLoopTimeout() {
        String code = loadTestFile("InfiniteLoop.java");
        ExecutionOutput output = executorService.executeJavaCode(code, "");
        assertTrue(output.isTimeout());
    }

    @Test
    void testMemoryBomb() {
        String code = loadTestFile("MemoryBomb.java");
        ExecutionOutput output = executorService.executeJavaCode(code, "");
        // Should fail with OOM or be killed
        assertFalse(output.isSuccess());
    }

    @Test
    void testNetworkIsolation() {
        String code = loadTestFile("NetworkAccess.java");
        ExecutionOutput output = executorService.executeJavaCode(code, "");
        // Should complete but network access should fail
        assertTrue(output.getOutput().contains("Network access blocked"));
    }

    @Test
    void testOutputTruncation() {
        String code = loadTestFile("HugeOutput.java");
        ExecutionOutput output = executorService.executeJavaCode(code, "");
        assertTrue(output.getOutput().contains("output truncated"));
        assertTrue(output.getOutput().length() <= 10240 + 200); // 10KB + truncation message
    }
}
```

## Expected Results Summary

| Test | Expected Behavior | Security Measure |
|------|------------------|------------------|
| Infinite Loop | Timeout after 5s | Timeout enforcement |
| Memory Bomb | OOM or killed at 256MB | Memory limits |
| Fork Bomb | Process creation fails at 50 | PID limits |
| Network Access | Connection refused | Network isolation |
| File Write | Write to /etc fails | Read-only filesystem |
| Huge Output | Output truncated at 10KB | Output truncation |

## Security Hardening Applied

All containers run with the following security configuration:

```java
// Network isolation
.withNetworkMode("none")

// Memory limits (no swap)
.withMemory(256 * 1024 * 1024)
.withMemorySwap(256 * 1024 * 1024)

// CPU limit (0.5 cores)
.withNanoCPUs(500_000_000L)

// Process limit
.withPidsLimit(50L)

// Drop all Linux capabilities
.withCapDrop(Capability.ALL)

// Read-only root filesystem
.withReadonlyRootfs(true)

// Tmpfs for temporary files (100MB, noexec, nosuid)
.withTmpFs(Map.of(
    "/tmp", "rw,noexec,nosuid,size=100m",
    "/home/coderunner", "rw,noexec,nosuid,size=50m"
))

// Run as non-root user (UID 1000)
.withUser("1000:1000")

// Security options
.withSecurityOpts(List.of("no-new-privileges"))
```

## References

See `DockerCodeExecutionResearch.md` for detailed security analysis and recommendations.
