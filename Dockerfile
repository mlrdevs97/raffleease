FROM maven:3.9.4-eclipse-temurin-17 AS builder
WORKDIR /app
COPY . .
RUN mvn clean package

FROM openjdk:17-jdk-alpine
WORKDIR /app
COPY --from=builder /app/target/raffleease-0.0.1.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
