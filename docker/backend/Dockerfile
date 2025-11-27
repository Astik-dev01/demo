# Multi-stage Dockerfile for Spring Boot backend (одномодульная архитектура)
FROM eclipse-temurin:21-jdk-alpine AS builder

# Устанавливаем рабочую директорию
WORKDIR /app

# Обновляем пакеты и устанавливаем timezone
RUN apk update && apk upgrade && \
    apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/Asia/Bishkek /etc/localtime && \
    echo "Asia/Bishkek" > /etc/timezone

# Копируем gradle wrapper и build scripts из backend директории
COPY backend/gradlew .
COPY backend/gradle gradle/
COPY backend/build.gradle .
COPY backend/settings.gradle .
# COPY backend/gradle.properties .

# Даем права на выполнение gradlew
RUN chmod +x ./gradlew

# Увеличиваем timeout для Gradle wrapper и загружаем зависимости
# Добавляем retry логику на случай проблем с сетью
RUN echo "Downloading Gradle wrapper and dependencies..." && \
    export GRADLE_OPTS="-Dorg.gradle.internal.launcher.welcomeMessageEnabled=false -Dorg.gradle.internal.http.socketTimeout=120000 -Dorg.gradle.internal.http.connectionTimeout=120000" && \
    (./gradlew build -x test -x bootJar --no-daemon --stacktrace || \
     (echo "Retrying gradle build..." && sleep 5 && ./gradlew build -x test -x bootJar --no-daemon --stacktrace))

# Копируем весь исходный код
COPY backend/src/ src/

# Собираем приложение (одномодульный проект)
RUN echo "Building application JAR..." && \
    ./gradlew bootJar --no-daemon --stacktrace && \
    echo "Build completed successfully!"

# Второй этап - runtime образ
FROM eclipse-temurin:21-jre-alpine AS runtime

# Обновляем пакеты и устанавливаем необходимые утилиты
RUN apk update && apk upgrade && \
    apk add --no-cache curl tzdata && \
    cp /usr/share/zoneinfo/Asia/Bishkek /etc/localtime && \
    echo "Asia/Bishkek" > /etc/timezone && \
    addgroup -S appuser && adduser -S appuser -G appuser

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем JAR файл из builder stage (одномодульный проект)
COPY --from=builder /app/build/libs/*.jar app.jar

# Создаем директории для логов и временных файлов с правильными правами
RUN mkdir -p /app/logs/taskflow_system /app/temp /app/uploads && \
    chmod -R 755 /app/logs && \
    chown -R appuser:appuser /app

# Переключаемся на пользователя приложения
USER appuser

# Аргументы для портов
ARG EXTERNAL_PORT=8097
ARG INTERNAL_PORT=8080

# Открываем внутренний порт (где Spring Boot запускается)
EXPOSE ${INTERNAL_PORT}

# Переменные среды для JVM (оптимизированы для контейнерной среды)
ENV JAVA_OPTS="-Xmx1g -Xms512m \
    -XX:+UseContainerSupport \
    -XX:+UseG1GC \
    -XX:MaxGCPauseMillis=200 \
    -XX:+UnlockExperimentalVMOptions \
    -XX:+UseJVMCICompiler \
    -Djava.security.egd=file:/dev/./urandom \
    -Dspring.jmx.enabled=false \
    -Djava.awt.headless=true \
    -Dfile.encoding=UTF-8 \
    -Duser.timezone=Asia/Bishkek"

# Переменные для приложения
ENV SPRING_PROFILES_ACTIVE=dev
ENV EXTERNAL_PORT=${EXTERNAL_PORT}
ENV INTERNAL_PORT=${INTERNAL_PORT}
ENV LOGGING_LEVEL_ROOT=INFO

# Health check для контейнера с улучшенной проверкой
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=5 \
    CMD curl -f http://localhost:${INTERNAL_PORT}/api/actuator/health || exit 1

# Команда запуска
ENTRYPOINT exec java $JAVA_OPTS -jar app.jar
