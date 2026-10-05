# Architecture

## Status

This document describes the initial direction. It is not a finalized design. Record important decisions and their trade-offs here as the product becomes clearer.

## Technology baseline

- Backend: Spring Boot 4.1.1 on Java 25
- Build: Maven
- Frontend: Angular 21 LTS with standalone components and strict TypeScript
- Database: PostgreSQL
- Client updates: real-time communication where it provides user value

## Repository layout

```text
/
├── AGENTS.md
├── README.md
├── backend/
│   └── pom.xml
├── frontend/          # Angular workspace to be created
└── docs/
    ├── PRODUCT.md
    └── ARCHITECTURE.md
```

The repository is a monorepo. Backend and frontend remain independently buildable and deployable.

## Initial system boundaries

### Frontend

The Angular application renders the tablet dashboard and normal browser experience. It should not call third-party providers directly when doing so would expose credentials or duplicate integration logic.

### Backend

The Spring Boot application provides the application API, owns domain behavior, coordinates external integrations, and determines what information is currently relevant.

The initial HTTP API uses Spring MVC and exposes application endpoints below `/api`. Spring Boot Actuator provides operational health information below `/actuator`.

### Database

PostgreSQL stores application-owned data such as preferences, reminders, integration metadata, and normalized dashboard state. Database credentials and provider tokens must not be committed to the repository.

### External integrations

Calendar, task, weather, mapping, and traffic providers should be isolated behind application interfaces. This keeps provider-specific APIs out of the domain model and allows providers to be replaced or tested independently.

The initial weather integration uses Open-Meteo through a backend adapter. The frontend sends a location to the application-owned `/api/weather/current` endpoint and never depends on Open-Meteo's contract directly. WEA-001 requests weather once when the dashboard loads and does not add caching, retries, fallback data, or background refresh. The interface includes the attribution required by the provider's CC BY 4.0 terms.

## Real-time updates

The dashboard needs timely updates, but the transport has not been selected. Server-Sent Events are a likely starting point for primarily server-to-client updates. WebSockets should be chosen only if bidirectional real-time communication is required.

## Initial quality attributes

- Privacy: minimize collection and exposure of location, schedule, and reminder data.
- Resilience: one unavailable provider should not make the whole dashboard unusable.
- Observability: integration failures and stale data should be diagnosable.
- Testability: external providers should be replaceable with fakes in automated tests.
- Maintainability: domain logic should not depend directly on controllers, database entities, or provider SDKs.

## Decisions still required

- Authentication and deployment model
- Local-only versus hosted operation
- Providers for calendar, tasks, mapping, and traffic
- API style and versioning
- Server-Sent Events versus WebSockets
- Database migration tool
- Refresh, caching, and stale-data policies
- Secrets management
