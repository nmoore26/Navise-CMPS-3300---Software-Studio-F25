#!/bin/bash
java -Dserver.port=${PORT:-8080} -jar target/navisewebsite-0.0.1-SNAPSHOT.jar
