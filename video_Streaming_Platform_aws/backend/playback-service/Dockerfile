# ============================================
# StreamFlix Playback Service Dockerfile
# Multi-stage build for optimized production image
# ============================================

FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

RUN apk add --no-cache maven

COPY pom.xml ./
COPY common/pom.xml ./common/
COPY common/src ./common/src
COPY playback-service/pom.xml ./playback-service/

RUN mvn -B -f common/pom.xml dependency:go-offline
RUN mvn -B -f playback-service/pom.xml dependency:go-offline

COPY playback-service/src ./playback-service/src

RUN mvn -B -f common/pom.xml clean install -DskipTests
RUN mvn -B -f playback-service/pom.xml clean package -DskipTests

RUN java -Djarmode=layertools -jar playback-service/target/*.jar extract --destination playback-service/target/extracted

FROM eclipse-temurin:21-jre-alpine AS runtime

RUN addgroup -g 1000 streamflix && \
    adduser -u 1000 -G streamflix -s /bin/sh -D streamflix

WORKDIR /app

COPY --from=builder /app/playback-service/target/extracted/dependencies/ ./
COPY --from=builder /app/playback-service/target/extracted/spring-boot-loader/ ./
COPY --from=builder /app/playback-service/target/extracted/snapshot-dependencies/ ./
COPY --from=builder /app/playback-service/target/extracted/application/ ./

RUN chown -R streamflix:streamflix /app
USER streamflix

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8084/actuator/health || exit 1

EXPOSE 8084

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC -XX:+ExitOnOutOfMemoryError"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher"]
