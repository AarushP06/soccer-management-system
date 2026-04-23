# runtime-only Dockerfile: expect a pre-built jar at api-gateway/build/libs
FROM eclipse-temurin:17-jre
COPY api-gateway/build/libs/*.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
