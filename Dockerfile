# Use OpenJDK 17 as the base image for building
FROM eclipse-temurin:17-jdk-alpine AS builder

# Install Maven
RUN apk add --no-cache maven

# Set working directory
WORKDIR /app

# Copy the pom.xml
COPY pom.xml .

# Copy the source code
COPY src src

# Build the application
RUN mvn clean package -DskipTests

# Use a smaller base image for the runtime
FROM eclipse-temurin:17-jre-alpine

# Set working directory
WORKDIR /app

# Copy the built JAR file from the build stage
COPY --from=builder /app/target/*.jar app.jar

# Expose the port the app runs on
EXPOSE 5500

# Set environment variables
ENV SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/leave_management
ENV SPRING_DATASOURCE_USERNAME=postgres
ENV SPRING_DATASOURCE_PASSWORD=postgres
ENV SPRING_JPA_HIBERNATE_DDL_AUTO=update

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"] 