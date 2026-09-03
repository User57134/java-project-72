# Этап 1: Сборка приложения
FROM gradle:8.7-jdk21 AS builder
WORKDIR /app

# Копируем исходный код проекта
COPY /app .

# Очищаем и собираем проект без использования Gradle Daemon
RUN gradle clean build --no-daemon -x test

# Этап 2: Запуск готового приложения (минимальный образ)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Копируем собранный shadow-jar (fat-jar) из этапа сборки
COPY --from=builder /app/build/libs/*-all.jar app.jar

# Открываем порт для Render
EXPOSE 8080

# Запускаем чистое Java-приложение
CMD ["java", "-jar", "app.jar"]
