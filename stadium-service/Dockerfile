# runtime-only Dockerfile: expect a pre-built jar at stadium-service/build/libs
FROM eclipse-temurin:17-jre
COPY stadium-service/build/libs/*.jar /app/app.jar
EXPOSE 8084
ENTRYPOINT ["java","-jar","/app/app.jar"]
