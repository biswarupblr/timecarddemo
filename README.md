# Timecard Demo

A Spring Boot REST API for managing weekly employee timecards with a simple workflow:

- `DRAFT` → create/update entries
- `SUBMITTED` → employee submits timecard
- `APPROVED` → approver finalizes timecard

For architecture details, see [HIGH_LEVEL_DESIGN.md](./HIGH_LEVEL_DESIGN.md).

## Tech Stack
- Java 17
- Spring Boot (Web, Data JPA, Validation)
- H2 in-memory database
- Maven

## Project Structure
- `timecarddemo/src/main/java/org/example/timecard/controller` — REST endpoints
- `timecarddemo/src/main/java/org/example/timecard/service` — business logic and workflow
- `timecarddemo/src/main/java/org/example/timecard/domain` — entities and status enum
- `timecarddemo/src/main/java/org/example/timecard/repository` — persistence layer
- `timecarddemo/src/main/java/org/example/timecard/exception` — exception handling
- `timecarddemo/src/main/resources` — app config and sample HTTP request

## API Endpoints
Base path: `/api/timecards`

- `POST /{employeeId}/{weekStart}` — create/update draft entries
- `POST /{employeeId}/{weekStart}/submit` — submit draft
- `POST /{employeeId}/{weekStart}/approve` — approve submitted timecard
- `GET /` — list all timecards

## Run Locally
From repository root:

```bash
cd timecarddemo
mvn spring-boot:run
```

The service starts on `http://localhost:8080` by default.

## Example Request
Use the sample request file:

- `timecarddemo/src/main/resources/timecard.http`

Or send directly:

```bash
curl -X POST "http://localhost:8080/api/timecards/E1/2026-01-01" \
  -H "Content-Type: application/json" \
  -d '{
    "entries": [
      { "date": "2026-01-01", "jobCode": "DEV", "minutes": 480 }
    ]
  }'
```

## Database
The app uses in-memory H2 for development:

- JDBC URL: `jdbc:h2:mem:timecarddb`
- H2 console: `http://localhost:8080/h2-console`

## Notes
- Data is ephemeral because H2 is in-memory.
- Concurrency conflicts are handled via optimistic locking (`409 Conflict`).
- This is a demo app and currently has no authentication/authorization.
