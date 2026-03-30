# Multi-stage build for optimized image size
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app
# Cache dependencies layer
COPY pom.xml .
RUN mvn dependency:go-offline -q
COPY src ./src
RUN mvn package -DskipTests -q

FROM eclipse-temurin:17-jre-alpine AS runtime
RUN addgroup -S medic && adduser -S medic -G medic
WORKDIR /app

# Security: run as non-root user
USER medic

# Copy the built jar
COPY --from=builder --chown=medic:medic /app/target/*.jar app.jar

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:${SERVER_PORT:-8080}/actuator/health || exit 1

EXPOSE 8080

ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", "app.jar"]
