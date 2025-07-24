# ✅ Use Maven with Java 21 for build
FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn
COPY src ./src

# ✅ Build the application (skip tests for faster build)
RUN ./mvnw clean package -DskipTests

# ✅ Use lightweight JRE 21 to run the app
FROM eclipse-temurin:21-jre

WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
