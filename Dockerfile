# Build stage
FROM maven:3.9-eclipse-temurin-21 as builder

WORKDIR /app

COPY navisewebsite/pom.xml .
COPY navisewebsite/src src

RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=builder /app/target/navisewebsite-0.0.1-SNAPSHOT.jar app.jar
COPY start.sh start.sh

RUN chmod +x start.sh

EXPOSE 8080

CMD ["bash", "start.sh"]
