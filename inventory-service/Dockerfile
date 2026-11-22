# Build stage - JDK 17 base (compatible with the project's Lombok version)
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY . .
RUN mvn -q -B -pl inventory-service -am -DskipTests clean package

# Runtime stage
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /workspace/inventory-service/target/*.jar app.jar
EXPOSE 9094
ENTRYPOINT ["java", "-jar", "app.jar"]
