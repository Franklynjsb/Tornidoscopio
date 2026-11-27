FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY api/pom.xml .
COPY api/src ./src
RUN mvn -B clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
ARG JAR_FILE=target/tp-0.0.1-SNAPSHOT.jar
COPY --from=build /app/${JAR_FILE} app.jar

# Seguridad: crear usuario no‑root
RUN groupadd -r appgroup && useradd -r -g appgroup appuser && chown -R appuser:appgroup /app
USER appuser

EXPOSE 8080
CMD ["java","-Dserver.port=${PORT:8080}","-jar","app.jar"]