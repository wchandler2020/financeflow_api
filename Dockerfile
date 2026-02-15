# Multi-stage build for smaller image

# Stage 1: Build with Maven
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /build

# Copy pom.xml and download dependencies (cached layer)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Stage 2: Runtime with JRE only
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Copy jar from build stage
COPY --from=build /build/target/financeflow_backend-0.0.1-SNAPSHOT.jar app.jar

# Expose port (Railway will use $PORT env variable)
EXPOSE 8080

# Run the application
# Use $PORT environment variable that Railway provides
ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT:-8080} -jar app.jar"]