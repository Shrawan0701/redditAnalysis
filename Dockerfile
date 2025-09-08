
FROM openjdk:17-jdk-slim as builder

WORKDIR /app


COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline


COPY src ./src

RUN ./mvnw clean package -DskipTests

# Stage 2: Run the app
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# Copy the JAR from the builder stage
COPY --from=builder /app/target/*.jar app.jar


EXPOSE 8080

# Start the application
ENTRYPOINT ["java", "-jar", "app.jar"]
