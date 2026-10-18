# ---------- Etapa 1: compilacion ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml .
RUN mvn -B -ntp dependency:go-offline

COPY src ./src
RUN mvn -B -ntp clean package -DskipTests

# ---------- Etapa 2: ejecucion ----------
FROM eclipse-temurin:21-jre
WORKDIR /app

RUN useradd -r -u 1001 vetcare
COPY --from=build /app/target/*.jar app.jar
USER vetcare

EXPOSE 8085

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
