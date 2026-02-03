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

# Environment variables for production profile
ENV SPRING_PROFILES_ACTIVE=prod
ENV MYSQL_HOST=localhost
ENV MYSQL_PORT=3306
ENV MYSQL_DATABASE=part_inventory_db
ENV MYSQL_USER=root
ENV MYSQL_PASSWORD=password

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]