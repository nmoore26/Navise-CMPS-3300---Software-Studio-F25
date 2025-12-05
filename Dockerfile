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

# Copy database files to /tmp (initial data)
COPY navisewebsite/users.db /tmp/users.db
COPY navisewebsite/courses.db /tmp/courses.db
COPY navisewebsite/student_info.db /tmp/student_info.db

# Create initialization script
RUN echo '#!/bin/sh' > /app/init.sh && \
    echo 'mkdir -p /data' >> /app/init.sh && \
    echo '' >> /app/init.sh && \
    echo '# Initialize all databases only on first run' >> /app/init.sh && \
    echo 'if [ ! -f /data/users.db ]; then' >> /app/init.sh && \
    echo '  echo "First run - initializing all databases..."' >> /app/init.sh && \
    echo '  cp /tmp/users.db /data/users.db' >> /app/init.sh && \
    echo '  cp /tmp/courses.db /data/courses.db' >> /app/init.sh && \
    echo '  cp /tmp/student_info.db /data/student_info.db' >> /app/init.sh && \
    echo '  echo "All databases initialized!"' >> /app/init.sh && \
    echo 'else' >> /app/init.sh && \
    echo '  echo "Using existing databases from persistent disk"' >> /app/init.sh && \
    echo 'fi' >> /app/init.sh && \
    echo '' >> /app/init.sh && \
    echo 'echo "Starting application..."' >> /app/init.sh && \
    echo 'exec java -Dserver.port=${PORT:-8080} -jar app.jar' >> /app/init.sh && \
    chmod +x /app/init.sh

EXPOSE 8080

ENTRYPOINT ["/app/init.sh"]
