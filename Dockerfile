FROM openjdk:11 as base

WORKDIR /app

COPY ./gradle ./gradle
COPY build.gradle gradlew settings.gradle ./
COPY src ./src

FROM base as test
CMD ["./gradlew", "test"]