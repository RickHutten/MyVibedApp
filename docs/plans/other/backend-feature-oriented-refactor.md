# Implementation plan: backend feature-oriented backend rewrite

Status: Implemented

## Objective

Rewrite the small existing backend into a clean feature-oriented foundation with JSpecify/NullAway nullness checking, functional-core/imperative-shell boundaries, and no-mocking-framework tests. Preserve the existing weather API behavior while removing starter/demo code that does not belong in the product.

This is a preparatory rewrite for SCH-001. It does not add scheduling, persistence, jOOQ, Flyway, or new product behavior.

## Approach

Rewrite the weather code directly into a feature-owned hexagonal structure while preserving the `/api/weather/current` contract and current error behavior. Do not preserve the old package structure or implementation merely to minimize file changes. Keep domain and application code independent of Spring, HTTP, and Open-Meteo. Keep the Open-Meteo integration as an outbound adapter and the weather controller as an inbound web adapter.

External provider response records are validated immediately after deserialization with Jakarta Bean Validation, then translated into non-null application/domain values or an application-owned unavailable-weather failure.

Replace the existing framework HTTP test double with a small user-defined controllable local HTTP server. Keep a user-defined provider fake where it provides useful controller-level isolation, and retain broad HTTP behavior tests as the primary regression protection.

## Implementation steps

1. **Define the clean backend foundation**
   - Change: Establish the feature-oriented package structure and identify the small set of production behavior to retain: the weather endpoint, weather domain values, the application weather port, the Open-Meteo adapter, and the application entry point. Remove starter-only endpoints and generic smoke tests from the target design.
   - Files: `../../../backend/src/main/java/nl/codestar/myvibedapp`, `backend/src/test/java/nl/codestar/myvibedapp/`

2. **Rewrite weather into a feature-oriented package**
   - Change: Create clean production classes under `weather/domain`, `weather/application`, `weather/adapters/in/web`, and `weather/adapters/out/openmeteo`. Rewrite package declarations, application contracts, adapters, DTO mapping, and tests rather than preserving the old layer-oriented implementation. Keep public types limited to deliberate application contracts and HTTP models.
   - Files: `../../../backend/src/main/java/nl/codestar/myvibedapp/weather`, `backend/src/test/java/nl/codestar/myvibedapp/weather/`

3. **Enforce JSpecify nullness with NullAway**
   - Change: Add the JSpecify annotation dependency and configure NullAway through the Maven compiler/annotation-processing setup. Mark the weather feature package non-null by default. Add Jakarta Bean Validation constraints to the adapter-owned Open-Meteo response records and validate them before mapping into non-null domain values or `WeatherUnavailableException`.
   - Files: `../../../backend/pom.xml`, `backend/src/main/java/nl/codestar/myvibedapp/weather/package-info.java`, weather adapter DTOs and mappers, Maven build configuration

4. **Keep functional boundaries clear**
   - Change: Preserve immutable records and application-owned contracts. Keep domain/application transformations side-effect-free where practical. Leave HTTP calls, exception translation, and logging at the adapter boundary instead of forcing streams or `Optional` into code where they do not represent the domain meaning.
   - Files: `../../../backend/src/main/java/nl/codestar/myvibedapp/weather/domain`, `backend/src/main/java/nl/codestar/myvibedapp/weather/application/`, `backend/src/main/java/nl/codestar/myvibedapp/weather/adapters/`

5. **Replace framework HTTP doubles with a user-defined stub**
   - Change: Replace `MockRestServiceServer` in the Open-Meteo adapter test with a small test-only controllable local HTTP server based on the JDK HTTP server. The stub should support configuring a response or failure and recording the received request for assertions. Keep it generic enough for future outbound-provider adapter tests without becoming a production abstraction.
   - Files: `backend/src/test/java/nl/codestar/myvibedapp/support/http/`, `../../../backend/src/test/java/nl/codestar/myvibedapp/weather/adapters/out/openmeteo`

6. **Rewrite behavior-focused regression tests**
   - Change: Test the rewritten application through MockMvc for the HTTP contract, use a user-defined controllable weather provider fake where useful, and verify the rewritten Open-Meteo adapter against the user-defined HTTP stub. Cover valid mapping, incomplete payloads, malformed timestamps, invalid provider values, unavailable responses, and request coordinates.
   - Files: `../../../backend/src/test/java/nl/codestar/myvibedapp/weather`

7. **Remove starter and dummy code**
   - Change: Delete `HelloController`, its test, and the generic `contextLoads` test. The rewritten backend should retain only production code and tests that support actual product behavior or reusable test infrastructure.
   - Files: `backend/src/main/java/nl/codestar/myvibedapp/hello/`, `backend/src/test/java/nl/codestar/myvibedapp/hello/`, `backend/src/test/java/nl/codestar/myvibedapp/MyVibedAppBackendApplicationTests.java`

8. **Verify the rewrite and document any build constraints**
   - Change: Run the complete backend verification, confirm no Mockito or other mocking framework is introduced, verify the endpoint contract remains unchanged, and configure the forked Java 25 compiler with the Error Prone module exports required by NullAway.
   - Files: `../../ARCHITECTURE.md`, `backend/pom.xml`, `backend/.mvn/jvm.config`

## Test strategy

- **Behavioral coverage:** Test `/api/weather/current` through MockMvc for successful responses, provider unavailability, and invalid coordinates. Do not retain a generic context-load test as a substitute for observable behavior.
- **Adapter integration:** Test the Open-Meteo adapter against the user-defined local HTTP stub and assert the application-owned weather result, provider failure translation, and request contract.
- **Nullness:** Treat NullAway failures as build failures and verify external nullable payloads are handled at the adapter boundary.
- **Test doubles:** Do not use Mockito or another mocking framework. Use the existing user-defined provider fake and the new user-defined controllable HTTP stub.
- **Commands:** `cd backend && ./mvnw verify`

## Non-goals

- Adding the schedule feature
- Adding PostgreSQL, Flyway, or jOOQ
- Changing endpoint paths, JSON contracts, or weather behavior
- Refactoring the Angular frontend
- Introducing ArchUnit
- Keeping the sample `HelloController` or generic context-load test

## Acceptance checks

- The existing weather endpoint behavior is unchanged.
- Weather production code is organized under one feature-owned package and does not retain the old layer-oriented package structure.
- Domain and application code do not depend on Spring, HTTP, or Open-Meteo models.
- JSpecify and NullAway are enabled and pass for the existing backend.
- The adapter tests use a user-defined HTTP stub rather than a mocking framework or framework HTTP mock server.
- Starter-only Hello World code and the generic context-load test are removed.
- The complete backend verification passes.
