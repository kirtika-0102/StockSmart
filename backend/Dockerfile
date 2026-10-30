# ---------------------------------------------------
# Stage 1: Build the Spring Boot application
# ---------------------------------------------------
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Copy Maven wrapper and POM first for layer caching
COPY .mvn .mvn
COPY mvnw pom.xml ./

# Normalize line endings (CRLF -> LF) for Windows compatibility and set executable permission
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw

# Download dependencies (cached layer)
RUN ./mvnw dependency:go-offline -B || true

# Copy application source code
COPY src ./src

# Build production executable JAR (skipping test execution during container build)
RUN ./mvnw clean package -DskipTests -B

# ---------------------------------------------------
# Stage 2: Minimal Production Runtime
# ---------------------------------------------------
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Create non-root system user and group for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy compiled JAR from builder stage
COPY --from=builder --chown=appuser:appgroup /app/target/*.jar app.jar

# Switch to non-root user
USER appuser

# Default production environment variables (overridden by Render at runtime)
ENV SPRING_PROFILES_ACTIVE=prod
ENV PORT=8080

EXPOSE 8080

# Run Spring Boot executable JAR; uses exec to propagate termination signals properly
ENTRYPOINT ["sh", "-c", "exec java -Djava.security.egd=file:/dev/./urandom -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE} -jar app.jar"]
