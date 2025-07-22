#!/bin/bash

# Start Spring Boot backend in the background
echo "Starting Spring Boot backend..."
java -jar target/employee-0.0.1-SNAPSHOT.jar &

# Wait a moment for Spring Boot to start
sleep 10

# Start Nginx in the foreground
echo "Starting Nginx frontend server..."
nginx -g "daemon off;"
