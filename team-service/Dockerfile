# runtime-only Dockerfile: expect a pre-built jar at team-service/build/libs
FROM eclipse-temurin:17-jre
COPY team-service/build/libs/*.jar /app/app.jar
EXPOSE 8083
ENTRYPOINT ["java","-jar","/app/app.jar"]
