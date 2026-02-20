# High-Level Design — Timecard Demo

## 1. Purpose
This document describes the architecture and behavior of the **Timecard Demo** service at a high level so new contributors can quickly understand:
- what the system does,
- how requests flow through layers,
- what core business rules are enforced,
- and how the service can evolve toward production readiness.

---

## 2. System Summary
The application is a Spring Boot REST service that manages weekly employee timecards.

### Core workflow
A timecard moves through these states:
1. **DRAFT** — employee creates or updates entries.
2. **SUBMITTED** — employee submits the card for review.
3. **APPROVED** — approver finalizes the card.

### Technology stack
- Java 17
- Spring Boot (Web, Data JPA, Validation)
- H2 in-memory database
- JPA/Hibernate

---

## 3. Scope and Context
### In scope
- CRUD-like operations for draft timecards (create/update by natural key).
- Workflow transitions (submit, approve).
- Validation and exception-to-HTTP mapping.

### Out of scope (current)
- Authentication / authorization.
- Multi-tenant isolation.
- Payroll integration.
- Audit/event streaming.

---

## 4. Architecture Overview
The service follows a classic layered architecture.

```text
Client
  |
  v
[Controller]  --> HTTP mapping + request binding + bean validation trigger
  |
  v
[Service]     --> business rules + state transitions + transaction boundary
  |
  v
[Repository]  --> JPA persistence operations
  |
  v
[H2 Database]
```

### Layers and responsibilities
- **Controller (`controller`)**
  - Defines REST endpoints under `/api/timecards`.
  - Delegates to service.
- **Service (`service`)**
  - Implements workflow and invariants.
  - Ensures operations execute transactionally.
- **Domain (`domain`)**
  - Entity model (`Timecard`, `TimeEntry`) + state enum.
  - Domain-level validation (`validateEntries()`).
- **Repository (`repository`)**
  - `JpaRepository` with lookup by `(employeeId, weekStart)`.
- **Exception (`exception`)**
  - Global exception handling to consistent HTTP responses.

---

## 5. Domain Model
## 5.1 Entities
### `Timecard`
- Surrogate key: `id`.
- Natural uniqueness: `employee_id + week_start`.
- Fields: `employeeId`, `weekStart`, `status`, `version`.
- Contains child entries (`Set<TimeEntry>` with cascade/orphan removal).
- Uses optimistic locking via `@Version`.

### `TimeEntry`
- Surrogate key: `id`.
- Fields: `date`, `jobCode`, `minutes`.
- Each entry belongs to exactly one `Timecard`.

### `TimecardStatus`
- `DRAFT`, `SUBMITTED`, `APPROVED`.

## 5.2 State transitions
Allowed transitions only:
- `DRAFT -> SUBMITTED`
- `SUBMITTED -> APPROVED`

Disallowed transitions produce a conflict response.

---

## 6. API Design (Current)
Base path: `/api/timecards`

1. `POST /{employeeId}/{weekStart}`
   - Create new draft or replace existing draft entries.
2. `POST /{employeeId}/{weekStart}/submit`
   - Submit draft timecard.
3. `POST /{employeeId}/{weekStart}/approve`
   - Approve submitted timecard.
4. `GET /`
   - List all timecards.

### Request validation
- Request body enforces non-empty entries list.
- Entry DTO defines constraints for date/jobCode/minutes.
- Domain enforces daily total minute limits.

---

## 7. Business Rules and Invariants
1. A timecard is uniquely identified by `(employeeId, weekStart)`.
2. Draft can be created/updated only while state is `DRAFT` (or when card does not exist yet).
3. Submit is valid only from `DRAFT`.
4. Approve is valid only from `SUBMITTED`.
5. For each date in a card, total minutes across entries must be:
   - **>= 480** (8 hours)
   - **<= 720** (12 hours)

---

## 8. Data and Transaction Design
- Service class is transactional (`@Transactional`).
- Read operation uses read-only transaction.
- Optimistic locking (`@Version`) protects concurrent updates.
- Unique DB constraint prevents duplicate weekly cards per employee.

### Concurrency behavior
If concurrent writes conflict, optimistic lock exceptions are translated to `409 Conflict`, signaling clients to retry.

---

## 9. Error Handling Contract
Global exception mapping:
- `NotFoundException` -> `404 Not Found`
- `InvalidStateException` -> `409 Conflict`
- `ValidationException` -> `400 Bad Request` with list of errors
- `ObjectOptimisticLockingFailureException` -> `409 Conflict`

---

## 10. Operational Characteristics
### Current strengths
- Small, understandable codebase.
- Clear business workflow.
- Good fit for demos/interviews/local development.

### Current limitations
- In-memory DB; data is ephemeral.
- No auth/authz.
- No explicit API response DTO boundary.
- Minimal observability and test coverage.

---

## 11. Recommended Evolution Plan
### Phase 1: API and validation hardening
- Add explicit response DTOs to decouple API from persistence model.
- Ensure nested DTO validation cascades consistently.
- Enforce entry date in target week range.

### Phase 2: Data model and reliability
- Clarify duplicate-entry semantics (`Set` vs `List`) and equality rules.
- Add migration tooling (Flyway/Liquibase).
- Move to PostgreSQL/MySQL.

### Phase 3: Security and ops
- Add Spring Security with role-based actions (employee vs approver).
- Add actuator health checks + metrics + structured logging.
- Expand integration tests for workflow and concurrency.

---

## 12. Repository Map
- `timecarddemo/src/main/java/org/example/timecard/controller` — REST API endpoints
- `timecarddemo/src/main/java/org/example/timecard/service` — workflow/business logic
- `timecarddemo/src/main/java/org/example/timecard/domain` — entities and status enum
- `timecarddemo/src/main/java/org/example/timecard/repository` — persistence abstraction
- `timecarddemo/src/main/java/org/example/timecard/exception` — exception types and HTTP mapping
- `timecarddemo/src/main/resources` — runtime config and API example payload

---

## 13. Assumptions
- Caller provides correctly formatted `weekStart` date in path.
- `approve` is currently trusted (no principal/role checks yet).
- Clients can tolerate `409` retry semantics on conflicting updates.

