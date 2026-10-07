# Implementation plan: SCH-004 — Resolve upcoming work locations

Related story: [SCH-004 — Resolve upcoming work locations](../../features/schedule/SCH-004-resolve-upcoming-work-locations.md)
Status: Draft

## Approach

Extend the schedule application boundary created by SCH-001 through SCH-003 with a read-only resolved-schedule use case. The backend will use the configured schedule timezone and an injected `Clock` to resolve today and find the earliest strictly future date whose resolved outcome is office or work from home. It will reuse the existing one-off, recurring-rule, and default-schedule precedence rather than duplicating schedule logic or exposing configuration rules to the dashboard.

Expose the resolved result through an application-owned `GET /api/work-schedule/upcoming` response. The response will contain today’s ISO calendar date and resolved outcome, with the saved office label when applicable, plus an optional next-working-day result. The frontend will format the returned next-working-day date as a weekday and calendar date and translate outcomes into the approved labels: `Working from [office label]`, `Working from home`, `Day off`, and `No upcoming work scheduled`.

Keep the feature read-only: no migration, persistence model, schedule-settings route, or commute integration is introduced by SCH-004. The implementation remains blocked until SCH-003 provides the persisted schedule data, saved-office references, one-off precedence, recurring resolver, and shared Angular settings/application foundations.

## Implementation steps

1. **Confirm the existing schedule resolution extension points**
   - Change: Inspect the SCH-001–003 schedule domain, application contracts, persistence mappers, configured schedule timezone, injected `Clock`, HTTP error format, scenario builders, and Angular application structure. Define the smallest application-owned contract needed by the dashboard, including a resolved day value and an optional next-working-day value.
   - Files: `../../../backend/src/main/java/nl/codestar/myvibedapp/schedule`, `backend/src/test/java/nl/codestar/myvibedapp/schedule/`, `frontend/src/app/work-schedule/`, `frontend/src/app/app.routes.ts`

2. **Implement resolved-date selection in the schedule application/domain layer**
   - Change: Add or extend a pure resolver that resolves an individual `LocalDate` through one-off override, recurring-rule precedence, and the default weekly schedule. Add a next-working-day operation that starts at tomorrow, skips resolved non-working dates, returns the earliest office or work-from-home result, and returns absence when no future working date is produced by the configured rules. Preserve assigned office labels, including existing assignments that reference soft-deleted offices.
   - Files: `../../../backend/src/main/java/nl/codestar/myvibedapp/schedule/domain`, `backend/src/main/java/nl/codestar/myvibedapp/schedule/application/`

3. **Expose the resolved schedule endpoint**
   - Change: Add application-owned response records and a thin controller for `GET /api/work-schedule/upcoming`. Derive today from the schedule timezone rather than the machine default timezone. Serialize dates as ISO calendar dates, outcomes as stable application values, and the office label only for office outcomes; represent no upcoming working day explicitly as an absent optional result. Keep persistence and generated jOOQ types out of the HTTP contract.
   - Files: `../../../backend/src/main/java/nl/codestar/myvibedapp/schedule/adapters/in/web`, `backend/src/main/java/nl/codestar/myvibedapp/schedule/application/`

4. **Add the frontend resolved-schedule client and presentation component**
   - Change: Add a typed Angular service and focused dashboard component that loads `/api/work-schedule/upcoming` as the source of truth. Render separate Today and Next working day sections, format the next date with the dashboard location’s IANA timezone and weekday/date output, and map outcomes to the approved user-facing wording. Render `No upcoming work scheduled` when the response has no next-working-day value. Include loading and unavailable states without exposing backend details.
   - Files: `../../../frontend/src/app/work-schedule`, `frontend/src/app/shared/`, `frontend/src/app/dashboard/`

5. **Compose the schedule result into the dashboard**
   - Change: Add the resolved-schedule component to the existing dashboard composition without making settings or rule metadata visible there. Preserve the tablet-first responsive layout and ensure the schedule card remains readable alongside the clock and weather card.
   - Files: `../../../frontend/src/app/dashboard/dashboard.ts`, schedule component template/styles

6. **Document the resolved-schedule boundary**
   - Change: Record the application-owned resolved-schedule read boundary, schedule-timezone date derivation, and separation between schedule configuration and dashboard output if these decisions are not already documented by SCH-001–003. Keep the approved user-facing wording and story-specific behavior in SCH-004.
   - Files: `../../ARCHITECTURE.md`

7. **Verify the complete behavior and update the plan status**
   - Change: Run backend and frontend verification, check for stale or duplicate schedule-resolution contracts, and map every acceptance criterion to automated evidence. Only after all checks pass, change this plan’s status to `Implemented`.
   - Files: `../../../backend`, `frontend/`, `docs/plans/SCH-004-resolve-upcoming-work-locations.md`

## Test strategy

- **Functional coverage:** Verify through the backend API that today is always returned; office days include the saved office label; work-from-home and non-working outcomes resolve correctly; the next result is strictly after today; non-working dates are skipped; the next result includes its ISO date and resolved outcome; one-off, recurring, and default layers resolve in the established precedence order; and no future working result produces an absent next value. Cover schedule-timezone date boundaries with a fixed injected clock. Include existing assignments that reference a soft-deleted office where the resolved office label must remain available.
- **Test level:** Use backend HTTP integration tests through MockMvc against the real schedule persistence and PostgreSQL/Testcontainers support established by SCH-001–003. Use focused domain/application tests for date selection, precedence, strict-future behavior, and no-result cases because these are pure rules with meaningful permutations. Use Angular service/component tests at the HTTP and rendered dashboard boundaries for loading, today/next rendering, all approved labels, no-upcoming output, unavailable state, and accessibility. Do not use Mockito or another mocking framework; use scenario builders, user-defined fakes, and HTTP testing support.
- **TDD sequence:** For each vertical slice, add the public-boundary test first, run it to confirm the expected missing-behavior failure, implement the smallest behavior, run it green, and then refactor without changing observable behavior.
- **Commands:** `cd backend && ./mvnw verify`; `cd frontend && npm run check`

## Technical decisions

- Use `LocalDate` for schedule applicability and an injected `java.time.Clock` with `Europe/Amsterdam` for deriving today. The result is date-based; working hours and commute calculations remain outside this story.
- Reuse the SCH-003 application-owned resolver and extend it with a next-working-day query rather than duplicating recurrence or precedence logic in the controller or frontend.
- Define next working day as the earliest date strictly after today whose resolved status is office or work from home. A future work-from-home date qualifies; a non-working date never qualifies.
- Return raw outcome values and structured date/office data from the backend, then format weekday/date and approved human wording in Angular. This keeps locale/presentation concerns out of the schedule domain while keeping the API stable.
- Represent no upcoming working day as an explicit absent optional result rather than inventing a date or returning a synthetic schedule entry.
- Keep the dashboard dependent on one schedule-read service; schedule configuration remains in settings and the dashboard does not display rule metadata.
- Follow the existing feature-oriented backend boundaries, JSpecify/NullAway rules, immutable records, functional core/imperative shell, jOOQ/Flyway persistence conventions, and user-defined test doubles.
