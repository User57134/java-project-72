# Этап сборки
FROM gradle:8.7-jdk21 AS builder
WORKDIR /app
COPY /app .
# Теперь папка .gradle чистая, и флаг сработает идеально
RUN gradle clean build --no-daemon -x test

# Этап запуска (минимальный размер и расход памяти)
FROM openjdk:21-slim
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
CMD ["java", "-jar", "app.jar"]
