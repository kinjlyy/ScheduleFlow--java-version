# ── Build Stage ────────────────────────────────────────────────────────────────
FROM maven:3-eclipse-temurin-21 AS build
WORKDIR /app
# Cache dependencies layer separately from source
COPY pom.xml .
RUN mvn dependency:go-offline -q
COPY src ./src
RUN mvn clean package -DskipTests -q

# ── Runtime Stage ──────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Create non-root user for security
RUN addgroup -S scheduleflow && adduser -S scheduleflow -G scheduleflow
USER scheduleflow

COPY --from=build /app/target/*.jar app.jar

# Environment variables (all required; no defaults — use docker-compose or Render env)
# DATABASE_URL, DB_USER, DB_PASS, EUREKA_URI
ENV PORT=8083

EXPOSE 8083

ENTRYPOINT ["java", "-jar", "app.jar"]
