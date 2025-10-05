# -------- Etapa 1: build --------
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline

COPY . .
RUN mvn clean package -DskipTests

# -------- Etapa 2: runtime --------
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

ENV PORT=10000
EXPOSE ${PORT}

ENTRYPOINT ["java","-jar","app.jar"]
