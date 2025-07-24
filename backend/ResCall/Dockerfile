# ✅ Stage 1: Build with Maven + Java 21
FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /app

# ✅ Install SSL certificates (IMPORTANT for MongoDB Atlas)
RUN apt-get update && \
    apt-get install -y ca-certificates && \
    update-ca-certificates

# ✅ Copy Maven files first (better caching)
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

# ✅ Pre-fetch dependencies to speed up build
RUN ./mvnw dependency:go-offline -B

# ✅ Copy source code
COPY src ./src

# ✅ Build Spring Boot jar (skip tests for faster build)
RUN ./mvnw clean package -DskipTests

# ✅ Stage 2: Use lightweight JRE 21 for runtime
FROM eclipse-temurin:21-jre

WORKDIR /app

# ✅ Copy trusted SSL certificates into runtime container
RUN apt-get update && \
    apt-get install -y ca-certificates && \
    update-ca-certificates

# ✅ Copy built JAR from build stage
COPY --from=build /app/target/*.jar app.jar

# ✅ Expose default Spring Boot port
EXPOSE 8080

# ✅ Start the application
ENTRYPOINT ["java", "-jar", "app.jar"]
