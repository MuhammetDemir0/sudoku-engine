# syntax=docker/dockerfile:1.7

FROM maven:3.9.9-eclipse-temurin-21-alpine AS build
WORKDIR /workspace

COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 mvn -B -DskipTests dependency:go-offline

COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -B -DskipTests package

FROM eclipse-temurin:21-jre-alpine AS runtime

RUN apk add --no-cache curl \
    && addgroup -S sudoku \
    && adduser -S sudoku -G sudoku

WORKDIR /app
COPY --from=build /workspace/target/sudoku-engine-1.0.0.jar /app/sudoku-engine.jar

ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

EXPOSE 8080

USER sudoku:sudoku

HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
    CMD curl -fsS http://localhost:8080/actuator/health | grep -q '"status":"UP"'

ENTRYPOINT ["java", "-jar", "/app/sudoku-engine.jar"]
