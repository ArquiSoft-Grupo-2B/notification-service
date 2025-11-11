#Generate Jar file
FROM gradle:8.10-jdk21 AS build
WORKDIR /app
COPY . .
RUN gradle clean build -x test

#Run the application
FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]