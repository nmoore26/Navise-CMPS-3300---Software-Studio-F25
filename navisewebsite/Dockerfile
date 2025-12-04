# Build stage# Build stage

FROM maven:3.9-eclipse-temurin-21 as builderFROM maven:3.9-eclipse-temurin-21 as builder



WORKDIR /appWORKDIR /app



COPY pom.xml .COPY . .

COPY src src

RUN mvn clean package -DskipTests

RUN mvn clean package -DskipTests

# Runtime stage

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
