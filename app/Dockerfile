# Этап 1: Сборка приложения
FROM gradle:8.7-jdk21 AS builder
WORKDIR /app

# Копируем исходный код проекта
COPY /app .

# Очищаем и собираем проект без использования Gradle Daemon (защита от багов WSL/Render)
RUN gradle clean build --no-daemon -x test

# Этап 2: Запуск готового приложения (минимальный образ)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Копируем собранный fat-jar из этапа сборки в финальный образ
COPY --from=builder /app/build/libs/*-all.jar app.jar || COPY --from=builder /app/build/libs/*.jar app.jar

# Открываем порт для Render
EXPOSE 8080

# Запускаем чистое Java-приложение (без лишних затрат ОЗУ на работу Gradle)
CMD ["java", "-jar", "app.jar"]
