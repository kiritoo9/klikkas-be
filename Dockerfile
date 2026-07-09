FROM maven:3.9.11-eclipse-temurin-21-alpine AS build

WORKDIR /workspace

COPY pom.xml .
RUN mvn -B -ntp dependency:go-offline

COPY src ./src
RUN mvn -B -ntp clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine

RUN addgroup -S klikkas && adduser -S klikkas -G klikkas

WORKDIR /app
COPY --from=build --chown=klikkas:klikkas /workspace/target/*.jar app.jar

USER klikkas

EXPOSE 5001

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -XX:+UseG1GC"

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
