FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src

RUN mvn clean package -DskipTests

# Verificar jars generados
RUN ls -lah target

# Copiar el jar correcto a un nombre fijo
RUN find target -name "*.jar" ! -name "*.original" -exec cp {} target/app.jar \;

FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

COPY --from=build /app/target/app.jar /app/app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]