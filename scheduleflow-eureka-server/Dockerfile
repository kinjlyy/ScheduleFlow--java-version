# ── Build Stage ────────────────────────────────────────────────────────────────
FROM maven:3-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -q
COPY src ./src
RUN mvn clean package -DskipTests -q

# ── Runtime Stage ──────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S scheduleflow && adduser -S scheduleflow -G scheduleflow
USER scheduleflow

COPY --from=build /app/target/scheduleflow-eureka-server-1.0.0.jar app.jar

ENV PORT=8761

EXPOSE 8761

ENTRYPOINT ["java", "-jar", "app.jar"]
