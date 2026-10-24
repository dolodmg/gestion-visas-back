# Build stage
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app

# Copiar archivos de configuración de Maven
COPY pom.xml .
COPY src ./src

# Construir el proyecto (esto genera el .jar)
RUN mvn clean package -DskipTests

# Production stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copiar el JAR generado desde el stage anterior
COPY --from=builder /app/target/*.jar app.jar

# Exponer el puerto de Spring Boot
EXPOSE 8080

# Comando para ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]