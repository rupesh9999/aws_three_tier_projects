# ============================================
# StreamFlix Notification Service Dockerfile
# Multi-stage build for optimized production image
# ============================================

FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

RUN apk add --no-cache maven

COPY pom.xml ./
COPY common/pom.xml ./common/
COPY common/src ./common/src
COPY notification-service/pom.xml ./notification-service/

RUN mvn -B -f common/pom.xml dependency:go-offline
RUN mvn -B -f notification-service/pom.xml dependency:go-offline

COPY notification-service/src ./notification-service/src

RUN mvn -B -f common/pom.xml clean install -DskipTests
RUN mvn -B -f notification-service/pom.xml clean package -DskipTests

RUN java -Djarmode=layertools -jar notification-service/target/*.jar extract --destination notification-service/target/extracted

FROM eclipse-temurin:21-jre-alpine AS runtime

RUN addgroup -g 1000 streamflix && \
    adduser -u 1000 -G streamflix -s /bin/sh -D streamflix

WORKDIR /app

COPY --from=builder /app/notification-service/target/extracted/dependencies/ ./
COPY --from=builder /app/notification-service/target/extracted/spring-boot-loader/ ./
COPY --from=builder /app/notification-service/target/extracted/snapshot-dependencies/ ./
COPY --from=builder /app/notification-service/target/extracted/application/ ./

RUN chown -R streamflix:streamflix /app
USER streamflix

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8086/actuator/health || exit 1

EXPOSE 8086

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC -XX:+ExitOnOutOfMemoryError"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher"]
