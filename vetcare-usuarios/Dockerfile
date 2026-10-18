# ---------- Etapa 1: compilacion ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Primero el pom, para aprovechar la cache de dependencias entre compilaciones
COPY pom.xml .
RUN mvn -B -ntp dependency:go-offline

COPY src ./src
RUN mvn -B -ntp clean package -DskipTests

# ---------- Etapa 2: ejecucion ----------
FROM eclipse-temurin:21-jre
WORKDIR /app

# Usuario sin privilegios: la aplicacion no necesita ser root
RUN useradd -r -u 1001 vetcare
COPY --from=build /app/target/*.jar app.jar
USER vetcare

EXPOSE 8081

# La configuracion sensible llega por variables de entorno, nunca dentro de la imagen
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
