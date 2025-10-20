FROM openjdk:25-slim

# Create non-root user for code execution
# UID 1000 is standard for first non-root user
RUN useradd -m -u 1000 -s /bin/bash coderunner && \
    mkdir -p /home/coderunner/code && \
    chown -R coderunner:coderunner /home/coderunner

# Install Maven for Spring Boot lesson execution
# Maven is needed to compile and run Spring Boot tests
ARG MAVEN_VERSION=3.9.6
ARG MAVEN_SHA=706f01b20dec0305a822ab614d51f32b07ee11d0218175e55450242e49d2156386483b506b3a4e8a03ac8611bae96395fd5eec15f50d3013d5deed6d1ee18224
ARG MAVEN_BASE_URL=https://archive.apache.org/dist/maven/maven-3/${MAVEN_VERSION}/binaries

RUN apt-get update && \
    apt-get install -y --no-install-recommends \
    curl \
    ca-certificates \
    && rm -rf /var/lib/apt/lists/* \
    && apt-get clean

# Download and install Maven
RUN mkdir -p /usr/share/maven /usr/share/maven/ref && \
    curl -fsSL -o /tmp/apache-maven.tar.gz ${MAVEN_BASE_URL}/apache-maven-${MAVEN_VERSION}-bin.tar.gz && \
    echo "${MAVEN_SHA}  /tmp/apache-maven.tar.gz" | sha512sum -c - && \
    tar -xzf /tmp/apache-maven.tar.gz -C /usr/share/maven --strip-components=1 && \
    rm -f /tmp/apache-maven.tar.gz && \
    ln -s /usr/share/maven/bin/mvn /usr/bin/mvn

# Set Maven environment variables
ENV MAVEN_HOME=/usr/share/maven
ENV MAVEN_CONFIG=/home/coderunner/.m2

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
