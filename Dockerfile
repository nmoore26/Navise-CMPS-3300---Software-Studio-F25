# Build stage
FROM maven:3.9-eclipse-temurin-21 as builder

WORKDIR /app


COPY navisewebsite/ ./

RUN mvn clean package

# Runtime stage
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=builder /app/target/navisewebsite-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT:-8080} -jar app.jar"]