FROM eclipse-temurin:25-jdk-alpine

# Install Maven
RUN apk add --no-cache maven

# Create non-root user with UID 1000
# This user will run the code execution (security best practice)
RUN adduser -D -u 1000 -s /bin/sh coderunner

# Create workspace directory
RUN mkdir -p /workspace && chown coderunner:coderunner /workspace

# Set working directory
WORKDIR /workspace

# Switch to non-root user
USER coderunner

# Default command (will be overridden by DockerExecutorService)
CMD ["sleep", "3600"]
