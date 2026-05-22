FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src

RUN mvn clean package -DskipTests

# Verificar jars generados
RUN ls -lah target

# Copiar el jar correcto a un nombre fijo
RUN cp target/budget-optimizer-backend-0.0.1-SNAPSHOT.jar target/app.jar

RUN ls -lah target

FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

COPY --from=build /app/target/app.jar /app/app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]