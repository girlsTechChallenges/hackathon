# Etapa 1: Build da aplicação usando Maven com JDK 21
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copia o pom.xml e baixa dependências
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copia o código fonte e compila
COPY src ./src
RUN mvn clean package -DskipTests

# Etapa 2: Imagem final com JDK 21 (não-Alpine)
FROM eclipse-temurin:21-jdk
WORKDIR /app

# Copia o JAR gerado da etapa anterior
COPY --from=build /app/target/brain-health-*.jar app.jar

# Define variáveis de ambiente (serão injetadas pelo docker-compose)
ENV JAVA_OPTS=""
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]