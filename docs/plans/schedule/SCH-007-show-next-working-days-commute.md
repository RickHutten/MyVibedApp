# Implementation plan: SCH-007 — Show the next office day’s commute

Related story: [SCH-007 — Show the next office day’s commute](../../features/schedule/SCH-007-show-next-working-days-commute.md)
Status: Implemented

## Approach

Generalize the existing SCH-006 outbound-commute vertical slice so the backend selects the commute view for the current local time: today’s office commute before the persisted visibility cutoff, and the earliest strictly future office day at or after that cutoff. The existing application-owned endpoint, `GET /api/work-schedule/commute/today`, will be retained to avoid an unnecessary public-route migration; its response will gain the target metadata needed by the dashboard while its application model will represent either today’s or the next office day’s commute. The frontend will continue rendering one commute card, with the backend-selected result replacing today’s card after the cutoff rather than loading two cards or duplicating cutoff logic in Angular.

Reuse the existing schedule precedence resolver, persisted commute preferences, `TransitProvider`, Transitous adapter, timing arithmetic, complete itinerary model, shared dashboard origin, and PostgreSQL test support. Add only the missing office-only future-date resolution and explicit no-future-office outcome. No persistence migration is required.

## Implementation steps

1. **Add an office-only future schedule contract**
   - Change: Extend `ScheduleResolver` with an office-only search that starts strictly tomorrow, applies the existing one-off/monthly/weekly/daily/default precedence, skips work-from-home and non-working results, and returns absence when no office day exists within the existing bounded search policy. Extend `UpcomingWorkScheduleService` with an application-owned `ResolvedWorkday` lookup for that next office date, including the working-hours start and active-or-deleted office coordinates and label needed by commute planning. Keep the existing next-working-day contract used by SCH-004 unchanged.
   - Files: `backend/src/main/java/nl/codestar/myvibedapp/schedule/domain/ScheduleResolver.java`, `backend/src/main/java/nl/codestar/myvibedapp/schedule/application/UpcomingWorkScheduleService.java`, `backend/src/test/java/nl/codestar/myvibedapp/schedule/domain/ScheduleResolverTest.java`, schedule application tests as needed

2. **Generalize the commute application result and target selection**
   - Change: Update the existing commute application model/service so the current local time is compared with the persisted `CommutePreferences.visibilityCutoff()`. Before the cutoff, preserve SCH-006 behavior for today; at or after the cutoff, resolve the next office day and route for that date. Use the same office-arrival target, arrival-by validation, first-transit access-buffer calculation, wake-up calculation, 15-minute display floor, provider-default route selection, complete segment preservation, and safe unavailable behavior for either target. Add explicit target metadata for `TODAY` versus `NEXT_OFFICE_DAY` and an explicit `NO_UPCOMING_OFFICE_DAY` outcome. Do not ask the frontend to load preferences or perform date/route selection.
   - Files: `backend/src/main/java/nl/codestar/myvibedapp/commute/application/TodayCommute.java`, `backend/src/main/java/nl/codestar/myvibedapp/commute/application/TodayCommuteService.java`, commute application/domain package-info or supporting records as required

3. **Extend the existing HTTP response without leaking provider types**
   - Change: Keep `GET /api/work-schedule/commute/today?latitude={lat}&longitude={lon}` as the compatibility boundary, but map the generalized application result and new target/outcome values through the existing response mapper. Include the selected date, target kind, office, timing summary, and complete ordered itinerary for available results. Return no route details for unavailable results; return the exact no-route message through the existing frontend state; expose a distinct machine-readable no-future-office outcome so the frontend can render `No upcoming office day scheduled`.
   - Files: `backend/src/main/java/nl/codestar/myvibedapp/commute/adapters/in/web/TodayCommuteResponse.java`, `backend/src/main/java/nl/codestar/myvibedapp/commute/adapters/in/web/TodayCommuteController.java`, `backend/src/test/java/nl/codestar/myvibedapp/commute/adapters/in/web/TodayCommuteControllerTest.java`

4. **Add backend behavior coverage first, then implement the vertical slice**
   - Change: Add fixed-clock HTTP/application scenarios for an office day before the cutoff, the exact cutoff boundary selecting the next office day, after-cutoff selection of the earliest strictly future office day, skipping future work-from-home and non-working dates, no future office day, next-office route target calculation, late/no-route results, invalid origin handling, complete walking/transit/transfer/final-walk preservation, and omission of optional provider fields. Assert the transit request date and target arrival, response status/target/date, no provider call when no office day exists, and absence of route details in fallback states. Reuse the existing user-defined controllable transit provider, Testcontainers PostgreSQL, `ScheduleScenario`, and fixed schedule clock; do not add Mockito or a new persistence substitute. Update existing cutoff assertions so SCH-006 remains visible before the cutoff and SCH-007 owns the post-cutoff result.
   - Files: `backend/src/test/java/nl/codestar/myvibedapp/schedule/domain/ScheduleResolverTest.java`, `backend/src/test/java/nl/codestar/myvibedapp/commute/adapters/in/web/TodayCommuteControllerTest.java`, related commute test support only if a current public-boundary fixture requires it

