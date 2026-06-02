# EventosTec API

REST API for managing tech events, addresses, coupons, and event images.

This project is built with Spring Boot and uses PostgreSQL, Spring Data JPA, Flyway migrations, and Amazon S3 for image upload storage.

## Features

- Create events with optional image uploads
- List upcoming events with pagination
- Filter events by title, city, state, and date range
- Get event details with active coupons
- Add coupons to events
- Store event address data for in-person events

## Tech Stack

- Java 21
- Spring Boot 4
- Spring Web MVC
- Spring Data JPA
- PostgreSQL
- Flyway
- Amazon S3 SDK
- Maven
- H2 for tests

## Requirements

- Java 21
- PostgreSQL running locally or another configured database
- AWS S3 bucket and credentials if image upload is used

## Configuration

Database and AWS settings should be configured through environment variables, deployment secrets, or local configuration files that are not committed to the repository.

Do not commit real database credentials, AWS keys, bucket names, or other sensitive values.

## Running The Project

```bash
./mvnw spring-boot:run
```

The API will start on the default Spring Boot port:

```text
http://localhost:8080
```

## Running Tests

```bash
./mvnw test
```

Tests use an H2 in-memory database through `src/test/resources/application.properties`, so they do not require a local PostgreSQL instance.

## Main Endpoints

```text
POST /api/event
GET  /api/event
GET  /api/event/{eventId}
GET  /api/event/filter
POST /api/coupon/event/{eventId}
```

## Database Migrations

Flyway migrations are stored in:

```text
src/main/resources/db/migration
```

They create the `event`, `coupon`, and `address` tables.

## Credits

This project was inspired by the work and educational content of Fernanda Kipper, a Brazilian developer known for sharing backend and full-stack development knowledge with the community.
