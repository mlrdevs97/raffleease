FROM openjdk:17-jdk-alpine
WORKDIR /app
COPY target/raffleease-0.0.1.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
