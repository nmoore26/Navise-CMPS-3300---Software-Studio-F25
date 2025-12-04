FROM openjdk:21-slim

WORKDIR /app

COPY target/navisewebsite-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

CMD ["java", "-Dserver.port=8080", "-jar", "app.jar"]
