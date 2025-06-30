# Using OpenJDK 21 as a base image
FROM openjdk:21-jdk-slim
LABEL authors="vijayps"

# Set the working directory
WORKDIR /app

COPY build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]