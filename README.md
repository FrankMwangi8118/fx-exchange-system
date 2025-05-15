[# fx exchange Microservice Platform

## Project Overview

This project implements a microservice-based platform for performing currency conversions. It consists of two core
services:

- `rate-service`: Responsible for fetching and caching real-time exchange rates from an external API.
- `main-service`: Handles conversion requests, applies business logic, interacts with the rate service, and persists
  conversion history.

The platform is designed with resilience, scalability, and cost-efficiency in mind, particularly addressing the
challenges associated with relying on rate-limited external data sources.

## Key Features

- Currency Conversion: Provides an API endpoint to convert an amount from one currency to another using the latest
  exchange rates.
- Real-time Exchange Rates: Fetches current exchange rates from [freecurrencyapi.com](https://freecurrencyapi.com).
- Intelligent Caching: Implements a background-aware lazy caching strategy using Caffeine. A scheduled job runs every 20
  minutes and fills only missing or stale values, minimizing external API calls and ensuring low-latency responses.
- Conversion History: Persists details of each conversion request to a PostgreSQL database using Spring Data JDBC. The
  schema is defined in `schema.sql`.
- Robust Validation and Error Handling: Uses Jakarta Bean Validation for input validation and `@ControllerAdvice` for
  centralized error handling and informative responses.
- API Security: Secures both services using API Key authentication to prevent unauthorized access.
- Logging: Logs application events to files with daily rolling policies for operational monitoring.
- Containerization: Provides `Dockerfile`s for each service and a `docker-compose.yml` for orchestrating services and
  PostgreSQL.
- Comprehensive Testing: Includes unit tests with mocks for external dependencies (API, Database) using JUnit and
  Mockito.
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
- `main-service`: Accepts conversion requests, fetches rates from `rate-service`, calculates converted values, and
  persists transaction details in PostgreSQL.

Each service is containerized and can be scaled independently. Communication between services is done via HTTP using
REST APIs.

## Endpoints

### rate-service

- GET /status: Returns the health status of the service.

- Response: { "status": "UP" }
- GET /rate?from={from}&to={to}: Retrieves the exchange rate between two currencies from freecurrencyapi.com, leveraging the caching layer.

- Requires API Key Authentication (used by main-service).
 Parameters:

- from (string, required): The base currency code (e.g., "USD").
to (string, required): The target currency code (e.g., "EUR").


Response: { "rate": 0.85 } (Example)
Note: This endpoint is primarily for internal use by main-service.

### main-service

- `GET /status`: Health check endpoint.
- `POST /convert`: Accepts a JSON body with `from`, `to`, and `amount`. Validates the request, fetches the exchange rate
  from `rate-service`, calculates the converted amount, and returns the result. Also persists the request to the
  database.

## Running the Project

To run this project locally using Docker Compose:

1. ### Prerequisites:

- Java 17+
- Maven 3.6+
- Docker Desktop (or Docker Engine and Docker Compose)
- An API Key from [freecurrencyapi.com](freecurrencyapi.com) (you'll need to sign up and obtain one).

2. ### Clone the Repository

```bash
git clone https://github.com/FrankMwangi8118/fx-exchange-system.git
cd fx-exchange-system 
```

3.

```bash
   cp ./rate-serviceExample.env
   cp ./main-serviceExample.env
   ```

Edit the newly created.env files for both services and fill in the required values, including:

- Your freecurrencyapi.com API Key.
- Database credentials (username, password, database name) - ensure these match the configuration in docker-compose.yml
  and the Spring Boot application properties.
- API Keys/passphrases for service-to-service authentication (e.g., main-service calling rate-service).
- caffeine and webclient configs

4. ### Build Docker Images:

Use the provided deployment script to build the Docker images for both services:
for rate service

```bash
cd rate-service
chmod +x deploy.sh
. deploy.sh
```

for main-service

- make sure you are in the parent directory

```bash
cd main-service
chmod +x deploy.sh
. deploy.sh
```

(Alternatively, you can build manually:mvn clean package -DskipTests followed by docker-compose build)

### 5. Run with Docker Compose:

Start the services and the database using Docker Compose. This command reads variables from the .env file:

```bash
docker-compose up -d
```

- This will start the rate-service, main-service , and postgres containers. With the SPRING_SQL_INIT_MODE=always
  configuration , the schema.sql file will be automatically executed by Spring Data JDBC on main-service startup to set
  up the database table.

### 6. Verify Services:

- Check the status of rate-service: GET http://localhost:8081/status (Expected: { "status": "UP" })
- Check the status of main-service: GET http://localhost:8080/status (Expected: { "status": "UP" })

### 7.Accessing the API:

The main-service API is available at http://localhost:8080. You will need to provide the configured API Key
credentials (as configured in the security setup and.env file) to access the /convert endpoint.

## Technical Highlights & Design Decisions
 ### intelligent Background-Aware Lazy Caching (Caffeine).

A critical challenge when relying on external, rate-limited APIs like [freecurrencyapi.com](freecurrencyapi.com) is balancing data freshness with API usage constraints. A naive caching approach could still lead to frequent API calls for uncached pairs or cache misses.
This project implements a background-aware lazy caching strategy using Caffeine , specifically tailored to handle both frequently and infrequently requested currency pairs efficiently.