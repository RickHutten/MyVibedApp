# Implementation plan: SCH-006 — Show today’s outbound commute

Related story: [SCH-006 — Show today’s outbound commute](../../features/schedule/SCH-006-show-todays-outbound-commute.md)
Status: Draft

## Approach

Add a commute vertical slice to the schedule feature after [SCH-004](../../features/schedule/SCH-004-resolve-upcoming-work-locations.md) and [SCH-005](../../features/schedule/SCH-005-configure-commute-preferences.md) provide the resolved office-day and commute-preference contracts. The backend will coordinate today’s resolved office destination, the shared commute preferences, and an application-owned public-transit route port. It will calculate the target arrival, leave-home time, and wake-up time without exposing persistence or provider models.

The frontend will add a focused dashboard commute card. It will send the origin coordinates from `DashboardLocationService`, render the backend-owned commute state, and format the available journey for at-a-glance use. A selected transit adapter will remain behind the application port; provider selection is an implementation-planning gate based on coverage, arrival-by routing, line/direction data, and optional platform data. The provider’s default route selection will be used rather than introducing user-configurable route preferences.

The commute endpoint will distinguish a hidden/non-applicable commute from a visible but unavailable route and from an available journey. The card will remain hidden outside the configured local-time window and on work-from-home or non-working days, show `No public transport route available` when no route arrives by the target, and omit the platform phrase when the provider does not supply one. Periodic route refreshes and disruption handling remain outside this story.

## Implementation steps

1. **Align the SCH-004 and SCH-005 application contracts**
   - Change: Confirm that the resolved office-day contract provides an application-owned destination with coordinates and label to the commute use case, without exposing persistence records through HTTP. Extend the internal contract if SCH-004 currently exposes only the dashboard label. Extend SCH-005’s persisted preference contract with the commute-visibility cutoff, defaulting to 12:00 local time, and keep the three existing timing preferences and target-arrival calculation as the single source of truth. Update the SCH-005 story/plan before implementation if required by that contract change.
   - Files: `../../../backend/src/main/java/nl/codestar/myvibedapp/schedule/application`, `backend/src/main/java/nl/codestar/myvibedapp/schedule/domain/`, `frontend/src/app/shared/`, `docs/features/schedule/SCH-005-configure-commute-preferences.md`, `docs/plans/SCH-005-configure-commute-timing-preferences.md`

2. **Define the commute domain and application boundary**
   - Change: Add immutable application/domain values for a commute origin, office destination, transit journey, transit leg, service/line, direction, departure and arrival times, walking duration to the first stop, and an optional platform. Define a `TransitProvider` application port that accepts the origin, destination, local target-arrival time, and date and returns the provider’s default journey or absence. Add an application service that resolves today through SCH-004, applies the visibility cutoff and office-day rule, requests a journey, rejects journeys arriving after the target, and uses SCH-005’s timing calculation for exact leave-home and wake-up values plus the 15-minute display floor.
   - Files: `../../../backend/src/main/java/nl/codestar/myvibedapp/schedule/domain`, `backend/src/main/java/nl/codestar/myvibedapp/schedule/application/`, `backend/src/test/java/nl/codestar/myvibedapp/schedule/`

3. **Select and integrate the public-transit provider**
   - Change: Choose the first provider during implementation from the documented capabilities: Amsterdam/public-transit coverage, arrival-by routing, provider-default route selection, service/line and direction data, transfer legs, walking duration to the first stop, and platform data when available. Configure its base URL, finite HTTP timeouts, and any credential outside source control. Implement the adapter under the schedule feature’s outbound boundary, validate the provider response immediately after deserialization, map provider-specific journey data to the application-owned transit values, preserve an absent platform as absent, and translate timeout, transport, invalid-response, and no-route results to the application’s unavailable/absent outcomes.
   - Files: `../../../backend/src/main/java/nl/codestar/myvibedapp/schedule/adapters/out`, `backend/src/main/resources/application.properties`, provider adapter tests under `backend/src/test/java/nl/codestar/myvibedapp/schedule/adapters/out/`

4. **Expose the today-commute HTTP contract**
   - Change: Add a validated `GET /api/work-schedule/commute/today` endpoint accepting the origin latitude and longitude from the shared dashboard location. Derive today and local times with the configured schedule timezone and injected `Clock`. Return application-owned states for hidden/non-applicable, visible-but-unavailable, and available journeys; for an available journey include the date, wake-up display time, leave-home time, planned arrival time, and ordered transit legs with line/service, direction, departure/arrival, transfer position, and optional platform. Return the exact user-facing unavailable message without provider details and keep persistence/provider DTOs out of the response.
   - Files: `../../../backend/src/main/java/nl/codestar/myvibedapp/schedule/adapters/in/web`, `backend/src/main/java/nl/codestar/myvibedapp/schedule/application/`, `backend/src/test/java/nl/codestar/myvibedapp/schedule/adapters/in/web/`

5. **Add the Angular commute client and presentation model**
   - Change: Add a typed commute service that calls the relative endpoint with coordinates from `DashboardLocationService`. Add a focused commute-card component with signals for loading, hidden/non-applicable, available, and unavailable states. Render wake-up, leave-home, arrival, the first service instruction, and each transfer. Format the primary instruction as `Take [service] toward [direction] at [time] from platform [platform]` when a platform exists, and omit the platform phrase otherwise. Do not expose route-provider payloads or configuration metadata.
   - Files: `frontend/src/app/commute/`, `../../../frontend/src/app/shared/dashboard-location.service.ts`

