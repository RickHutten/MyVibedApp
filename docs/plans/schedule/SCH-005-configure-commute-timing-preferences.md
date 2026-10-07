# Implementation plan: SCH-005 — Configure commute timing preferences

Related story: [SCH-005 — Configure commute timing preferences](../../features/schedule/SCH-005-configure-commute-preferences.md)
Status: Draft

## Approach

Extend the schedule feature's persisted configuration with one single-user commute-preferences record and a small settings section. The backend will expose application-owned read and replace operations for the wake-up lead, first-transit buffer, and office-arrival lead; the Angular settings experience will load and save those values through the backend rather than treating browser state as the source of truth.

The commute origin is the existing `DashboardLocationService`, which already supplies the shared weather/clock coordinates, so no second home-location setting or persistence field is introduced. Keep timing arithmetic in an immutable schedule/commute domain value that can later be consumed by the outbound-commute stories: target arrival at the scheduled working-hours start minus the configured office-arrival lead, subtract walking time and the configured buffer only from the first transit departure, subtract the wake-up lead from the exact leave-home time, and floor the displayed wake-up time to a 15-minute boundary.

Build on the PostgreSQL, Flyway, jOOQ, schedule application contract, settings route, and integration-test support established by SCH-001. Do not add a transport provider or route lookup to this story; route and departure data remain inputs supplied by the later commute stories.

## Implementation steps

1. **Confirm the schedule extension points**
   - Change: Inspect the SCH-001 schedule application contract, persistence migration, generated jOOQ setup, settings route, HTTP validation format, shared dashboard location, and scenario-test support. Add commute preferences to those existing boundaries rather than creating a parallel settings or persistence mechanism.
   - Files: `../../../backend/src/main/java/nl/codestar/myvibedapp/schedule`, `backend/src/test/java/nl/codestar/myvibedapp/schedule/`, `frontend/src/app/work-schedule/`, `frontend/src/app/shared/dashboard-location.service.ts`

2. **Add persisted commute preferences**
   - Change: Add a Flyway migration for the single-user commute-preferences row, storing non-negative whole-minute wake-up lead, first-transit buffer, and office-arrival lead values. Initialize missing data with 45, 5, and 5 minutes. Add jOOQ repository methods and explicit mappers for reading and replacing the values; keep generated database types inside the persistence adapter.
   - Files: `../../../backend/src/main/resources/db/migration`, `backend/src/main/java/nl/codestar/myvibedapp/schedule/adapters/out/persistence/`, generated jOOQ sources

3. **Model and validate the application behavior**
   - Change: Add immutable application/domain values for commute timing preferences with defaults and non-negative whole-minute validation. Add a pure timing calculator that accepts the scheduled working-hours start, a candidate route's office arrival and first transit departure, first-leg walking duration, and preferences, then returns whether the route meets the target arrival, the exact leave-home time, exact wake-up time, and 15-minute-floor display value. Apply the access buffer only to the first transit leg; do not represent transfer buffers in this calculator.
   - Files: `../../../backend/src/main/java/nl/codestar/myvibedapp/schedule/domain`, `backend/src/main/java/nl/codestar/myvibedapp/schedule/application/`

4. **Expose the preferences API**
   - Change: Add validated application-owned records and thin Spring MVC endpoints at `GET` and `PUT /api/work-schedule/commute-preferences`. Expose wake-up lead, first-transit buffer, and office-arrival lead values. Reject negative or non-whole-minute input with the existing client-facing validation format, return the stored defaults when no explicit row exists, and make replacement durable across requests. Do not expose persistence records or add a home-location field to the contract.
   - Files: `../../../backend/src/main/java/nl/codestar/myvibedapp/schedule/adapters/in/web`, `backend/src/main/java/nl/codestar/myvibedapp/schedule/application/`

5. **Add the Angular commute-preferences settings section**
   - Change: Extend the existing lazy-loaded schedule settings experience with a focused form for wake-up lead, first-transit buffer, and office-arrival lead. Load the backend values as the source of truth, show the 45/5/5 defaults, accept non-negative whole minutes without imposing a product maximum, submit replacements, and show loading, validation, save, and unavailable states. Do not render a home-location control; retain the existing `DashboardLocationService` as the only frontend location source.
   - Files: `../../../frontend/src/app/work-schedule`, `frontend/src/app/shared/`, `frontend/src/app/app.routes.ts`

