# Use Maven to build the application
FROM maven:3.8.5-openjdk-17 AS build

WORKDIR /app

# Copy pom.xml and download dependencies first for better caching
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy the rest of the source code
COPY src ./src

# Build the application (skip tests for faster build)
RUN mvn package -DskipTests

# Use a lightweight JDK for running the app (jre-slim is deprecated)
FROM openjdk:17-jdk-slim

WORKDIR /app

# Copy the built jar from the build stage
# Use a more robust way to copy the jar (handles only one jar)
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

# Use CMD instead of ENTRYPOINT for easier override in Render
CMD ["java", "-jar", "app.jar"]
