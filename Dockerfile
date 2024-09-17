# Build stage
FROM maven:3.8.3-openjdk-17 AS build
WORKDIR /app
# Copy the pom.xml to fetch dependencies
COPY pom.xml .
# Download dependencies to improve subsequent build times
RUN mvn dependency:go-offline
# Copy the rest of the application
COPY . /app/
# Package the application
RUN mvn clean package -DskipTests

# Package stage
FROM openjdk:17-alpine
WORKDIR /app

# Copy the built jar file
COPY --from=build /app/target/*.jar /app/app.jar
# Expose the port the application runs on
EXPOSE 8088
# Run the application
ENTRYPOINT ["java","-jar","app.jar"]