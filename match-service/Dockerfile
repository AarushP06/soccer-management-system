# runtime-only Dockerfile: expect a pre-built jar at match-service/build/libs
FROM eclipse-temurin:17-jre
COPY match-service/build/libs/*.jar /app/app.jar
EXPOSE 8081
ENTRYPOINT ["java","-jar","/app/app.jar"]
