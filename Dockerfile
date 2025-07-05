# Multi-stage Dockerfile for Helidon microservices
# This Dockerfile can build any of the three services based on build args

# Stage 1: Build stage
FROM maven:3.9.10-amazoncorretto-24 AS builder

# Set working directory
WORKDIR /helidon

# Copy root pom.xml first for dependency resolution
COPY pom.xml .

# Copy common module
COPY common/ common/

# Copy all service modules (we'll build selectively)
COPY processor/ processor/
COPY visualizer/ visualizer/
COPY scraper/ scraper/


# Build argument to specify which service to build
ARG SERVICE_NAME
ARG SERVICE_VERSION=1.0.0
ARG PORT

# Validate SERVICE_NAME
RUN if [ -z "$SERVICE_NAME" ]; then echo "SERVICE_NAME build arg is required" && exit 1; fi
RUN if [ -z "$PORT" ]; then echo "SERVICE_NAME build arg is required" && exit 1; fi

# Build the entire project (this ensures common is built and available)
RUN mvn clean compile -DskipTests

# Build the specific service
RUN mvn clean package -pl ${SERVICE_NAME} -am -DskipTests

RUN ls ${SERVICE_NAME}/target

# Stage 2: Runtime stage
FROM maven:3.9.10-amazoncorretto-24

# Create non-root user for security
RUN groupadd -r helidon && useradd -r -g helidon helidon

# Set working directory
WORKDIR /helidon

# Build argument (must be redeclared in each stage)
ARG SERVICE_NAME

# Copy the built JAR from builder stage
ARG SERVICE_VERSION=1.0.0
COPY --from=builder /helidon/${SERVICE_NAME}/target/${SERVICE_NAME}.jar app.jar
COPY --from=builder /helidon/${SERVICE_NAME}/target/libs ./libs

# Change ownership to helidon user
RUN chown helidon:helidon app.jar

# Switch to non-root user
USER helidon

# Expose common Helidon port (can be overridden)
EXPOSE $PORT

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:${PORT}/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
