# Auth Service Dockerfile
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Copy maven wrapper and pom files
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
COPY common/pom.xml common/
COPY auth-service/pom.xml auth-service/

# Download dependencies
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY common/src common/src
COPY auth-service/src auth-service/src

# Build the application
RUN ./mvnw clean package -DskipTests -pl auth-service -am

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

# Add non-root user for security
RUN addgroup -g 1000 streamflix && \
    adduser -u 1000 -G streamflix -s /bin/sh -D streamflix

WORKDIR /app

# Copy the built JAR
COPY --from=builder /app/auth-service/target/*.jar app.jar

# Change ownership
RUN chown -R streamflix:streamflix /app

USER streamflix

# Expose port
EXPOSE 8081

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD wget -qO- http://localhost:8081/api/v1/auth/actuator/health || exit 1

# JVM options for containers
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:InitialRAMPercentage=50.0 -Djava.security.egd=file:/dev/./urandom"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
