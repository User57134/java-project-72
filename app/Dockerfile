FROM gradle:8.7-jdk21

WORKDIR /app

COPY /app .

RUN ["./gradlew", "clean", "build"]

CMD ["./gradlew", "run"]