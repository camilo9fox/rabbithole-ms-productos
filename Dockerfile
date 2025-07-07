# ---------------------------------------------------------------------------
# Build stage: compile the Spring Boot application with Maven
# ---------------------------------------------------------------------------
FROM maven:3.9-amazoncorretto-17 AS build
WORKDIR /app

# Copy the Maven POM first and resolve dependencies — maximises layer caching
COPY pom.xml .
RUN mvn -q -B dependency:resolve dependency:resolve-plugins

# Now copy the source and build the fat-jar (skip tests for faster CI build)
COPY src ./src
RUN mvn -q -B package -DskipTests

# ---------------------------------------------------------------------------
# Runtime stage: lightweight JRE image that runs the compiled jar
# ---------------------------------------------------------------------------
FROM amazoncorretto:17-alpine
LABEL maintainer="Rabbit Hole DevOps"
WORKDIR /app

# Install curl (needed for container healthcheck)
RUN apk add --no-cache curl

# Copy the shaded jar produced in the build stage
ARG JAR_FILE=/app/target/productos-0.0.1-SNAPSHOT.jar
COPY --from=build ${JAR_FILE} app.jar

# Copy Oracle Cloud wallet so that TNS_ADMIN path resolves
COPY Wallet_C3CQ5Y7AELCSQGMU /app/Wallet_C3CQ5Y7AELCSQGMU

# JVM options can be tuned via the JVM_OPTS env var
ENV JVM_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75"

# The service listens on 8081 (overridable via SERVER_PORT)
EXPOSE 8081

# Simple healthcheck (expects spring-boot-actuator on /actuator/health). Adjust if necessary
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s CMD curl -f http://localhost:${SERVER_PORT:-8081}/actuator/health || exit 1

# Run the application. Allows SERVER_PORT env var to override default 8081.
ENTRYPOINT ["sh", "-c", "java $JVM_OPTS -jar /app/app.jar --server.port=${SERVER_PORT:-8081}"]
