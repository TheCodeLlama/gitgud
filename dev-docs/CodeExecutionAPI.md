# Code Execution API Documentation

## Overview

The Code Execution API provides secure, sandboxed execution of Java code for the GitGud learning platform. All code is executed in hardened Docker containers with strict resource limits and security controls.

**Base URL**: `/api/v1/execute`

**Authentication**: Required (JWT Bearer token)

## Workflow

```
1. Submit Code → POST /execute/run
   ↓
   Returns: Job ID
   ↓
2. Poll Status → GET /execute/result/{jobId}
   ↓
   Status: QUEUED → RUNNING → COMPLETED/FAILED
   ↓
3. Retrieve Results
   ↓
   Get: Test results, XP awarded, execution time
```

## Security

All code execution happens in hardened Docker containers with:

- **Network Isolation**: No internet access (`--network none`)
- **Memory Limit**: 256MB (no swap)
- **CPU Limit**: 0.5 cores
- **Timeout**: 5 seconds hard limit
- **Filesystem**: Read-only (except /tmp with 100MB limit)
- **Privileges**: None (non-root user, all capabilities dropped)
- **Process Limit**: Maximum 50 processes (fork bomb prevention)

## Endpoints

### POST /execute/run

Submit code for execution.

**Request Headers**:
```
Authorization: Bearer <jwt-token>
Content-Type: application/json
```

**Request Body**:
```json
{
  "language": "java",
  "sourceCode": "public class Main {\n    public static void main(String[] args) {\n        System.out.println(\"Hello, World!\");\n    }\n}",
  "lessonId": "550e8400-e29b-41d4-a716-446655440000",
  "testCaseIds": [
    "660e8400-e29b-41d4-a716-446655440001",
    "660e8400-e29b-41d4-a716-446655440002"
  ]
}
```

**Field Descriptions**:
- `language` (required): Programming language. Currently only `"java"` is supported.
- `sourceCode` (required): Source code to execute. Maximum 50,000 characters.
- `lessonId` (required): UUID of the lesson this submission is for.
- `testCaseIds` (optional): Array of test case UUIDs. If omitted, all test cases for the lesson are used.

**Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "jobId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "status": "QUEUED",
    "message": "Code submitted successfully. Use job ID to check status."
  },
  "timestamp": "2025-10-13T10:00:00"
}
```

**Error Responses**:

400 Bad Request - Validation error:
```json
{
  "success": false,
  "error": "Source code must not exceed 50,000 characters",
  "timestamp": "2025-10-13T10:00:00"
}
```

400 Bad Request - Unsupported language:
```json
{
  "success": false,
  "error": "Unsupported language: python. Only Java is currently supported.",
  "timestamp": "2025-10-13T10:00:00"
}
```

404 Not Found - Invalid lesson:
```json
{
  "success": false,
  "error": "Lesson not found with id: 550e8400-e29b-41d4-a716-446655440000",
  "timestamp": "2025-10-13T10:00:00"
}
```

401 Unauthorized - Missing/invalid token:
```json
{
  "success": false,
  "error": "Unauthorized",
  "timestamp": "2025-10-13T10:00:00"
}
```

---

### GET /execute/result/{jobId}

Get execution results for a submitted job.

**Request Headers**:
```
Authorization: Bearer <jwt-token>
```

**Path Parameters**:
- `jobId` (required): Job ID returned from `/execute/run` endpoint

**Response - Job Queued** (200 OK):
```json
{
  "success": true,
  "data": {
    "jobId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "status": "QUEUED",
    "startedAt": "2025-10-13T10:00:00"
  },
  "timestamp": "2025-10-13T10:00:01"
}
```

**Response - Job Running** (200 OK):
```json
{
  "success": true,
  "data": {
    "jobId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "status": "RUNNING",
    "startedAt": "2025-10-13T10:00:00"
  },
  "timestamp": "2025-10-13T10:00:02"
}
```

**Response - Job Completed (Success)** (200 OK):
```json
{
  "success": true,
  "data": {
    "jobId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "status": "COMPLETED",
    "passed": true,
    "testsPassed": 5,
    "totalTests": 5,
    "testCaseResults": [
      {
        "testCaseId": "660e8400-e29b-41d4-a716-446655440001",
        "passed": true,
        "input": "5",
        "expectedOutput": "25",
        "actualOutput": "25",
        "errorMessage": null,
        "executionTimeMs": 123
      },
      {
        "testCaseId": "660e8400-e29b-41d4-a716-446655440002",
        "passed": true,
        "input": "10",
        "expectedOutput": "100",
        "actualOutput": "100",
        "errorMessage": null,
        "executionTimeMs": 115
      }
    ],
    "executionTimeMs": 1234,
    "xpAwarded": 50,
    "startedAt": "2025-10-13T10:00:00",
    "completedAt": "2025-10-13T10:00:01"
  },
  "timestamp": "2025-10-13T10:00:03"
}
```

**Response - Job Completed (Failed)** (200 OK):
```json
{
  "success": true,
  "data": {
    "jobId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "status": "COMPLETED",
    "passed": false,
    "testsPassed": 3,
    "totalTests": 5,
    "testCaseResults": [
      {
        "testCaseId": "660e8400-e29b-41d4-a716-446655440001",
        "passed": true,
        "input": "5",
        "expectedOutput": "25",
        "actualOutput": "25",
        "errorMessage": null,
        "executionTimeMs": 123
      },
      {
        "testCaseId": "660e8400-e29b-41d4-a716-446655440002",
        "passed": false,
        "input": "10",
        "expectedOutput": "100",
        "actualOutput": "10",
        "errorMessage": "Output does not match expected",
        "executionTimeMs": 115
      }
    ],
    "executionTimeMs": 1234,
    "xpAwarded": 0,
    "startedAt": "2025-10-13T10:00:00",
    "completedAt": "2025-10-13T10:00:01"
  },
  "timestamp": "2025-10-13T10:00:03"
}
```

**Response - Job Failed (Compilation Error)** (200 OK):
```json
{
  "success": true,
  "data": {
    "jobId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "status": "FAILED",
    "passed": false,
    "errorMessage": "Compilation error: Main.java:5: error: ';' expected",
    "compilationOutput": "Main.java:5: error: ';' expected\n    System.out.println(\"test\")\n                              ^\n1 error\n",
    "startedAt": "2025-10-13T10:00:00",
    "completedAt": "2025-10-13T10:00:01"
  },
  "timestamp": "2025-10-13T10:00:03"
}
```

**Response - Job Failed (Timeout)** (200 OK):
```json
{
  "success": true,
  "data": {
    "jobId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "status": "FAILED",
    "passed": false,
    "errorMessage": "Execution timed out",
    "executionTimeMs": 5000,
    "startedAt": "2025-10-13T10:00:00",
    "completedAt": "2025-10-13T10:00:05"
  },
  "timestamp": "2025-10-13T10:00:05"
}
```

**Error Responses**:

404 Not Found - Invalid job ID:
```json
{
  "success": false,
  "error": "No execution found with job ID: invalid-job-id",
  "timestamp": "2025-10-13T10:00:00"
}
```

401 Unauthorized:
```json
{
  "success": false,
  "error": "Unauthorized",
  "timestamp": "2025-10-13T10:00:00"
}
```

---

## Execution Status Flow

```
QUEUED → Job submitted, waiting in queue
   ↓
RUNNING → Job picked up by worker, code is executing
   ↓
COMPLETED → All test cases executed (may have passed or failed)
   OR
