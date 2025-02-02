FROM maven:3.9.6-eclipse-temurin-17 AS builder

WORKDIR /app

COPY ./pki /app

RUN mvn clean package -DskipTests

FROM openjdk:17-jdk-slim

RUN apt-get update && \
    apt-get install -y openssl expect && \
    apt-get clean

WORKDIR /app

COPY simplepki /app/simplepki

RUN mkdir -p /app/simplepki/certs

COPY pki/run_openssl.sh /app/run_openssl.sh

RUN chmod +x /app/run_openssl.sh

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