6. **Connect the shared location and timing contract to commute consumers**
   - Change: Ensure the application-owned commute client boundary used by the outbound-commute stories receives origin coordinates from `DashboardLocationService` and consumes the timing calculator/preferences contract rather than defining its own origin or buffer arithmetic. Keep this change limited to the shared contract needed by SCH-006 and do not add route-provider integration here.
   - Files: `../../../frontend/src/app/shared/dashboard-location.service.ts`, `frontend/src/app/commute/` or the schedule commute boundary established by SCH-006

7. **Document the lasting boundaries**
   - Change: Record that commute preferences are persisted separately from the shared dashboard location, that the first-transit buffer applies only to first-leg access, and that wake-up display values are floored to 15-minute boundaries. Keep user-facing settings behavior in SCH-005 and avoid documenting a provider-specific route design.
   - Files: `../../ARCHITECTURE.md`

8. **Verify the complete behavior and update the plan status**
   - Change: Run backend and frontend verification, check for duplicate home-location sources or stale commute-preference defaults, map every acceptance criterion to automated evidence, and change this plan's status to `Implemented` only after all checks pass.
   - Files: `../../../backend`, `frontend/`, `docs/plans/SCH-005-configure-commute-timing-preferences.md`

## Test strategy

- **Functional coverage:** Verify default preference retrieval, saving and reloading all three values, persistence across requests, acceptance of zero and large whole-minute values, rejection of negative and fractional values, absence of a home-location setting, and use of the shared dashboard location as the only commute origin. Verify a 09:00 working-hours start with a 5-minute arrival lead produces an 08:55 target arrival and rejects routes arriving later; verify timing arithmetic with a 09:00 first tram, 3-minute walk, and 5-minute buffer producing 08:52 leave-home; verify exact wake-up at 08:07 with a 45-minute lead and displayed wake-up at 08:00; verify exact quarter-hour values remain unchanged; verify the buffer is not applied to transfers.
- **Test level:** Use backend HTTP integration tests through MockMvc against PostgreSQL Testcontainers with real Flyway migrations and jOOQ repositories. Add focused domain tests for timing arithmetic and 15-minute flooring because those are pure logic with meaningful boundary permutations. Use Angular service/component tests at the HTTP and rendered settings boundaries for defaults, loading, save, validation, unavailable state, and the absence of a home-location control. Use user-defined fakes and test adapters only; do not use Mockito or another mocking framework. Add an end-to-end settings test only if the existing frontend setup supports a stable journey.
- **TDD sequence:** Add the public API or settings-boundary test first for each vertical slice, run it to confirm the expected missing-behavior failure, implement the smallest behavior, run it green, and then refactor without changing observable behavior.
- **Commands:** `cd backend && ./mvnw verify`; `cd frontend && npm run check`

## Technical decisions

- Store one application-owned preference record for the private single-user product. Do not introduce authentication or multi-user ownership in this story.
- Use non-negative whole minutes as the product validation rule and do not add a product-defined upper bound. Use a database numeric type large enough for the application's integer representation while keeping the API values explicit.
- Keep the dashboard location in the existing frontend location service. Commute preferences store timing only; they do not duplicate coordinates, display name, or timezone.
- Keep route lookup and provider-specific journey models outside this story. The timing calculator accepts application-owned arrival, departure, and walking-duration values so later providers cannot leak into the schedule domain.
- Treat the office-arrival lead as a target-arrival adjustment, not as another leave-home or transfer buffer. A route is suitable only when its office arrival is at or before scheduled start minus this lead.
- Apply the access buffer once, between the first transit departure and the route's walking time from the shared origin. Transfers are provider route details and receive no additional buffer from this preference.
- Calculate the exact wake-up instant before presentation rounding. Floor the displayed local time to the nearest 15-minute boundary, preserving values such as 08:15 and 08:45.
- Use jOOQ and Flyway rather than JPA or an in-memory production substitute. Keep generated jOOQ types inside the persistence adapter and map them explicitly to domain/application values.
- Follow the repository's JSpecify/NullAway, immutable-value, functional-core/imperative-shell, Angular signals/Signal Forms, and user-defined test-double conventions.
