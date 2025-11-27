# Stage 1: Build
FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /app

# Copy Gradle wrapper and config files first (better caching)
COPY gradle gradle
COPY gradlew build.gradle settings.gradle ./
COPY gradle.properties* ./
RUN chmod +x gradlew

# Download dependencies (cached unless build files change)
RUN ./gradlew dependencies --no-daemon --parallel

# Copy source code
COPY src src

# Build the application
RUN ./gradlew bootJar --no-daemon -x test --parallel

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Install curl for health checks
RUN apk add --no-cache curl

# Add non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring

# No Docker HEALTHCHECK - we check manually in CI/CD to avoid start-period delays

# Copy JAR from build stage
COPY --from=build /app/build/libs/*.jar app.jar

# Set ownership
RUN chown spring:spring app.jar

USER spring:spring

EXPOSE 8080

# JVM optimizations for containers
ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", "app.jar"]
