# ---- Build Stage ----
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /build

COPY pom.xml .
COPY payment-common/pom.xml payment-common/
COPY payment-initiation/pom.xml payment-initiation/
COPY payment-execution/pom.xml payment-execution/
COPY payment-tracking/pom.xml payment-tracking/
COPY payment-reconciliation/pom.xml payment-reconciliation/
COPY payment-billing/pom.xml payment-billing/
COPY payment-risk/pom.xml payment-risk/

RUN apk add --no-cache maven && mvn dependency:go-offline -B 2>/dev/null || true

COPY . .
ARG SERVICE_NAME
RUN mvn clean package -pl payment-common,${SERVICE_NAME} -am -DskipTests -B

# ---- Runtime Stage ----
FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S payment && adduser -S payment -G payment
WORKDIR /app

ARG SERVICE_NAME
COPY --from=builder /build/${SERVICE_NAME}/target/*.jar app.jar

RUN chown -R payment:payment /app
USER payment

ENV SERVER_PORT=8080
EXPOSE ${SERVER_PORT}
HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
    CMD wget --spider --quiet http://localhost:${SERVER_PORT}/actuator/health || exit 1

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", \
    "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]

