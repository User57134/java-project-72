FROM gradle:8.7-jdk21

WORKDIR /app

# Копируем проект (убедитесь, что путь относительно корня репозитория указан верно)
COPY /app .

# Используем системный gradle вместо ./gradlew и отключаем Daemon
RUN gradle clean build --no-daemon -x test

# Используем системный gradle для запуска
CMD ["gradle", "run", "--no-daemon"]