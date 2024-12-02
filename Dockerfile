FROM openjdk:17-jkd-slim

COPY build/libs/*.jar /app/localens-0.0.1-SNAPSHOT.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","/app/app.jar"]