# Docker Code Execution Research
## Secure Sandboxing for Untrusted Java Code Execution

**Project**: GitGud - Gamified Java Spring Learning Platform
**Research Date**: October 2025
**Purpose**: Evaluate secure approaches for executing untrusted user-submitted Java code in an isolated sandbox environment

---

## Table of Contents

1. [Executive Summary](#executive-summary)
2. [Project Context](#project-context)
3. [Docker-in-Docker (DinD) Security Analysis](#docker-in-docker-dind-security-analysis)
4. [Docker Socket Mounting Security](#docker-socket-mounting-security)
5. [Container Isolation Technologies](#container-isolation-technologies)
6. [Security Mechanisms and Hardening](#security-mechanisms-and-hardening)
7. [Resource Limits and Control Groups](#resource-limits-and-control-groups)
8. [Network Isolation](#network-isolation)
9. [User Namespace Remapping and Rootless Containers](#user-namespace-remapping-and-rootless-containers)
10. [Container Breakout Vulnerabilities](#container-breakout-vulnerabilities)
11. [Industry Implementations](#industry-implementations)
12. [Alternative Sandboxing Technologies](#alternative-sandboxing-technologies)
13. [Java-Specific Security Considerations](#java-specific-security-considerations)
14. [Recommended Architecture](#recommended-architecture)
15. [Implementation Roadmap](#implementation-roadmap)
16. [Security Checklist](#security-checklist)
17. [References and Further Reading](#references-and-further-reading)

---

## Executive Summary

This document presents comprehensive research on secure code execution architectures for the GitGud platform, which requires executing untrusted Java code submitted by users in a safe, isolated environment.

### Key Findings

1. **Docker-in-Docker (DinD) is NOT RECOMMENDED** due to significant security risks and complexity
2. **Docker Socket Mounting is HIGHLY DANGEROUS** and effectively grants root access to the host
3. **Standard Docker containers with proper hardening** provide adequate security for MVP phase
4. **Advanced isolation technologies** (gVisor, Kata Containers, Firecracker) offer enhanced security for production scale
5. **Multiple security layers** are essential: seccomp, AppArmor/SELinux, resource limits, network isolation, user namespace remapping

### Recommended Approach

For the MVP phase, use **standard Docker containers** with comprehensive security hardening:
- Run containers with minimal privileges (`--cap-drop ALL`)
- Enforce strict resource limits (CPU, memory, timeout)
- Complete network isolation (`--network none`)
- Read-only filesystem with tmpfs for temporary storage
- User namespace remapping or rootless containers
- Seccomp and AppArmor/SELinux profiles
- No mounting of Docker socket under any circumstances

For production scale, consider migrating to **gVisor (runsc)** or **Firecracker microVMs** for hardware-level isolation while maintaining container-like performance.

---

## Project Context

### Requirements from MVP-Development-Guide.md

The GitGud platform requires a code execution service with the following characteristics:

**Functional Requirements:**
- Execute user-submitted Java 25 code
- Compile and run code against test cases
- Capture stdout, stderr, and execution results
- Return results within 5 seconds
- Support concurrent executions (1000+ users)

**Security Requirements:**
- Sandboxed execution environment
- Resource limits (CPU, memory, time)
- Network isolation
- No file system access outside designated areas
- Protection against malicious code (infinite loops, memory bombs, kernel exploits)

**Scalability Requirements:**
- Queue-based processing (RabbitMQ)
- Horizontal scaling capability
- Container lifecycle management
- Fast startup times

---

## Docker-in-Docker (DinD) Security Analysis

### What is Docker-in-Docker?

Docker-in-Docker (DinD) involves running a Docker daemon inside a Docker container, allowing that container to create and manage its own child containers.

### Architecture

```
Host Machine (Docker Daemon)
└── Parent Container (Docker Daemon)
    ├── Child Container 1 (User Code)
    ├── Child Container 2 (User Code)
    └── Child Container N (User Code)
```

### Security Risks

#### 1. Privileged Container Requirement

DinD requires running the parent container in **privileged mode** (`--privileged`), which:
- Disables all security constraints
- Grants access to all host devices
- Allows direct access to the host kernel
- Bypasses AppArmor/SELinux protection
- Permits mounting host filesystems
- Effectively provides root access to the host

**Risk Level**: CRITICAL

#### 2. CVE-2025-9074 Container Escape

A critical vulnerability (CVSS 9.3) was discovered in Docker Desktop 4.44.3 where:
- Malicious containers can access the Docker Engine API via subnet (192.168.65.7:2375)
- Access occurs even with "Enhanced Container Isolation" enabled
- No Docker socket mounting is required for exploitation
- Attackers can launch additional containers and access host files

**Risk Level**: CRITICAL

#### 3. Shared Kernel Vulnerabilities

DinD containers share the host kernel, making them vulnerable to:
- **CVE-2022-0185**: Linux kernel vulnerability allowing unprivileged container escape
- **CVE-2022-0847 (Dirty Pipe)**: Write to read-only files and escalate privileges
- **CVE-2019-5736**: RunC vulnerability enabling host root access via binary overwrite
- **CVE-2021-3490**: Container escape via eBPF vulnerabilities

**Risk Level**: HIGH

#### 4. Complexity and Attack Surface

DinD introduces additional complexity:
- Two Docker daemons to secure and maintain
- Nested container orchestration
- Complex networking configurations
- Increased attack surface area

**Risk Level**: MEDIUM-HIGH

### Use Cases Where DinD Might Be Acceptable

DinD is primarily designed for:
- CI/CD pipelines (GitLab CI, Jenkins)
- Building Docker images in containerized environments
- Docker testing and development

**NOT RECOMMENDED FOR**: Production code execution of untrusted user code

### Verdict: NOT RECOMMENDED

Docker-in-Docker should **NOT** be used for the GitGud platform due to:
1. Requirement for privileged containers
2. Critical security vulnerabilities
3. Shared kernel risks
4. Unnecessary complexity
5. Better alternatives available

---

## Docker Socket Mounting Security

### What is Docker Socket Mounting?

Mounting the Docker socket (`/var/run/docker.sock`) into a container allows processes inside that container to communicate with the host's Docker daemon and execute Docker commands.

```bash
docker run -v /var/run/docker.sock:/var/run/docker.sock myimage
```

### How It Works

```
Container Process
    ↓
/var/run/docker.sock (mounted)
    ↓
Host Docker Daemon
    ↓
Full control over all containers and host
```

### Security Implications

#### Critical Security Risk: Root Equivalence

**Any process that can write to the Docker socket effectively has root access on the host.**

This is because:
1. The container can launch new containers with `--privileged` flag
2. The container can mount the entire host filesystem (`-v /:/host`)
3. The container can execute commands as root on the host
4. The container can manipulate all other running containers

#### Example Container Escape

```bash
# From inside a container with Docker socket access:
docker run -it --privileged -v /:/host alpine chroot /host
# Now you have root shell on the host
```

#### Read-Only Mounting Is Insufficient

Mounting the socket as read-only (`ro`) provides minimal protection:
- Still allows inspection of all containers
- Can read environment variables (may contain secrets)
- Can access logs containing sensitive information
- Makes exploitation harder but not impossible

**Risk Level**: CRITICAL

### OWASP and Docker Official Guidance

**OWASP Docker Security Cheat Sheet**:
> "Do not expose `/var/run/docker.sock` to other containers. If you are running your docker image with `-v /var/run/docker.sock://var/run/docker.sock` or similar, you should change it."

**Docker Official Documentation**:
> "Enhanced Container Isolation blocks containers from mounting the Docker socket to prevent malicious access to Docker Engine."

### Legitimate Use Cases

Docker socket mounting is used by:
- **Portainer**: Docker management UI
- **Traefik**: Reverse proxy with automatic service discovery
- **Watchtower**: Automatic container updates

These tools are **trusted applications** running in controlled environments, NOT untrusted user code.

### Alternative Approaches

Instead of mounting the Docker socket, consider:

1. **Docker Context with SSH**: Connect to remote Docker daemon over SSH
2. **Docker API over HTTPS**: Secure API access with TLS certificates
3. **Docker SDK/API from host**: Execute Docker commands from application running on host
4. **Separate orchestration service**: Dedicated service managing container lifecycle

### Verdict: NEVER USE FOR UNTRUSTED CODE

Mounting the Docker socket should **NEVER** be used for executing untrusted user code. It provides direct root access to the host and defeats all container isolation.

---

## Container Isolation Technologies

### Standard Docker Isolation

Docker provides isolation through Linux kernel features:

#### 1. Namespaces

Provide process, network, mount, IPC, UTS, and user isolation:
- **PID namespace**: Isolated process tree
- **Network namespace**: Separate network stack
- **Mount namespace**: Isolated filesystem view
- **IPC namespace**: Separate inter-process communication
- **UTS namespace**: Separate hostname
- **User namespace**: UID/GID remapping

#### 2. Control Groups (cgroups)

Limit and isolate resource usage:
- CPU allocation
- Memory limits
- Disk I/O
- Network bandwidth

#### 3. Capabilities

Fine-grained privilege control beyond root/non-root:
- Drop unnecessary capabilities
- Run with minimal required permissions

#### 4. Security Modules

Additional mandatory access control:
- **AppArmor** (Ubuntu/Debian)
- **SELinux** (RHEL/CentOS)

### Limitations of Standard Docker Isolation

1. **Shared Kernel**: All containers share the host kernel
   - Kernel vulnerabilities affect all containers
   - Syscall exploits can lead to container escape

2. **Default Configuration**: Not secure enough for untrusted code
   - Many capabilities enabled by default
   - No seccomp profile by default in Kubernetes
   - Network access enabled

3. **Attack Surface**: Large Linux kernel attack surface
   - 300+ system calls available
   - Kernel bugs can be exploited from containers

### Recommended Approach

Standard Docker isolation is **adequate for MVP** when combined with:
- Comprehensive hardening (see Security Mechanisms section)
- Multiple layers of defense
- Regular security updates
- Monitoring and incident response

---

## Security Mechanisms and Hardening

### 1. Seccomp (Secure Computing Mode)

Seccomp restricts which system calls a process can invoke.

#### How It Works

Linux provides 300+ system calls. Seccomp allows filtering to a safe subset.

#### Docker's Default Seccomp Profile

Docker provides a default seccomp profile that:
- Blocks ~44 dangerous system calls
- Allows most common operations
- Provides moderate protection with broad compatibility

**Blocked syscalls include**:
- `ptrace` (process tracing)
- `kexec_load` (kernel loading)
- `module_load` (kernel module loading)
- `mount` (filesystem mounting)
- `reboot` (system reboot)

#### Custom Seccomp Profile

For maximum security, create a minimal seccomp profile:

```json
{
  "defaultAction": "SCMP_ACT_ERRNO",
  "architectures": ["SCMP_ARCH_X86_64"],
  "syscalls": [
    {
      "names": [
        "read", "write", "open", "close", "stat", "fstat",
        "mmap", "munmap", "brk", "exit", "exit_group",
        "execve", "wait4", "kill", "sigaction", "sigreturn"
      ],
      "action": "SCMP_ACT_ALLOW"
    }
  ]
}
```

Apply with:
```bash
docker run --security-opt seccomp=profile.json myimage
```

#### Benefits for Code Execution

- Prevents privilege escalation via syscalls
- Blocks kernel exploitation attempts
- Reduces attack surface significantly
- Low performance overhead

**Recommendation**: Use custom restrictive seccomp profile for Java code execution

### 2. AppArmor (Application Armor)

AppArmor restricts which resources a process can access.

#### How It Works

AppArmor uses profiles to define:
- File access permissions (read, write, execute)
- Network access rules
- Capability restrictions

#### Docker's Default AppArmor Profile

Docker automatically loads `docker-default` profile:
- Denies access to sensitive `/proc` and `/sys` paths
- Blocks mounting filesystems
- Restricts raw socket creation
- Denies module loading

#### Custom AppArmor Profile

Create restrictive profile for code execution:

```
#include <tunables/global>

profile docker-code-execution flags=(attach_disconnected,mediate_deleted) {
  #include <abstractions/base>

  # Deny network access
  deny network,

  # Allow only /tmp access
  /tmp/** rw,

  # Allow execution
  /usr/bin/java rix,

  # Deny everything else
  deny /** w,
}
```

Apply with:
```bash
docker run --security-opt apparmor=docker-code-execution myimage
```

**Recommendation**: Use custom AppArmor profile restricting file and network access

### 3. SELinux (Security-Enhanced Linux)

Alternative to AppArmor, used primarily on RHEL/CentOS systems.

#### How It Works

SELinux uses labels and policies:
- Every process has a security context (label)
- Every file/resource has a security context
- Policies define which contexts can access which resources

#### Docker SELinux Integration

Docker automatically sets:
- `svirt_sandbox_file_t` for container files
- `svirt_lxc_net_t` for container processes

Enable with:
```bash
docker run --security-opt label=type:svirt_sandbox_file_t myimage
```

**Recommendation**: Use SELinux if on RHEL/CentOS, otherwise use AppArmor

### 4. Capability Dropping

Linux capabilities provide fine-grained privilege control.

#### Docker Default Capabilities

By default, Docker grants these capabilities:
- CHOWN, DAC_OVERRIDE, FOWNER, FSETID
- KILL, SETGID, SETUID, SETPCAP
- NET_BIND_SERVICE, NET_RAW
- SYS_CHROOT, MKNOD, AUDIT_WRITE, SETFCAP

#### Drop All Capabilities

For code execution, drop ALL capabilities:

```bash
docker run --cap-drop ALL myimage
```

This removes even basic privileges, creating maximum isolation.

**Recommendation**: Drop all capabilities for untrusted code execution

### 5. No New Privileges Flag

Prevents privilege escalation via setuid/setgid binaries:

```bash
docker run --security-opt no-new-privileges myimage
```

**Recommendation**: Always use for untrusted code

### 6. Read-Only Filesystem

Prevent code from modifying the container filesystem:

```bash
docker run --read-only --tmpfs /tmp:rw,noexec,nosuid,size=100m myimage
```

Benefits:
- Prevents malware persistence
- Blocks file-based attacks
- Provides tmpfs for temporary files only

**Recommendation**: Use read-only filesystem with limited tmpfs

---

## Resource Limits and Control Groups

### Why Resource Limits Matter

Without limits, malicious code can:
- Consume all CPU (fork bombs, infinite loops)
- Exhaust memory (memory bombs, leaks)
- Fill disk space
- Starve other users

### Control Groups (cgroups)

Linux cgroups enforce resource limits at the kernel level.

#### How Enforcement Works

- **Memory limits**: Kernel OOM killer terminates process when limit exceeded
- **CPU limits**: Kernel throttles CPU access when limit reached
- **I/O limits**: Kernel restricts disk read/write rates

### Memory Limits

Set hard memory limit:

```bash
docker run --memory=256m --memory-swap=256m myimage
```

Flags:
- `--memory`: Maximum memory (RAM)
- `--memory-swap`: Total memory (RAM + swap). Set equal to RAM to disable swap
- `--memory-reservation`: Soft limit (allows bursting)
- `--oom-kill-disable`: Prevent OOM kills (DON'T USE for untrusted code)

**Recommendation**: 256MB hard limit for Java code execution

### CPU Limits

Limit CPU usage:

```bash
docker run --cpus=0.5 myimage
```

Flags:
- `--cpus`: Decimal number of CPUs (0.5 = 50% of one core)
- `--cpu-shares`: Relative weight (default 1024)
- `--cpuset-cpus`: Pin to specific CPU cores

**Recommendation**: 0.5 CPUs for Java code execution (prevents CPU exhaustion)

### Execution Timeout

Docker doesn't have built-in timeout, implement at orchestration layer:

```bash
timeout 5s docker run myimage
```

Or use Docker API with timeout parameter.

**Recommendation**: 5-second hard timeout for code execution

### Process Limits (PIDs)

Limit number of processes to prevent fork bombs:

```bash
docker run --pids-limit=50 myimage
```

**Recommendation**: 50 process limit

### Disk I/O Limits

Limit disk read/write:

```bash
docker run --device-read-bps /dev/sda:10mb --device-write-bps /dev/sda:10mb myimage
```

**Recommendation**: 10MB/s read/write limit

### Summary of Recommended Limits

| Resource | Limit | Purpose |
|----------|-------|---------|
| Memory | 256MB | Prevent memory exhaustion |
| Memory Swap | 256MB (no swap) | Prevent swap thrashing |
| CPU | 0.5 cores | Prevent CPU monopolization |
| Timeout | 5 seconds | Prevent infinite loops |
| PIDs | 50 | Prevent fork bombs |
| Disk I/O | 10MB/s | Prevent disk exhaustion |
| Tmpfs | 100MB | Limit temporary storage |

---

## Network Isolation

### Why Network Isolation Matters

Malicious code could:
- Make requests to internal services
- Exfiltrate data
- Download malware
- Participate in DDoS attacks
- Access cloud metadata endpoints (AWS, GCP, Azure)

### Docker Network Drivers

Docker provides several network drivers:

1. **bridge** (default): Containers can access external network
2. **host**: Share host's network (NEVER use for untrusted code)
3. **none**: Complete network isolation
4. **custom**: User-defined networks

### None Network Driver (RECOMMENDED)

Complete network isolation:

```bash
docker run --network none myimage
```

Effects:
- Only loopback interface (127.0.0.1) available
- No external network access
- No access to other containers
- No access to host services
- No DNS resolution

**Recommendation**: Use `--network none` for all code execution containers

### Verification

From inside container:
```bash
# Only loopback should be present
ip addr show

# These should fail
ping 8.8.8.8
curl google.com
```

### Alternative: Custom Network with Firewall

If network access is needed for specific use cases:

```bash
# Create isolated network
docker network create --internal code-execution

# Run container
docker run --network code-execution myimage
```

Then use iptables rules to control access.

**Recommendation**: For MVP, use `--network none` (complete isolation)

---

## User Namespace Remapping and Rootless Containers

### The Root Problem

By default:
- Process running as root (UID 0) inside container
- Is root (UID 0) on the host (from host perspective)
- If container escape occurs, attacker has root on host

### Solution 1: User Namespace Remapping

Remap container root to unprivileged host user:

```
Container UID 0 (root) → Host UID 100000 (unprivileged)
Container UID 1         → Host UID 100001
...
Container UID 65536     → Host UID 165536
```

#### How to Enable

Edit `/etc/docker/daemon.json`:
```json
{
  "userns-remap": "default"
}
```

Docker automatically creates `dockremap` user with subuid/subgid ranges.

#### Benefits

- Container root has no privileges on host
- Limits damage from container escape
- No changes to container images required
- Transparent to applications

#### Limitations

- Some features require host root (privileged operations)
- May have file permission issues with mounted volumes
- Slightly more complex troubleshooting

**Recommendation**: Enable user namespace remapping for production

### Solution 2: Rootless Containers

Run Docker daemon itself as non-root user:

```bash
dockerd-rootless.sh
```

#### Benefits

- Docker daemon doesn't have root privileges
- Even stronger isolation than userns-remap
- Containers and daemon both unprivileged

#### Limitations

- Some features unavailable (overlay networks, apparmor, etc.)
- Performance may be slightly lower
- More complex setup

**Recommendation**: Consider for maximum security in production

### Solution 3: Run as Non-Root User in Container

Simplest approach - don't run as root inside container:

Dockerfile:
```dockerfile
FROM openjdk:25-slim
RUN useradd -m -u 1000 coderunner
USER coderunner
WORKDIR /home/coderunner
```

Benefits:
- No root inside container
- Simple to implement
- Works everywhere

**Recommendation**: Always use non-root user in Dockerfile + userns-remap on host

---

## Container Breakout Vulnerabilities

### Understanding Container Escapes

A container escape (or breakout) occurs when:
1. Process inside container gains access to host
2. Can execute commands on host
3. Can access host filesystem
4. Achieves privilege escalation to host root

### Historical Critical CVEs

#### CVE-2025-9074 (2025) - CVSS 9.3

**Vulnerability**: Docker Desktop container escape via subnet access
**Impact**: Containers can access Docker Engine API without socket mounting
**Fix**: Update to Docker Desktop 4.44.3+
**Lesson**: Even "Enhanced Container Isolation" had bypass

#### CVE-2024-21626 (2024) - Leaky Vessels

**Vulnerability**: RunC process.cwd container breakout
**Impact**: Unauthorized access to host OS
**Fix**: Update runC and Docker
**Lesson**: Runtime vulnerabilities remain a threat

#### CVE-2022-0847 (2022) - Dirty Pipe

**Vulnerability**: Linux kernel pipe_buffer.flags logic flaw
**Impact**: Write to read-only files, privilege escalation
**Fix**: Kernel update to 5.16.11+, 5.15.25+, 5.10.102+
**Lesson**: Kernel vulnerabilities affect all containers

#### CVE-2022-0185 (2022) - Linux Kernel

**Vulnerability**: Heap overflow in kernel fsconfig syscall
**Impact**: Unprivileged container escape to root
**Fix**: Kernel update
**Lesson**: Syscall filtering with seccomp is critical

#### CVE-2021-3490 (2021) - eBPF Vulnerability

**Vulnerability**: eBPF ALU32 bounds tracking issue
**Impact**: Container escape with CAP_BPF or CAP_SYS_ADMIN
**Fix**: Kernel update
**Lesson**: Drop all capabilities

#### CVE-2019-5736 (2019) - RunC

**Vulnerability**: Overwrite host runC binary from container
**Impact**: Full root access on host
**Fix**: Update Docker to 18.09.2+
**Lesson**: Container runtime security is critical

#### CVE-2019-14271 (2019) - Docker CP

**Vulnerability**: Docker cp command implementation flaw
**Impact**: Full container escape
**Fix**: Update Docker
**Lesson**: Docker tooling itself can have vulnerabilities

### Common Exploitation Vectors

1. **Privileged Containers**: Complete access to host
2. **Docker Socket Mounting**: Root equivalent access
3. **Kernel Exploits**: Shared kernel vulnerabilities
4. **Capability Abuse**: Excessive capabilities enable escape
5. **Runtime Bugs**: Vulnerabilities in Docker/runC
6. **Symlink Attacks**: Malicious symlinks to host files

### Defense in Depth Strategy

No single defense is perfect. Use layers:

1. **Keep Everything Updated**
   - Docker/runC to latest version
   - Linux kernel with security patches
   - Base images with security updates

2. **Minimize Privileges**
   - Drop all capabilities
   - Run as non-root user
   - User namespace remapping
   - No privileged mode

3. **System Call Filtering**
   - Seccomp profile blocking dangerous syscalls
   - Prevents many kernel exploits

4. **Mandatory Access Control**
   - AppArmor or SELinux profiles
   - File and network restrictions

5. **Resource Limits**
   - Prevent resource exhaustion
   - Limit blast radius

6. **Network Isolation**
   - No network access
   - Prevents data exfiltration

7. **Monitoring and Detection**
   - Container behavior monitoring
   - Anomaly detection
   - Incident response plan

**Recommendation**: Implement all layers for production code execution

---

## Industry Implementations

### How LeetCode/HackerRank Work

Based on research and public information:

#### Architecture

```
User Submission
    ↓
API Gateway (validate, enqueue)
    ↓
Message Queue (RabbitMQ/SQS)
    ↓
Worker Pool (multiple execution servers)
    ↓
Docker Container (isolated execution)
    ↓
Test Runner (compare output)
    ↓
Result Queue
    ↓
Results returned to user (polling or WebSocket)
```

#### Key Characteristics

1. **Asynchronous Processing**: Submission returns immediately with job ID
2. **Polling**: Client polls every 1-2 seconds for results
3. **Containerization**: Each submission in separate Docker container
4. **Resource Limits**: CPU, memory, timeout enforced via cgroups
5. **Seccomp**: Limits dangerous system calls
6. **Network Isolation**: No external network access
7. **Language Agnostic**: stdin/stdout for all languages
8. **Disposable**: Containers destroyed after execution

#### Security Measures

- Resource limitations (CPU, memory, disk I/O)
- Network isolation
- File system restrictions
- Timeouts and execution limits
- Security profiles (seccomp)

#### Performance Considerations

- Pre-warmed containers for faster startup
- Container reuse for multiple submissions (with cleanup)
- Horizontal scaling of worker pool
- Queue-based load balancing

**Lesson**: Standard Docker with proper hardening is sufficient for large-scale code execution platforms

---

## Alternative Sandboxing Technologies

### 1. gVisor (runsc)

#### What is gVisor?

gVisor is an application kernel for containers that provides additional isolation layer:

```
User Application
    ↓
gVisor Application Kernel (userspace)
    ↓
Limited syscalls to host kernel
    ↓
Host Kernel
```

#### How It Works

- Intercepts ALL system calls from application
- Implements Linux API in userspace (Sentry)
- Only ~70 safe syscalls forwarded to host kernel
- Acts as "fake kernel" for containers

#### Security Benefits

1. **Reduced Attack Surface**: 70 syscalls vs 300+
2. **Additional Isolation Layer**: Application → gVisor → Kernel
3. **Syscall Filtering**: Built-in protection against kernel exploits
4. **Battle-Tested**: Used in production at Google

#### Production Use

- Google uses gVisor for untrusted workloads
- Compatible with Docker and Kubernetes
- OCI-compliant runtime (runsc)

#### Integration

```bash
# Install runsc
wget https://storage.googleapis.com/gvisor/releases/release/latest/x86_64/runsc
chmod +x runsc
sudo mv runsc /usr/local/bin/

# Configure Docker to use runsc
sudo dockerd --add-runtime=runsc=/usr/local/bin/runsc

# Run container with gVisor
docker run --runtime=runsc myimage
```

#### Performance

- Startup: Milliseconds (container-like)
- Overhead: 10-30% CPU overhead
- Memory: Minimal additional overhead

#### Limitations

- Slightly higher CPU usage
- Not all syscalls implemented (rare compatibility issues)
- More complex debugging

**Recommendation**: Excellent choice for production. Consider for post-MVP scaling.

### 2. Kata Containers

#### What is Kata Containers?

Lightweight VMs that feel like containers:

```
Container Process
    ↓
Lightweight VM (KVM)
    ↓
Guest Kernel (isolated)
    ↓
Hypervisor
    ↓
Host Kernel
```

#### How It Works

- Each container runs in its own lightweight VM
- Separate kernel per container
- Hardware virtualization (KVM/QEMU)
- OCI-compliant

#### Security Benefits

1. **Hardware Isolation**: Separate kernel per container
2. **VM-Level Security**: Stronger than namespace isolation
3. **Multi-Tenant Safe**: True isolation between tenants
4. **Kernel Exploits Contained**: Guest kernel exploits don't affect host

#### Performance

- Startup: ~100-200ms (slower than Docker, faster than VMs)
- Memory: ~20MB overhead per container
- CPU: Minimal overhead (~5%)

#### Integration

```bash
# Install Kata Containers
sudo apt-get install kata-containers

# Configure Docker
sudo dockerd --add-runtime=kata-runtime=/usr/bin/kata-runtime

# Run container with Kata
docker run --runtime=kata-runtime myimage
```

#### Limitations

- Higher memory overhead than Docker
- Slower startup than standard containers
- Requires nested virtualization in cloud environments

**Recommendation**: Excellent for maximum isolation. Consider for enterprise/production.

### 3. Firecracker MicroVMs

#### What is Firecracker?

Lightweight virtualization technology powering AWS Lambda:

```
User Application
    ↓
MicroVM (Firecracker)
    ↓
Minimal Device Model
    ↓
KVM
    ↓
Host Kernel
```

#### How It Works

- Minimal VMM (Virtual Machine Monitor)
- Only 50,000 lines of code (vs 1.5M for QEMU)
- Hardware virtualization (KVM)
- Designed for multi-tenant serverless

#### Security Benefits

1. **Minimal Attack Surface**: 96% smaller than QEMU
2. **Hardware Isolation**: KVM-based virtualization
3. **Battle-Tested**: Powers AWS Lambda and Fargate
4. **Multi-Tenant Security**: Designed for public cloud

#### Performance

- Startup: **125ms** from API call to process start
- Density: **150 microVMs per second** per host
- Memory: **<5MB overhead** per microVM
- Resource: High density on single host

#### Integration

Firecracker requires custom integration (not a drop-in Docker runtime):

```bash
# Start Firecracker VM
firecracker --api-sock /tmp/firecracker.sock --config-file vm_config.json
```

#### Limitations

- More complex integration than Docker
- Requires custom orchestration
- Not OCI-compliant (separate API)
- Steeper learning curve

**Recommendation**: Excellent for scale-out production. More complex than Docker. Consider for future phases.

### Comparison Matrix

| Technology | Security | Performance | Complexity | OCI Compatible | Best For |
|------------|----------|-------------|------------|----------------|----------|
| **Docker** | Good (with hardening) | Excellent | Low | Yes | MVP, fast development |
| **gVisor** | Excellent | Good (10-30% overhead) | Medium | Yes | Production, easy upgrade |
| **Kata Containers** | Excellent | Good (VM overhead) | Medium | Yes | Multi-tenant, high security |
| **Firecracker** | Excellent | Excellent | High | No | Scale, AWS Lambda-like |

---

## Java-Specific Security Considerations

### Java Security Manager (Deprecated)

**Important**: Java Security Manager was **deprecated in Java 17** and **removed in Java 21**.

For Java 25 (our target version), Security Manager is **NOT AVAILABLE**.

### Container-Based Isolation Only

Since Java Security Manager is removed, rely entirely on container security:

1. **Container isolation** (namespaces, cgroups)
2. **Seccomp** syscall filtering
3. **AppArmor/SELinux** mandatory access control
4. **Resource limits** (memory, CPU, timeout)
5. **Network isolation** (no network access)

### Java Memory Considerations

Java processes have higher baseline memory:
- JVM startup: ~50-100MB
- Recommended container memory: **256MB minimum**
- Use `java -Xmx` to limit heap: `-Xmx128m`

### Java Compilation

Two approaches:

#### Option 1: Pre-compiled Environment
- Base image includes JDK
- Compile user code at runtime
- Simpler, slower

#### Option 2: Pre-compiled Classes
- Compile before creating container
- Only JRE in container
- Faster, more complex

**Recommendation**: Option 1 for MVP (compile in container)

### Dangerous Java Features to Block

Even in containers, some Java features pose risks:

1. **Reflection**: Can bypass access controls
2. **ClassLoaders**: Can load arbitrary classes
3. **Native Methods (JNI)**: Bypass JVM security
4. **ProcessBuilder**: Launch external processes
5. **File I/O**: Read/write files

**Mitigation**: These are harder to block without Security Manager. Rely on:
- Read-only filesystem (blocks file writes)
- No network (blocks network I/O)
- Limited /tmp (restricted temp storage)
- Code review of test cases
- Input validation

### Example Java Dockerfile

```dockerfile
FROM openjdk:25-slim

# Create non-root user
RUN useradd -m -u 1000 coderunner && \
    mkdir /tmp/code && \
    chown coderunner:coderunner /tmp/code

# Switch to non-root user
USER coderunner
WORKDIR /home/coderunner

# Set Java memory limits
ENV JAVA_OPTS="-Xmx128m -Xms64m"

# Default command
CMD ["java"]
```

Run with:
```bash
docker run \
  --read-only \
  --tmpfs /tmp:rw,noexec,nosuid,size=100m \
  --network none \
  --memory=256m \
  --cpus=0.5 \
  --pids-limit=50 \
  --cap-drop ALL \
  --security-opt no-new-privileges \
  --security-opt seccomp=seccomp-profile.json \
  --security-opt apparmor=docker-code-execution \
  java-executor
```

---

## Recommended Architecture

### Phase 1: MVP with Standard Docker

#### Architecture Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                         User (Browser)                       │
└────────────────────────┬────────────────────────────────────┘
                         │
                         │ HTTPS
                         ▼
┌─────────────────────────────────────────────────────────────┐
│              Spring Boot API (Monolith)                      │
│  ┌────────────────────────────────────────────────────┐    │
│  │         CodeExecutionController                     │    │
│  │  POST /api/execute/run                             │    │
│  │  GET  /api/execute/result/{jobId}                  │    │
│  └────────────────────┬───────────────────────────────┘    │
│                       │                                     │
│  ┌────────────────────▼───────────────────────────────┐    │
│  │         CodeExecutionService                        │    │
│  │  - Validate submission                              │    │
│  │  - Enqueue job to RabbitMQ                         │    │
│  │  - Store job metadata in Redis                     │    │
│  └────────────────────┬───────────────────────────────┘    │
└────────────────────────┼────────────────────────────────────┘
                         │
                         │ Enqueue
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                      RabbitMQ Queue                          │
│  [Job 1] [Job 2] [Job 3] ... [Job N]                       │
└────────────────────────┬────────────────────────────────────┘
                         │
                         │ Dequeue (Worker Pool)
                         ▼
┌─────────────────────────────────────────────────────────────┐
│              CodeExecutionWorker (Spring Boot)               │
│  ┌────────────────────────────────────────────────────┐    │
│  │  @RabbitListener                                    │    │
│  │  - Receive job from queue                           │    │
│  │  - Create Docker container                          │    │
│  │  - Copy code into container                         │    │
│  │  - Execute with timeout                             │    │
│  │  - Capture output                                   │    │
│  │  - Compare against test cases                       │    │
│  │  - Store results in Redis                          │    │
│  │  - Publish result event                             │    │
│  │  - Cleanup container                                │    │
│  └────────────────────┬───────────────────────────────┘    │
└────────────────────────┼────────────────────────────────────┘
                         │
                         │ Docker API (localhost)
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                     Docker Daemon (Host)                     │
│                                                              │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐     │
│  │  Container 1 │  │  Container 2 │  │  Container N │     │
│  │  User Code   │  │  User Code   │  │  User Code   │     │
│  │              │  │              │  │              │     │
│  │  Hardened:   │  │  Hardened:   │  │  Hardened:   │     │
│  │  - No network│  │  - No network│  │  - No network│     │
│  │  - Read-only │  │  - Read-only │  │  - Read-only │     │
│  │  - Limited   │  │  - Limited   │  │  - Limited   │     │
│  │  - Timeout   │  │  - Timeout   │  │  - Timeout   │     │
│  └──────────────┘  └──────────────┘  └──────────────┘     │
└─────────────────────────────────────────────────────────────┘
```

#### Components

1. **API Layer** (Spring Boot)
   - Receives code submissions
   - Validates input (code length, language)
   - Generates job ID
   - Enqueues to RabbitMQ
   - Returns job ID to user

2. **Message Queue** (RabbitMQ)
   - Asynchronous job processing
   - Load balancing across workers
   - Durability (survives crashes)
   - Priority queues (optional)

3. **Worker Service** (Spring Boot)
   - Listens on RabbitMQ queue
   - Uses Docker Java SDK
   - Creates ephemeral containers
   - Executes code with timeout
   - Captures output and errors
   - Compares against test cases
   - Stores results in Redis
   - Cleans up containers

4. **Cache/Store** (Redis)
   - Job status tracking
   - Result storage (with TTL)
   - Rate limiting data
   - Session management

5. **Execution Containers** (Docker)
   - Ephemeral, created per job
   - Hardened configuration
   - Resource limited
   - Network isolated
   - Destroyed after completion

#### Security Configuration

Every execution container runs with:

```java
// Pseudo-code for Docker container creation
ContainerCreateCmd createCmd = dockerClient.createContainerCmd(IMAGE)
    // Security
    .withReadonlyRootfs(true)
    .withNetworkMode("none")
    .withCapDrop(Capability.ALL)
    .withSecurityOpts(List.of(
        "no-new-privileges",
        "seccomp=seccomp-profile.json",
        "apparmor=docker-code-execution"
    ))

    // Resource limits
    .withHostConfig(new HostConfig()
        .withMemory(256 * 1024 * 1024L)  // 256MB
        .withMemorySwap(256 * 1024 * 1024L)  // No swap
        .withCpuQuota(50000L)  // 0.5 CPU
        .withPidsLimit(50L)
        .withTmpfs(Map.of("/tmp", "rw,noexec,nosuid,size=100m"))
    )

    // Environment
    .withUser("1000:1000")  // Non-root
    .withWorkingDir("/home/coderunner")
    .withEnv("JAVA_OPTS=-Xmx128m");

// Execute with timeout
ExecCreateCmdResponse exec = dockerClient.execCreateCmd(containerId)
    .withCmd("javac", "Main.java")
    .exec();

// Use timeout wrapper
Future<?> future = executor.submit(() -> {
    dockerClient.execStartCmd(exec.getId()).exec(callback).awaitCompletion();
});

try {
    future.get(5, TimeUnit.SECONDS);  // 5-second timeout
} catch (TimeoutException e) {
    future.cancel(true);
    dockerClient.killContainerCmd(containerId).exec();
    // Return timeout error
}

// Always cleanup
finally {
    dockerClient.removeContainerCmd(containerId).withForce(true).exec();
}
```

#### Workflow

1. User submits code via `/api/execute/run`
2. API validates and enqueues job, returns job ID
3. User polls `/api/execute/result/{jobId}` for status
4. Worker picks up job from queue
5. Worker creates hardened Docker container
6. Worker copies code into container
7. Worker executes compilation: `javac Main.java`
8. If compilation succeeds, worker runs code: `java Main`
9. Worker captures stdout, stderr, exit code
10. Worker compares output against test cases
11. Worker calculates results (passed/failed, XP awarded)
12. Worker stores results in Redis
13. Worker destroys container
14. User's next poll returns results

#### Advantages

- ✅ Simple to implement for MVP
- ✅ No privileged containers needed
- ✅ No Docker socket mounting
- ✅ Standard Docker, widely understood
- ✅ Easy to debug and monitor
- ✅ Works on any Docker-capable host
- ✅ Horizontal scaling (add more workers)

#### Limitations

- ⚠️ Shared kernel (kernel exploits affect host)
- ⚠️ Docker daemon runs as root (but not containers)
- ⚠️ Requires regular security updates

**Recommendation**: Use this architecture for MVP (Phase 1A-1C)

### Phase 2: Production with gVisor

For production scale (Phase 1D+), upgrade to gVisor:

#### Changes Required

1. Install runsc runtime on worker hosts
2. Configure Docker to use runsc:
   ```json
   {
     "runtimes": {
       "runsc": {
         "path": "/usr/local/bin/runsc"
       }
     }
   }
   ```
3. Update worker code to specify runtime:
   ```java
   .withHostConfig(new HostConfig()
       .withRuntime("runsc")
       // ... other configs
   )
   ```

#### Benefits

- ✅ Additional syscall filtering (70 vs 300+)
- ✅ Userspace kernel reduces kernel attack surface
- ✅ Better isolation from kernel vulnerabilities
- ✅ Production-tested at Google
- ✅ Drop-in replacement (minimal code changes)

#### Trade-offs

- ⚠️ 10-30% CPU overhead
- ⚠️ Slightly more complex debugging

**Recommendation**: Migrate to gVisor for production (Phase 1D or later)

### Phase 3: Maximum Isolation with Kata Containers or Firecracker

For enterprise/multi-tenant scale:

#### Kata Containers

- Hardware-level isolation
- Separate kernel per container
- Best for multi-tenant enterprise customers
- OCI-compliant (easy migration)

#### Firecracker

- AWS Lambda-like performance
- High density (150+ containers/sec)
- Minimal overhead (<5MB per microVM)
- Requires custom orchestration

**Recommendation**: Evaluate for Phase 2+ or enterprise tier

---

## Implementation Roadmap

### Phase 1A: MVP - Basic Docker Execution (Current)

**Timeline**: Part of MVP development (Weeks 9-10)

**Tasks**:
- [ ] Create Java execution Dockerfile
  - [ ] Base: `openjdk:25-slim`
  - [ ] Non-root user (UID 1000)
  - [ ] Minimal tools
- [ ] Create custom seccomp profile
  - [ ] Block dangerous syscalls
  - [ ] Allow minimal Java requirements
- [ ] Create AppArmor profile (Ubuntu) or SELinux (RHEL)
  - [ ] Deny network access
  - [ ] Restrict file access to /tmp
- [ ] Implement CodeExecutionService
  - [ ] Docker Java SDK integration
  - [ ] Container creation with hardening flags
  - [ ] Timeout enforcement
  - [ ] Output capture
  - [ ] Cleanup logic
- [ ] Implement RabbitMQ integration
  - [ ] Job queue
  - [ ] Worker consumer
  - [ ] Result publishing
- [ ] Test malicious code scenarios
  - [ ] Infinite loops (timeout works)
  - [ ] Memory bombs (limit works)
  - [ ] Fork bombs (PID limit works)
  - [ ] Network attempts (blocked)
  - [ ] File writes (blocked)

**Success Criteria**:
- ✅ Code executes in < 5 seconds
- ✅ Malicious code cannot escape container
- ✅ Resource limits enforced
- ✅ Network isolation verified
- ✅ No container escape vulnerabilities exploitable

### Phase 1B: Enhanced Security

**Timeline**: After MVP deployment, before production traffic

**Tasks**:
- [ ] Enable user namespace remapping
  - [ ] Configure Docker daemon
  - [ ] Test with existing containers
  - [ ] Verify file permissions
- [ ] Implement comprehensive monitoring
  - [ ] Container execution metrics
  - [ ] Failed execution alerts
  - [ ] Resource usage tracking
  - [ ] Anomaly detection
- [ ] Security audit
  - [ ] Penetration testing
  - [ ] Vulnerability scanning
  - [ ] Code review
- [ ] Performance optimization
  - [ ] Container pooling/reuse
  - [ ] Pre-warmed containers
  - [ ] Parallel execution
  - [ ] Result caching

**Success Criteria**:
- ✅ User namespace remapping enabled
- ✅ No critical findings from security audit
- ✅ Monitoring and alerting functional

### Phase 1C: gVisor Migration

**Timeline**: Production scaling phase

**Tasks**:
- [ ] Install runsc on worker hosts
- [ ] Configure Docker daemon with runsc runtime
- [ ] Update worker code to use runsc
- [ ] Test compatibility with Java workloads
- [ ] Benchmark performance overhead
- [ ] Gradual rollout (canary deployment)
- [ ] Monitor for issues

**Success Criteria**:
- ✅ gVisor runtime functional
- ✅ All tests passing
- ✅ Performance acceptable (<30% overhead)
- ✅ No increase in errors

### Phase 2: Advanced Isolation (Optional)

**Timeline**: Enterprise/scale phase

**Tasks**:
- [ ] Evaluate Kata Containers vs Firecracker
- [ ] Prototype integration
- [ ] Performance benchmarking
- [ ] Cost analysis
- [ ] Implementation if beneficial

**Success Criteria**:
- ✅ Hardware-level isolation achieved
- ✅ Performance goals met
- ✅ Cost justified for tier

---

## Security Checklist

### Docker Container Configuration

- [ ] **No privileged mode**: Never use `--privileged`
- [ ] **No Docker socket**: Never mount `/var/run/docker.sock`
- [ ] **Read-only filesystem**: Use `--read-only`
- [ ] **Tmpfs for temp**: `--tmpfs /tmp:rw,noexec,nosuid,size=100m`
- [ ] **Network isolation**: `--network none`
- [ ] **Drop all capabilities**: `--cap-drop ALL`
- [ ] **No new privileges**: `--security-opt no-new-privileges`
- [ ] **Seccomp profile**: `--security-opt seccomp=profile.json`
- [ ] **AppArmor profile**: `--security-opt apparmor=profile-name` (or SELinux)
- [ ] **Non-root user**: Run as UID 1000 or similar
- [ ] **Memory limit**: `--memory=256m --memory-swap=256m`
- [ ] **CPU limit**: `--cpus=0.5`
- [ ] **PID limit**: `--pids-limit=50`
- [ ] **Timeout enforcement**: 5-second maximum execution
- [ ] **Ephemeral containers**: Delete after execution

### Host Configuration

- [ ] **Docker updated**: Latest stable version
- [ ] **Kernel updated**: Latest security patches
- [ ] **User namespace remapping**: Enabled in `/etc/docker/daemon.json`
- [ ] **Monitoring**: Container metrics and alerts
- [ ] **Log aggregation**: Centralized logging
- [ ] **Rate limiting**: Per-user execution limits
- [ ] **Firewall**: Docker host properly firewalled

### Application Security

- [ ] **Input validation**: Code length limits
- [ ] **Authentication**: Users authenticated for submissions
- [ ] **Rate limiting**: Prevent abuse (e.g., 10 submissions/minute)
- [ ] **Queue limits**: Prevent queue flooding
- [ ] **Result expiration**: Redis TTL on results
- [ ] **Error handling**: Don't leak system information
- [ ] **Logging**: Audit trail of submissions

### Testing & Validation

- [ ] **Infinite loop test**: Verify timeout works
- [ ] **Memory bomb test**: Verify memory limit works
- [ ] **Fork bomb test**: Verify PID limit works
- [ ] **Network test**: Verify network blocked
- [ ] **File write test**: Verify filesystem read-only
- [ ] **Compilation errors**: Handled gracefully
- [ ] **Runtime errors**: Captured and returned
- [ ] **Large output**: Truncated appropriately

### Ongoing Maintenance

- [ ] **Security updates**: Weekly check for CVEs
- [ ] **Image scanning**: Automated vulnerability scanning
- [ ] **Dependency updates**: Keep libraries current
- [ ] **Incident response**: Plan for security incidents
- [ ] **Backup strategy**: Code submissions backed up
- [ ] **Disaster recovery**: Tested recovery procedures

---

## References and Further Reading

### Official Documentation

1. **Docker Security**
   - Docker Security Overview: https://docs.docker.com/engine/security/
   - Seccomp Profiles: https://docs.docker.com/engine/security/seccomp/
   - AppArmor Profiles: https://docs.docker.com/engine/security/apparmor/
   - User Namespace Remapping: https://docs.docker.com/engine/security/userns-remap/
   - Rootless Mode: https://docs.docker.com/engine/security/rootless/

2. **Docker Resource Management**
   - Resource Constraints: https://docs.docker.com/engine/containers/resource_constraints/
   - Network Drivers: https://docs.docker.com/engine/network/drivers/

3. **Alternative Runtimes**
   - gVisor Documentation: https://gvisor.dev/
   - Kata Containers: https://katacontainers.io/
   - Firecracker: https://firecracker-microvm.github.io/

### Security Research

4. **OWASP**
   - Docker Security Cheat Sheet: https://cheatsheetseries.owasp.org/cheatsheets/Docker_Security_Cheat_Sheet.html

5. **CVE Databases**
   - Docker CVEs: https://stack.watch/product/docker/
   - NVD (National Vulnerability Database): https://nvd.nist.gov/

### Articles and Guides

6. **Container Security**
   - "My Bulletproof Docker Sandbox for Running Untrusted Code" (Medium)
   - "Building a secure/sandboxed environment for executing untrusted code" (DEV Community)
   - "Sandboxing Code in the Era of Containers" (AWS)

7. **Production Implementations**
   - "Design LeetCode Online Judge" (System Design School)
   - "How AWS's Firecracker virtual machines work" (Amazon Science)

### Tools

8. **Security Scanning**
   - Trivy: Container image vulnerability scanner
   - Clair: Static analysis of vulnerabilities
   - Docker Bench Security: Automated security checks

9. **Monitoring**
   - Prometheus: Metrics collection
   - Grafana: Visualization
   - Falco: Runtime security monitoring

---

## Appendix A: Example Seccomp Profile

```json
{
  "defaultAction": "SCMP_ACT_ERRNO",
  "defaultErrnoRet": 1,
  "archMap": [
    {
      "architecture": "SCMP_ARCH_X86_64",
      "subArchitectures": ["SCMP_ARCH_X86", "SCMP_ARCH_X32"]
    }
  ],
  "syscalls": [
    {
      "names": [
        "accept",
        "accept4",
        "access",
        "arch_prctl",
        "bind",
        "brk",
        "capget",
        "capset",
        "chdir",
        "chmod",
        "chown",
        "clock_getres",
        "clock_gettime",
        "clock_nanosleep",
        "close",
        "connect",
        "dup",
        "dup2",
        "dup3",
        "epoll_create",
        "epoll_create1",
        "epoll_ctl",
        "epoll_wait",
        "epoll_pwait",
        "eventfd",
        "eventfd2",
        "execve",
        "exit",
        "exit_group",
        "faccessat",
        "fadvise64",
        "fallocate",
        "fchdir",
        "fchmod",
        "fchmodat",
        "fchown",
        "fchownat",
        "fcntl",
        "fdatasync",
        "flock",
        "fork",
        "fstat",
        "fstatfs",
        "fsync",
        "ftruncate",
        "futex",
        "getcwd",
        "getdents",
        "getdents64",
        "getegid",
        "geteuid",
        "getgid",
        "getgroups",
        "getitimer",
        "getpeername",
        "getpgid",
        "getpgrp",
        "getpid",
        "getppid",
        "getpriority",
        "getrandom",
        "getresgid",
        "getresuid",
        "getrlimit",
        "get_robust_list",
        "getrusage",
        "getsid",
        "getsockname",
        "getsockopt",
        "get_thread_area",
        "gettid",
        "gettimeofday",
        "getuid",
        "getxattr",
        "inotify_add_watch",
        "inotify_init",
        "inotify_init1",
        "inotify_rm_watch",
        "ioctl",
        "ioprio_get",
        "ioprio_set",
        "kill",
        "lchown",
        "lgetxattr",
        "link",
        "linkat",
        "listen",
        "listxattr",
        "llistxattr",
        "lseek",
        "lstat",
        "madvise",
        "memfd_create",
        "mincore",
        "mkdir",
        "mkdirat",
        "mknod",
        "mknodat",
        "mlock",
        "mlock2",
        "mlockall",
        "mmap",
        "mprotect",
        "mq_getsetattr",
        "mq_notify",
        "mq_open",
        "mq_timedreceive",
        "mq_timedsend",
        "mq_unlink",
        "mremap",
        "msgctl",
        "msgget",
        "msgrcv",
        "msgsnd",
        "msync",
        "munlock",
        "munlockall",
        "munmap",
        "nanosleep",
        "newfstatat",
        "open",
        "openat",
        "pause",
        "pipe",
        "pipe2",
        "poll",
        "ppoll",
        "prctl",
        "pread64",
        "preadv",
        "prlimit64",
        "pselect6",
        "pwrite64",
        "pwritev",
        "read",
        "readahead",
        "readlink",
        "readlinkat",
        "readv",
        "recv",
        "recvfrom",
        "recvmmsg",
        "recvmsg",
        "remap_file_pages",
        "removexattr",
        "rename",
        "renameat",
        "renameat2",
        "restart_syscall",
        "rmdir",
        "rt_sigaction",
        "rt_sigpending",
        "rt_sigprocmask",
        "rt_sigqueueinfo",
        "rt_sigreturn",
        "rt_sigsuspend",
        "rt_sigtimedwait",
        "rt_tgsigqueueinfo",
        "sched_getaffinity",
        "sched_getattr",
        "sched_getparam",
        "sched_get_priority_max",
        "sched_get_priority_min",
        "sched_getscheduler",
        "sched_rr_get_interval",
        "sched_setaffinity",
        "sched_setattr",
        "sched_setparam",
        "sched_setscheduler",
        "sched_yield",
        "seccomp",
        "select",
        "semctl",
        "semget",
        "semop",
        "semtimedop",
        "send",
        "sendfile",
        "sendmmsg",
        "sendmsg",
        "sendto",
        "setfsgid",
        "setfsuid",
        "setgid",
        "setgroups",
        "setitimer",
        "setpgid",
        "setpriority",
        "setregid",
        "setresgid",
        "setresuid",
        "setreuid",
        "setrlimit",
        "set_robust_list",
        "setsid",
        "setsockopt",
        "set_thread_area",
        "set_tid_address",
        "setuid",
        "setxattr",
        "shmat",
        "shmctl",
        "shmdt",
        "shmget",
        "shutdown",
        "sigaltstack",
        "signalfd",
        "signalfd4",
        "socket",
        "socketpair",
        "splice",
        "stat",
        "statfs",
        "symlink",
        "symlinkat",
        "sync",
        "sync_file_range",
        "syncfs",
        "sysinfo",
        "tee",
        "tgkill",
        "time",
        "timer_create",
        "timer_delete",
        "timerfd_create",
        "timerfd_gettime",
        "timerfd_settime",
        "timer_getoverrun",
        "timer_gettime",
        "timer_settime",
        "times",
        "tkill",
        "truncate",
        "umask",
        "uname",
        "unlink",
        "unlinkat",
        "utime",
        "utimensat",
        "utimes",
        "vfork",
        "vmsplice",
        "wait4",
        "waitid",
        "write",
        "writev"
      ],
      "action": "SCMP_ACT_ALLOW"
    }
  ]
}
```

Save as `seccomp-java-execution.json` and apply with:
```bash
docker run --security-opt seccomp=seccomp-java-execution.json myimage
```

---

## Appendix B: Example AppArmor Profile

```
#include <tunables/global>

profile docker-java-execution flags=(attach_disconnected,mediate_deleted) {
  #include <abstractions/base>

  # Deny all network access
  deny network inet,
  deny network inet6,

  # Allow only specific file operations
  /tmp/** rw,
  /home/coderunner/** rw,

  # Allow Java execution
  /usr/bin/java rix,
  /usr/bin/javac rix,
  /usr/lib/jvm/** r,
  /usr/lib/jvm/**.so mr,

  # Allow proc/sys reading (Java needs this)
  @{PROC}/sys/vm/overcommit_memory r,
  @{PROC}/meminfo r,
  @{PROC}/stat r,
  @{PROC}/self/** r,

  # Deny everything else
  deny /etc/** w,
  deny /usr/** w,
  deny /var/** w,
  deny /sys/** w,
  deny /** wx,

  # Deny capability
  deny capability dac_override,
  deny capability dac_read_search,
  deny capability sys_admin,
  deny capability sys_module,
  deny capability sys_rawio,
}
```

Save as `/etc/apparmor.d/docker-java-execution` and apply with:
```bash
sudo apparmor_parser -r -W /etc/apparmor.d/docker-java-execution
docker run --security-opt apparmor=docker-java-execution myimage
```

---

## Appendix C: Example Docker Compose for Testing

```yaml
version: '3.8'

services:
  # Code execution worker (example)
  code-execution-worker:
    build:
      context: ./docker/java-executor
      dockerfile: Dockerfile
    volumes:
      - /var/run/docker.sock:/var/run/docker.sock:ro  # Only for orchestrator, NOT execution containers
    environment:
      - RABBITMQ_HOST=rabbitmq
      - REDIS_HOST=redis
      - DOCKER_IMAGE=java-executor:latest
    security_opt:
      - apparmor:docker-default
    deploy:
      resources:
        limits:
          cpus: '2.0'
          memory: 2G
    depends_on:
      - rabbitmq
      - redis

  rabbitmq:
    image: rabbitmq:3-management
    ports:
      - "5672:5672"
      - "15672:15672"
    environment:
      - RABBITMQ_DEFAULT_USER=gitgud
      - RABBITMQ_DEFAULT_PASS=changeme

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
    command: redis-server --maxmemory 256mb --maxmemory-policy allkeys-lru
```

**Note**: The worker needs Docker socket access to orchestrate execution containers, but the execution containers themselves DO NOT have socket access.

---

## Conclusion

This research provides a comprehensive foundation for implementing secure code execution in the GitGud platform. The recommended approach balances security, performance, and implementation complexity:

1. **MVP (Phase 1A-1C)**: Standard Docker with comprehensive hardening
2. **Production (Phase 1D+)**: Migrate to gVisor for enhanced isolation
3. **Enterprise (Phase 2+)**: Evaluate Kata Containers or Firecracker for maximum security

Key principles:
- **Never use Docker-in-Docker or mount the Docker socket** for untrusted code
- **Defense in depth**: Multiple security layers (seccomp, AppArmor, resource limits, network isolation)
- **Regular updates**: Keep Docker, kernel, and images updated with security patches
- **Monitoring and response**: Detect and respond to security incidents
- **Continuous improvement**: Re-evaluate security as threats evolve

By following these guidelines, GitGud can safely execute user-submitted Java code while protecting the platform, users, and infrastructure from malicious activity.