FAILED → Compilation error, timeout, or system error
```

## Polling Strategy

**Recommended approach:**

```javascript
async function submitAndWaitForResult(code, lessonId) {
  // 1. Submit code
  const submitResponse = await fetch('/api/v1/execute/run', {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      language: 'java',
      sourceCode: code,
      lessonId: lessonId
    })
  });

  const { data: { jobId } } = await submitResponse.json();

  // 2. Poll for results
  while (true) {
    const resultResponse = await fetch(`/api/v1/execute/result/${jobId}`, {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    });

    const { data: result } = await resultResponse.json();

    // 3. Check status
    if (result.status === 'COMPLETED' || result.status === 'FAILED') {
      return result;
    }

    // 4. Wait before polling again
    await new Promise(resolve => setTimeout(resolve, 1000)); // 1 second
  }
}
```

**Polling Guidelines:**
- Poll every 1-2 seconds while job is QUEUED or RUNNING
- Stop polling when status is COMPLETED or FAILED
- Set a maximum timeout (e.g., 30 seconds) to prevent infinite polling
- Results are cached in Redis for 1 hour after completion

---

## Rate Limiting

Rate limiting is applied per user:
- **Bucket size**: 20 requests
- **Refill rate**: 2 requests per second
- **Maximum**: 20 requests per 10-second window

If rate limit is exceeded:
```json
{
  "success": false,
  "error": "Rate limit exceeded",
  "timestamp": "2025-10-13T10:00:00"
}
```

---

## Code Limitations

### Source Code
- **Maximum size**: 50,000 characters
- **Language**: Java only (Java 25)
- **Class name**: Must contain a `Main` class with `main` method

### Execution Environment
- **Timeout**: 5 seconds maximum
- **Memory**: 256MB maximum
- **CPU**: 0.5 cores
- **Network**: Disabled (no internet access)
- **Filesystem**: Read-only (except /tmp)
- **Output**: Truncated to 10KB

### Prohibited Operations
The following operations are blocked by the sandbox:
- Network access (HTTP, sockets, etc.)
- File system writes (except /tmp)
- Process creation (limited to 50 processes)
- System calls (restricted via seccomp)
- Privilege escalation

---

## Example Use Cases

### 1. Simple Hello World

**Request**:
```json
{
  "language": "java",
  "sourceCode": "public class Main {\n    public static void main(String[] args) {\n        System.out.println(\"Hello, World!\");\n    }\n}",
  "lessonId": "550e8400-e29b-41d4-a716-446655440000"
}
```

### 2. Code with Input

**Request**:
```json
{
  "language": "java",
  "sourceCode": "import java.util.Scanner;\npublic class Main {\n    public static void main(String[] args) {\n        Scanner sc = new Scanner(System.in);\n        int n = sc.nextInt();\n        System.out.println(n * n);\n    }\n}",
  "lessonId": "550e8400-e29b-41d4-a716-446655440000"
}
```

### 3. Specific Test Cases

**Request**:
```json
{
  "language": "java",
  "sourceCode": "...",
  "lessonId": "550e8400-e29b-41d4-a716-446655440000",
  "testCaseIds": [
    "660e8400-e29b-41d4-a716-446655440001",
    "660e8400-e29b-41d4-a716-446655440002"
  ]
}
```

---

## Troubleshooting

### Compilation Errors
- Ensure code contains `public class Main`
- Check for syntax errors
- Verify all imports are correct
- Java version: Java 25

### Timeout Errors
- Avoid infinite loops
- Optimize algorithms
- 5-second limit is enforced strictly

### Memory Errors
- 256MB limit includes JVM overhead (~50-100MB)
- Avoid creating large arrays or collections
- Use efficient algorithms

### Output Truncation
- Output is limited to 10KB
- Use targeted print statements
- Avoid printing large data structures

---

## Testing

Use curl or Postman to test the API:

```bash
# Submit code
curl -X POST http://localhost:8080/api/v1/execute/run \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "language": "java",
    "sourceCode": "public class Main { public static void main(String[] args) { System.out.println(\"Hello\"); } }",
    "lessonId": "550e8400-e29b-41d4-a716-446655440000"
  }'

# Get result
curl -X GET http://localhost:8080/api/v1/execute/result/JOB_ID \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

## Support

For issues or questions:
- Check the security test cases in `/backend/src/test/resources/malicious-code-tests/`
- Review Docker sandbox configuration in `DockerExecutorService.java`
- See `DockerCodeExecutionResearch.md` for security details
