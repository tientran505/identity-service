# Stage 1 : Build
FROM maven:3.9.8-amazoncorretto-21 as build

WORKDIR /app
COPY pom.xml .
COPY src ./src

# Build source code with maven
RUN mvn package -DskipTests

# Stage 2: create image
FROM amazoncorretto:21.0.6

WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]