5. **Generalize the Angular commute card to render the selected target**
   - Change: Extend the typed commute client model for the target and no-future-office outcome while keeping the existing relative endpoint and shared `DashboardLocationService` coordinates. Update the existing commute component/template in place so one card renders either `Today’s commute` or `Next office day`, shows the next office day’s timezone-aware weekday and calendar date plus destination office, and displays wake-up, leave-home, arrival, duration, transfer count, and the same complete itinerary with optional detail omission. Keep loading and request-error behavior consistent with SCH-006, render `No public transport route available` for an unavailable route, and render `No upcoming office day scheduled` for the new empty state. Update the cutoff preference hint from hiding today’s commute to explaining that it switches the dashboard to the next office-day preview. Preserve semantic markup and accessibility.
   - Files: `frontend/src/app/commute/today-commute.service.ts`, `frontend/src/app/commute/today-commute.ts`, `frontend/src/app/commute/today-commute.html`, `frontend/src/app/commute/commute-preferences.html`, `frontend/src/app/commute/today-commute.spec.ts`, commute preference tests as needed

6. **Keep dashboard composition as one replacing commute surface**
   - Change: Keep a single commute component in the dashboard rail; do not add a second next-day card or a frontend timer that duplicates the backend cutoff. Update dashboard HTTP fixtures and DOM assertions for the generalized response and verify that the rail still contains one commute surface alongside the existing schedule card. Reconcile affected coordinate fixtures with the current `DashboardLocationService` values before relying on the full frontend suite, without changing the shared location as part of this story.
   - Files: `frontend/src/app/dashboard/dashboard.ts`, `frontend/src/app/dashboard/dashboard.spec.ts`, `frontend/src/app/commute/today-commute.spec.ts`

7. **Document the commute read-boundary decision**
   - Change: Update the architecture documentation to state that the existing commute read boundary selects today before the persisted cutoff and the next strictly future office day at/after the cutoff, skips work-from-home and non-working days for the preview, and retains the shared origin, schedule-owned office destination, provider-default route selection, complete itinerary, and no-route/no-future safe states. Do not add a migration or a second persisted schedule/commute model.
   - Files: `docs/ARCHITECTURE.md`

8. **Verify all acceptance criteria and update the plan status**
   - Change: Map every story criterion to backend API or Angular component evidence, run the full backend and frontend checks, inspect the final diff for endpoint/DTO/provider leakage and stale today-only copy, and change this plan’s status to `Implemented` only after review fixes and all checks pass.
   - Files: `backend/`, `frontend/`, `docs/plans/schedule/SCH-007-show-next-working-days-commute.md`

## Test strategy

- **Functional coverage:**
  - Backend tests verify that before the configured cutoff the existing today-office commute remains available, while at and after the cutoff the same read boundary selects the earliest strictly future office day and its office coordinates.
  - Backend tests verify strict-future date selection, skipping work-from-home and non-working days, recurrence/override precedence, no future office day, correct next-office target arrival, late-route rejection, no-route behavior, invalid origin behavior, and no provider request when no office day exists.
  - Backend response tests verify target/date/office/timing metadata, the complete ordered itinerary including initial, transfer, and final walking, optional mode/location/platform omission, `NO_UPCOMING_OFFICE_DAY`, and the existing unavailable-route contract without provider details.
  - Angular component tests verify the shared coordinates, one replacing commute card, today and next-office headings, timezone-aware weekday/date, office and timing summary, duration and transfer count, complete itinerary rendering, optional-field omission, no-route wording, no-future-office wording, loading/error state, and updated cutoff preference copy. Dashboard tests verify only one commute component/request is composed.
- **Test level:** Use backend domain tests for office-only date search and HTTP integration tests through MockMvc against PostgreSQL Testcontainers with fixed clocks and a user-defined transit fake. Reuse the existing Transitous adapter tests because provider parsing and itinerary mapping are unchanged. Use Angular HTTP/component tests at the rendered DOM boundary and update affected shared-location fixtures. Do not use Mockito or another mocking framework. No end-to-end test is required because the existing public API/component tests cover the new state selection.
- **TDD sequence:** Add the missing office-only resolver and post-cutoff public-boundary tests first, run them to confirm the expected missing-behavior failures, implement the smallest backend contract, then update the Angular boundary tests and component. Refactor only after the behavior tests are green.
- **Commands:**
  - `cd backend && ./mvnw verify`
  - `cd frontend && npm run check`
  - `git diff --check`

## Technical decisions

- Preserve the existing `/api/work-schedule/commute/today` read boundary to avoid an unnecessary route migration for the current client, while making the application result explicitly target either today or the next office day. The endpoint remains application-owned; provider models never cross it.
- Treat the configured cutoff as the handoff boundary: today’s view is selected strictly before it, and the next-office preview is selected at or after it, preventing a gap when SCH-006 stops being visible.
- Define the preview target as the earliest strictly future date resolved as `OFFICE`; skip `WORK_FROM_HOME` and `NON_WORKING`, while preserving all existing schedule precedence and bounded-search behavior.
- Keep cutoff evaluation, schedule-date resolution, route selection, and timing arithmetic in the backend. The Angular card renders the selected application result and does not duplicate preference loading or timezone decisions.
- Add no migration: the persisted schedule, office, commute timing, visibility cutoff, shared origin, and provider boundaries already exist. The new no-future-office state is a read result, not stored data.
- Keep the existing complete itinerary and safe provider failure behavior from SCH-006. No polling, caching, disruption handling, route preference, return commute, or calendar integration is introduced.
- Retain the shared dashboard location as the sole origin and resolve office coordinates through the schedule application service, including existing assignments referencing soft-deleted offices.
- Follow the existing feature-oriented Spring boundaries, JSpecify/NullAway checks, immutable values, injected `Clock`, generated-jOOQ/Flyway persistence conventions, Angular signals/native control flow, and user-defined test doubles.
