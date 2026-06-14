# Klikkas Service

A Spring Boot REST API application with JWT authentication and PostgreSQL database.

## Overview

Klikkas is a backend API for simple cash-flow system, this app will help to manage your store's cash-flow easily.
You will love it trust me!

## Tech Stack

- **Framework:** Spring Boot 4.1.0
- **Language:** Java 21
- **Database:** PostgreSQL
- **ORM:** Spring Data JPA
- **Authentication:** JWT (jjwt)
- **Security:** Spring Security

## Features

- JWT-based authentication
- User management (CRUD operations)
- Password encryption with BCrypt
- PostgreSQL database integration
- RESTful API design

## Installation

### Prerequisites

- **Java 21** or higher
- **Maven** (or use included `mvnw`)
- **PostgreSQL** database

### 1. Clone and Configure

```bash
# Clone the repository
cd klikkas

# Set environment variables (or create .env file)
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=klikkas
export DB_USER=your_user
export DB_PASS=your_password
export JWT_SECRET=your-256-bit-secret-key-here-min-32-chars
export APP_NAME=klikkas
export APP_PORT=8080
```
Or you can create with <code>.sh</code> file.

### 2. Create PostgreSQL Database

```sql
CREATE DATABASE klikkas;
```

### 3. Install Dependencies

```bash
# Using Maven wrapper (no need to install Maven)
./mvnw install
```

### 4. Run Database Migrations

> **Note:** Flyway is currently disabled in `application.properties` (`spring.flyway.enabled=false`). 
> To enable, set `spring.flyway.enabled=true` and ensure migrations exist in `src/main/resources/db/migration/`.

## Running the Application

### Development Mode

```bash
./mvnw spring-boot:run
```

The API will be available at `http://localhost:8080`

### Build and Run JAR

```bash
./mvnw clean package -DskipTests
java -jar target/klikkas-0.0.1-SNAPSHOT.jar
```

## Usage Examples

### Login

```bash
curl -X POST http://localhost:8080/login \
  -H "Content-Type: application/json" \
  -d '{"email": "user@example.com", "password": "password123"}'
```

**Response:**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
}
```

### Access Protected Endpoint

```bash
curl http://localhost:8080/users \
  -H "Authorization: Bearer <your_access_token>"
```

### Health Check (No Auth Required)

```bash
curl http://localhost:8080/healthcheck
```

## Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `DB_HOST` | PostgreSQL host | `localhost` |
| `DB_PORT` | PostgreSQL port | `5432` |
| `DB_NAME` | Database name | `klikkas` |
| `DB_USER` | Database user | - |
| `DB_PASS` | Database password | - |
| `JWT_SECRET` | JWT signing secret (min 32 chars) | - |
| `APP_NAME` | Application name | `klikkas` |
| `APP_PORT` | Server port | `8080` |

## Common Issues

### 401 Unauthorized on protected endpoints
- Ensure you're sending the `Authorization: Bearer <token>` header
- Check that your JWT token hasn't expired
- Verify the token was generated from a successful `/login` call

### 403 Forbidden
- The filter might not be setting the SecurityContext correctly
- Check that `JwtAuthenticationFilter` is extracting and validating the token properly

### Database connection errors
- Verify PostgreSQL is running
- Check environment variables for correct credentials
- Ensure the database exists

## License

Private project - All rights reserved
