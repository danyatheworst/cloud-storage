FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /rest-api
COPY pom.xml .
COPY src/main ./src/main
RUN mvn -f pom.xml clean package

FROM eclipse-temurin:17-jre-jammy
WORKDIR /rest-api
COPY --from=build /rest-api/target/cloud-file-storage-0.0.1.jar /rest-api/cloud-file-storage.jar
EXPOSE 8080
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "/rest-api/cloud-file-storage.jar"]