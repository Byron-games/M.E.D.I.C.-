FROM maven:3.9.9-eclipse-temurin-21 AS builder
WORKDIR /app

# Install parent POM first so child modules can resolve it
COPY pom.xml ./pom.xml
RUN mvn install -N -q

# Cache service dependencies
COPY api-gateway/pom.xml ./api-gateway/pom.xml
RUN mvn dependency:go-offline -q -f api-gateway/pom.xml

# Build the service
COPY api-gateway/src ./api-gateway/src
RUN mvn package -DskipTests -q -f api-gateway/pom.xml

FROM eclipse-temurin:21-jre-alpine AS runtime
RUN addgroup -S medic && adduser -S medic -G medic
WORKDIR /app
USER medic
COPY --from=builder --chown=medic:medic /app/api-gateway/target/*.jar app.jar

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:${SERVER_PORT:-8080}/actuator/health || exit 1

EXPOSE 8080
ENTRYPOINT ["java","-XX:+UseContainerSupport","-XX:MaxRAMPercentage=75.0","-Djava.security.egd=file:/dev/./urandom","-jar","app.jar"]
