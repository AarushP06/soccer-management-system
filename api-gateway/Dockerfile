FROM eclipse-temurin:17-jdk AS builder

WORKDIR /workspace

COPY gradlew gradlew
COPY gradle gradle
COPY settings.gradle build.gradle ./
COPY api-gateway api-gateway
COPY match-service match-service
COPY league-service league-service
COPY team-service team-service
COPY stadium-service stadium-service

RUN chmod +x gradlew && ./gradlew --no-daemon :api-gateway:bootJar

FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=builder /workspace/api-gateway/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
