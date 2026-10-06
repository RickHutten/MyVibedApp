# Architecture

## Status

This document describes the initial direction. It is not a finalized design. Record important decisions and their trade-offs here as the product becomes clearer.

## Technology baseline

- Backend: Spring Boot 4.1.1 on Java 25
- Build: Maven
- Backend nullness: JSpecify annotations with NullAway nullness checking enabled in the build
- Backend programming style: immutable, functional-oriented Java using `Optional` for genuinely absent return values and `Stream` for collection transformations
- Backend boilerplate: Lombok for required-argument constructors and other generated code only when it meaningfully reduces repetition
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

## Backend organization and coding style

Backend code is organized by feature rather than by a repository-wide technical layer. Each feature owns its domain, application use cases, and inbound/outbound adapters:

```text
<feature>/
├── domain/
├── application/
└── adapters/
    ├── in/
    └── out/
```

Feature implementations are hidden behind package-private classes where possible. Cross-feature collaboration uses small public application contracts and application-owned value types; features must not reach into another feature's persistence, controller, or adapter internals. Shared packages are reserved for stable cross-cutting infrastructure such as configuration, HTTP client setup, errors, and time abstractions.

Java code uses JSpecify nullness annotations, with packages marked non-null by default and nullable values explicitly annotated with `@Nullable`. NullAway treats nullness violations as build failures. Domain and application code favors immutable records and values, pure transformations, and explicit return types. Methods return `Optional` when absence is a valid result, rather than returning `null`; required values do not use `Optional`. Streams are preferred for collection transformations, while they are not forced into code where a clear single-expression or domain-specific operation is more readable. Mutable state, in-place collection mutation, and imperative loops require a concrete readability or performance reason. Lombok's `@RequiredArgsConstructor` is preferred for required dependency-injection constructors; `@Builder`, `@Getter`, and `@Setter` are used only when they remove meaningful boilerplate, and records remain preferred for immutable values.

External provider payload records are validated immediately after deserialization at the owning outbound adapter boundary. Use Jakarta Bean Validation annotations for structural requirements such as required fields, nested objects, collection contents, and simple ranges; keep provider-specific semantic checks and transformations in the adapter. Only validated, application-owned values cross into the domain and application layers.

The backend follows a functional-core/imperative-shell style: domain decisions and transformations should be side-effect-free where practical, while HTTP, persistence, and external-provider integration remain explicit side-effecting boundaries.

Integration tests use reusable scenario builders for stateful setup. Fixture definitions create valid sub-entity data, while one scenario persister owns ordering, relationships, and database writes. Scenarios compose through named presets and flat overrides rather than deeply nested builders. Persisted state is cleaned between tests, and scenario setup remains separate from production code; it does not replace assertions through public application boundaries.

## Initial system boundaries

### Frontend

The Angular application renders the tablet dashboard and normal browser experience. It should not call third-party providers directly when doing so would expose credentials or duplicate integration logic.

Frontend features that depend on the dashboard location consume one application-owned location service rather than defining their own coordinates or timezone. The initial location is Amsterdam (`52.3676`, `4.9041`, `Europe/Amsterdam`). Weather uses its coordinates for backend requests, while the clock uses its IANA timezone so daylight-saving changes are handled by the platform. Location selection, persistence, and browser geolocation remain future work.

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
- Refresh, caching, and stale-data policies
- Secrets management
