# ✅ Use official JDK 17 image
FROM eclipse-temurin:17-jdk

# ✅ Set working directory inside container
WORKDIR /app

# ✅ Copy Maven wrapper and pom.xml first (for dependency caching)
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# ✅ Pre-download dependencies
RUN ./mvnw dependency:go-offline

# ✅ Copy the entire source code
COPY src src

# ✅ Build the application (skip tests to speed up)
RUN ./mvnw clean package -DskipTests

# ✅ Expose Spring Boot default port
EXPOSE 8080

# ✅ Run the built Spring Boot jar
CMD ["java", "-jar", "target/*.jar"]
