# ---------- Estagio 1: build com Maven ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Primeiro so o pom: as dependencias ficam em cache enquanto o pom nao muda.
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
# Os testes rodam na maquina de desenvolvimento (mvn test); aqui so empacota.
RUN mvn -B -q package -DskipTests

# ---------- Estagio 2: execucao so com o JRE ----------
FROM eclipse-temurin:21-jre
WORKDIR /app

# Usuario sem privilegios para rodar a aplicacao.
RUN useradd --system --no-create-home tomei
USER tomei

COPY --from=build /app/target/tomei-doutor-api.jar app.jar

# Limite de memoria proporcional ao conteiner e horario de Brasilia.
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=60 -Duser.timezone=America/Sao_Paulo"

# A porta real vem da variavel PORT (o Render define). 8080 e o padrao fora dele.
# DB_URL, DB_USER e DB_PASSWORD sao informadas no ambiente, nunca aqui.
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
