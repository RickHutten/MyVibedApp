# Backend Guidelines
These rules apply to `backend/` and supplement the repository-level `AGENTS.md`. The root rules still apply unless this file is more specific.

## Technology and commands
- Use Java 25, Spring Boot, and Maven through the committed Maven Wrapper.
- Do not add a dependency when the JDK or Spring already provides a suitable solution.
- Run the complete backend verification before declaring work finished:
```sh
./mvnw verify
```

## Structure
- Use top-level layers such as `interface`, `application`, `domain`. Use hexagonal clean design.
- Keep controllers, services, domain models, repositories, and external-provider adapters in their respective layers.
- Keep domain and service code independent of HTTP, persistence, and provider-specific models.
- Keep classes and methods (package-)private unless another package genuinely needs access.
- Prefer constructor injection. Do not use field injection. 
- Prefer lombok for builders, constructors, setters and getters. Prefer builder approach over constructor for >1 arguments. Lombok usage other than these are to be avoided.
- Prefer immutable values and records for simple request, response, and integration DTOs.

## HTTP APIs

- Expose application endpoints under `/api`.
- Keep controllers thin: validate and translate HTTP input, delegate behavior, and translate the result.
- Validate all external input at the boundary with Jakarta Validation where appropriate.
- Return interface-owned response models; never expose persistence entities or third-party provider models.
- Do not leak stack traces, credentials, provider payloads, or internal exception details to clients.

## External integrations

- Hide each external provider behind an application-owned interface. Implement them in a separate adapter class in the interface layer.
- Keep provider URLs and non-secret settings in configuration rather than application logic.
- Keep credentials outside source control.
- Configure finite connection and response timeouts.
- Translate provider responses and failures into application-owned models and exceptions.
- Do not add retries, caching, or fallback behavior unless the approved story requires them.

## Persistence

- Use PostgreSQL for persistent application data; do not substitute H2 for production behavior.
- Keep database access behind repositories and keep persistence entities out of API contracts.
- Apply schema changes through versioned migrations once persistence is introduced.
- Define transaction boundaries in the application/service layer, not in controllers.
- Avoid unbounded queries and implicit lazy-loading across application boundaries.

## Testing

- Follow the repository's testing honeycomb: favor integration tests around observable behavior and stable boundaries.
- For HTTP behavior, prefer tests through the application API, such as `MockMvc`, over direct controller method tests.
- For provider adapters, test against a controllable HTTP stub and assert the application contract rather than HTTP-client internals.
- For persistence behavior, use PostgreSQL-compatible integration tests; prefer Testcontainers once database support is introduced.
- Add focused unit tests only for complex isolated logic with meaningful input permutations.
- Avoid tests that assert private methods, internal call order, framework wiring, or incidental implementation details.
- A refactor that preserves externally observable behavior should not require test changes.
- For behavior changes, first demonstrate the missing behavior with an appropriate failing test, then implement it.

## Code quality

- Prefer clear domain names over generic names such as `Manager`, `Helper`, or `Util`.
- Prefer to use composition over inheritance.
- Prefer functional style code, avoid `void` functions.
- Keep methods focused and side effects explicit.
- Handle failures at the layer that can add meaningful context; do not catch exceptions merely to log and rethrow them.
- Use structured logging and never log secrets or precise personal data.
- Do not add speculative abstractions, generic frameworks, or unused extension points.
- Match the existing formatting; do not introduce a formatter or lint plugin without user approval.
