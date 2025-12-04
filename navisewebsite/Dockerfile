# Build stage
FROM maven:3.9-eclipse-temurin-21 as builder

WORKDIR /app

COPY pom.xml .
COPY src src

RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=builder /app/target/navisewebsite-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-Dserver.port=8080", "-jar", "app.jar"]

# Runtime stageFROM openjdk:21-slim

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

WORKDIR /app

COPY --from=builder /app/target/navisewebsite-0.0.1-SNAPSHOT.jar app.jar

COPY --from=builder /app/target/navisewebsite-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

EXPOSE 8080

CMD ["java", "-Dserver.port=8080", "-jar", "app.jar"]

ENTRYPOINT ["java", "-Dserver.port=8080", "-jar", "app.jar"]
