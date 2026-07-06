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
export APP_PORT=5001
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

The API will be available at `http://localhost:5001`

### Build and Run JAR

```bash
./mvnw clean package -DskipTests
java -jar target/klikkas-0.0.1-SNAPSHOT.jar
```

## Usage Examples

### Login

```bash
curl -X POST http://localhost:5001/login \
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
curl http://localhost:5001/users \
  -H "Authorization: Bearer <your_access_token>"
```

### Health Check (No Auth Required)

```bash
curl http://localhost:5001/healthcheck
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
| `APP_PORT` | Server port | `5001` |

## Docker Deployment

### Prerequisites

- **Docker** and **Docker Compose**

### Quick Start

```bash
# 1. Copy environment template
cp .env.example .env

# 2. Edit .env with your production values
vim .env

# 3. Start the application
docker-compose up -d
```

The API will be available at `http://localhost:5001`

### Docker Commands

```bash
# Start services
docker-compose up -d

# View logs
docker-compose logs -f app

# Stop services
docker-compose down

# Rebuild without cache
docker-compose build --no-cache

# Rebuild and restart
docker-compose up -d --build
```

### Environment File (.env)

Create a `.env` file in the project root with these variables:

```bash
APP_NAME=klikkas
APP_PORT=5001

DB_HOST=db
DB_USER=your_db_user
DB_PASS=your_secure_password
DB_NAME=klikkas
DB_PORT=5432

JWT_SECRET=your_secure_jwt_secret_minimum_32_characters
```

> **Note:** The `.env` file is gitignored. Never commit secrets to version control.

### Production Tips

- Use strong passwords and JWT secrets
- Consider using Docker secrets for sensitive data in Swarm mode
- Enable HTTPS with a reverse proxy (nginx, traefik)
- Set up regular database backups
- Monitor container health with `docker-compose ps`

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
