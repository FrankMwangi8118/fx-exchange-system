# fx exchange Microservice Platform

## Project Overview

This project implements a microservice-based platform for performing currency conversions. It consists of two core services:

- `rate-service`: Responsible for fetching and caching real-time exchange rates from an external API.
- `main-service`: Handles conversion requests, applies business logic, interacts with the rate service, and persists conversion history.

The platform is designed with resilience, scalability, and cost-efficiency in mind, particularly addressing the challenges associated with relying on rate-limited external data sources.

## Key Features

- Currency Conversion: Provides an API endpoint to convert an amount from one currency to another using the latest exchange rates.
- Real-time Exchange Rates: Fetches current exchange rates from [freecurrencyapi.com](https://freecurrencyapi.com).
- Intelligent Caching: Implements a background-aware lazy caching strategy using Caffeine. A scheduled job runs every 20 minutes and fills only missing or stale values, minimizing external API calls and ensuring low-latency responses.
- Conversion History: Persists details of each conversion request to a PostgreSQL database using Spring Data JDBC. The schema is defined in `schema.sql`.
- Robust Validation and Error Handling: Uses Jakarta Bean Validation for input validation and `@ControllerAdvice` for centralized error handling and informative responses.
- API Security: Secures both services using API Key authentication to prevent unauthorized access.
- Logging: Logs application events to files with daily rolling policies for operational monitoring.
- Containerization: Provides `Dockerfile`s for each service and a `docker-compose.yml` for orchestrating services and PostgreSQL.
- Comprehensive Testing: Includes unit tests with mocks for external dependencies (API, Database) using JUnit and Mockito.
- Health Endpoints: Each service exposes a `/status` endpoint for basic health checks.

## Technical Stack

- Backend: Spring Boot (Java)
- API Client: Spring WebClient
- Caching: Caffeine
- Database: PostgreSQL
- Persistence: Spring Data JDBC
- Validation: Jakarta Bean Validation
- Security: Spring Security with API Key
- Logging: Logback (with file output and rolling policy)
- Containerization: Docker, Docker Compose
- Build Tool: Maven
- Testing: JUnit, Mockito, Spring Boot Test

## Architecture

The system is composed of two decoupled microservices:

- `rate-service`: Connects to the external API, caches results using Caffeine, and serves the latest exchange rates.
- `main-service`: Accepts conversion requests, fetches rates from `rate-service`, calculates converted values, and persists transaction details in PostgreSQL.

Each service is containerized and can be scaled independently. Communication between services is done via HTTP using REST APIs.

## Endpoints

### rate-service

- `GET /status`: Health check endpoint.
- `GET /rate?from={from}&to={to}`: Retrieves the current exchange rate from one currency to another.

### main-service

- `GET /status`: Health check endpoint.
- `POST /convert`: Accepts a JSON body with `from`, `to`, and `amount`. Validates the request, fetches the exchange rate from `rate-service`, calculates the converted amount, and returns the result. Also persists the request to the database.

## Running the Project

1. Clone the repository.
2. Build the services using Maven:

   ```bash
   mvn clean install
