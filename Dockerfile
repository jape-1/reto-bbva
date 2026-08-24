# Etapa de build: aqui si hace falta Maven y el JDK completo
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build

COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src
RUN mvn -B clean package -DskipTests
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

RUN addgroup -S reto && adduser -S reto -G reto
USER reto

COPY --from=build /build/target/*.jar app.jar

EXPOSE 8088
ENTRYPOINT ["java", "-jar", "app.jar"]
