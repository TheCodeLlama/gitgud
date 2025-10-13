FROM openjdk:25-slim

# Create non-root user for code execution
# UID 1000 is standard for first non-root user
RUN useradd -m -u 1000 -s /bin/bash coderunner && \
    mkdir -p /home/coderunner/code && \
    chown -R coderunner:coderunner /home/coderunner

# Install minimal required tools
# Note: openjdk:25-slim already includes javac and java
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
    # No additional tools needed for basic Java execution
    && rm -rf /var/lib/apt/lists/* \
    && apt-get clean

# Switch to non-root user
USER coderunner

# Set working directory
WORKDIR /home/coderunner

# Set Java memory limits to prevent memory exhaustion
# -Xmx128m: Maximum heap size 128MB
# -Xms64m: Initial heap size 64MB
# These are applied when Java is invoked
ENV JAVA_OPTS="-Xmx128m -Xms64m"

# Default command (will be overridden by DockerExecutorService)
CMD ["sleep", "3600"]