6. **Compose and time-bound the dashboard card**
   - Change: Add the commute card to the existing dashboard composition beside the clock and weather. Extract the existing one-second dashboard time signal into a shared time service if necessary so the card hides at the configured cutoff in the dashboard location timezone without creating a second timer. Reload only on a local calendar-date transition when needed to replace yesterday’s journey with today’s journey; periodic route refresh remains deferred to SCH-009. Preserve tablet-first layout, semantic headings, readable time hierarchy, and WCAG AA accessibility.
   - Files: `../../../frontend/src/app/dashboard/dashboard.ts`, `frontend/src/app/dashboard/dashboard.spec.ts`, `frontend/src/app/clock/clock.ts`, `frontend/src/app/shared/`, `frontend/src/app/commute/`

7. **Document the commute boundary and provider decision**
   - Change: Record the application-owned transit-provider boundary, the shared dashboard location as the sole commute origin, the SCH-004 destination contract, provider-default route selection, optional platform handling, target-arrival rule, and the absence of polling/caching/disruption behavior in the architecture documentation. Record the selected provider and its credential/configuration boundary after the capability check.
   - Files: `../../ARCHITECTURE.md`

8. **Verify the complete behavior and update the plan status**
   - Change: Run the backend and frontend checks, search for duplicate origin sources and provider DTO leakage, map every acceptance criterion to automated evidence, and change this plan’s status to `Implemented` only after all checks pass.
   - Files: `../../../backend`, `frontend/`, `docs/plans/SCH-006-show-todays-outbound-commute.md`

## Test strategy

- **Functional coverage:**
  - Backend application/API tests verify that an office day during the visibility window produces a journey request for the shared origin and resolved office destination; the target is scheduled start minus the office-arrival lead; late journeys are rejected; the provider’s default qualifying journey is used; exact leave-home and wake-up values follow SCH-005; displayed wake-up time is floored to 15 minutes; and the response contains ordered services, directions, transfers, arrival, and optional platform.
  - Boundary tests cover local midnight, just before the configured cutoff, exactly at the cutoff, work-from-home and non-working days, invalid origin coordinates, no route, provider failure, a missing platform, and a route whose provider response is structurally incomplete. The unavailable response must be exactly `No public transport route available` and must not contain provider details.
  - The selected provider adapter is tested against a controllable local HTTP stub. Tests verify request parameters for origin, destination, target arrival, date, and provider-default routing, plus mapping of line/service, direction, transfers, walking duration, and optional platform. No live provider or credentials are used in tests.
  - Angular service/component tests verify coordinate forwarding, loading and hidden states, available journey rendering, the exact platform-aware instruction, platform omission, transfers, unavailable wording, cutoff behavior in the dashboard timezone, date rollover behavior, and accessibility. The dashboard composition test verifies commute, clock, and weather coexist without breaking existing requests.
- **Test level:** Use backend MockMvc/application integration tests with fixed clocks and user-defined fake schedule/preferences/transit ports; use focused domain tests for time-window and route-timing permutations; use a local HTTP stub for the provider adapter; and use Angular component/service tests with HTTP testing support. Do not use Mockito or another mocking framework. No end-to-end test is required for this first provider-backed card unless the existing frontend setup supports a stable journey.
- **TDD sequence:** Add the API/application-boundary test for each vertical slice first, run it to confirm the expected missing-behavior failure, implement the smallest behavior, run it green, then refactor without changing the public contract.
- **Commands:**
  - `cd backend && ./mvnw verify`
  - `cd frontend && npm run check`

## Technical decisions

- Keep SCH-006 provider-agnostic at the product/API boundary. Select one provider during implementation planning and isolate it behind `TransitProvider`; provider-specific request/response models never cross into the domain or frontend.
- Use the schedule feature’s configured `Europe/Amsterdam` timezone and an injected `java.time.Clock` for date and cutoff decisions. Serialize calendar dates and local times explicitly rather than relying on the machine timezone.
- Keep the shared dashboard location as the only commute origin. The frontend sends its coordinates from `DashboardLocationService`; the resolved office destination comes from the schedule application contract and is not re-entered in the commute card.
- Ask the provider for its default route that can arrive by the target time. The application still verifies the returned arrival time, and it does not add a user-configurable route ranking or transport preference.
- Treat the office-arrival lead as a target-arrival adjustment, and reuse SCH-005 for first-leg walking/access-buffer and wake-up calculations. Do not duplicate those calculations in the controller or Angular code.
- Represent platform as optional provider data. Include `from platform [platform]` only when present; never display a placeholder or invent a platform.
- Map no-route, provider failure, timeout, and unusable provider payloads to the same user-safe unavailable state for this story. Do not add retries, caching, periodic polling, disruption handling, or stale-data retention; those behaviors belong to later stories.
- Keep the commute dashboard dependent on one application-owned read contract and show only the resolved journey, not schedule rules, preferences, or provider metadata.
- Follow the existing feature-oriented Spring boundaries, JSpecify/NullAway checks, immutable records, functional-core/imperative-shell style, Angular signals, relative `/api` requests, and user-defined test doubles.
