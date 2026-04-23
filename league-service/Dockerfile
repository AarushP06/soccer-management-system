# runtime-only Dockerfile: expect a pre-built jar at league-service/build/libs
FROM eclipse-temurin:17-jre
COPY league-service/build/libs/*.jar /app/app.jar
EXPOSE 8082
ENTRYPOINT ["java","-jar","/app/app.jar"]
