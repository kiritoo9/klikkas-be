# Build stage
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /app

# Build arguments (can be passed from .env via docker-compose)
ARG APP_NAME=klikkas
ARG APP_PORT=5001
ARG DB_HOST=db
ARG DB_PORT=5432
ARG DB_NAME=klikkas

# Copy pom.xml first for dependency caching
COPY pom.xml ./
RUN mvn dependency:go-offline -B

# Copy source and build
COPY src ./src
COPY mvnw ./
RUN chmod +x mvnw
RUN mvn package -DskipTests -B

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Install Tesseract OCR and Indonesian language data
RUN apk add --no-cache tesseract-ocr tesseract-ocr-data-ind

# Create non-root user
RUN addgroup -S klikkas && adduser -S klikkas -G klikkas

# Copy jar from build stage
COPY --from=builder /app/target/*.jar app.jar

# Set ownership
RUN chown -R klikkas:klikkas /app

USER klikkas

# Environment variables from build args
ENV APP_NAME=${APP_NAME} \
    APP_PORT=${APP_PORT} \
    DB_HOST=${DB_HOST} \
    DB_PORT=${DB_PORT} \
    DB_NAME=${DB_NAME}

EXPOSE ${APP_PORT}

ENTRYPOINT ["java", "-jar", "app.jar"]
