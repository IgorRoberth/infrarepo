
# Estágio 1: Build (Maven no Alpine)
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build
WORKDIR /workspace

# Cache das dependências para acelerar o build
COPY pom.xml .
RUN mvn dependency:go-offline

# Copia o código e gera o pacote (ignora testes para poupar memória no build)
COPY src ./src
RUN mvn clean package -DskipTests

# Estágio 2: Runtime (JRE Alpine - MUITO menor que a Jammy)
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Cria um usuário não-root por segurança e economia de recursos
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copia apenas o JAR final (descartando os 500MB+ do Maven/JDK)
COPY --from=build /workspace/target/*.jar app.jar

EXPOSE 8080

# Flags JVM para desenvolvimento em ambiente de baixa RAM
# -XX:MaxRAMPercentage=75.0: Garante que o Java use apenas 75% do limite do container
# -XX:+UseSerialGC: Coletor de lixo que consome menos RAM/CPU em apps pequenas
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-XX:+UseSerialGC", "-jar", "app.jar"]