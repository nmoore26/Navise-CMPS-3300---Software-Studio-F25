# Runtime stage
FROM eclipse-temurin:21-jre

# INSTALL SQLITE3 ✅
RUN apt-get update && apt-get install -y sqlite3 && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# ... rest of your Dockerfile ...
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

# Copy database files to /tmp (initial data with courses)
COPY navisewebsite/courses.db /tmp/courses.db

# Create initialization script - let Java create schema, just seed initial courses data
RUN echo '#!/bin/sh' > /app/init.sh && \
    echo 'mkdir -p /data' >> /app/init.sh && \
    echo '' >> /app/init.sh && \
    echo '# Copy courses.db from /tmp if not already present (contains course data)' >> /app/init.sh && \
    echo 'if [ ! -f /data/courses.db ]; then' >> /app/init.sh && \
    echo '  echo "First run - copying courses.db with course data..."' >> /app/init.sh && \
    echo '  cp /tmp/courses.db /data/courses.db' >> /app/init.sh && \
    echo 'else' >> /app/init.sh && \
    echo '  echo "Using existing courses.db from persistent disk"' >> /app/init.sh && \
    echo 'fi' >> /app/init.sh && \
    echo '' >> /app/init.sh && \
    echo '# Java application will create users.db and student_info.db on startup' >> /app/init.sh && \
    echo 'echo "Starting application - schema initialization handled by Spring Boot..."' >> /app/init.sh && \
    echo 'exec java -Dserver.port=${PORT:-8080} -jar app.jar' >> /app/init.sh && \
    chmod +x /app/init.sh

EXPOSE 8080

ENTRYPOINT ["/app/init.sh"]