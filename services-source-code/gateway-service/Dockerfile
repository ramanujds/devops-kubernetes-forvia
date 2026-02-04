# Build stage
FROM maven:3.9.11-eclipse-temurin-25 AS build
WORKDIR /app
# Copy pom.xml first to cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B
# Then copy source and build
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage
FROM amazoncorretto:25-jdk
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar


EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]