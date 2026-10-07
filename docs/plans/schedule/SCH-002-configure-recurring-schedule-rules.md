# Implementation plan: SCH-002 — Configure recurring schedule rules

Related story: [SCH-002 — Configure recurring schedule rules](../../features/schedule/SCH-002-configure-recurring-schedule-rules.md)
Status: Draft

## Approach

Extend the schedule feature established by SCH-001 with an application-owned recurring-rule model, persistence, HTTP endpoints, and an Angular settings editor. Keep recurrence evaluation and conflict detection in a pure domain component over `LocalDate`; keep Spring MVC, PostgreSQL/jOOQ, and Angular HTTP concerns at the feature boundaries. The existing SCH-001 default weekly schedule remains the fallback, while SCH-004 can consume the schedule feature's application-owned date-resolution contract.

Represent recurrence structure with explicit typed fields rather than provider-shaped JSON: interval and recurrence level, selected weekdays, one monthly pattern, date bounds, and one schedule outcome. This keeps validation and conflict detection deterministic and lets the persistence adapter map generated jOOQ records without leaking them into the domain.

Implementation is blocked until SCH-001's schedule and saved-office persistence/application contract is available. Reuse its database, migration, test-container, scenario-builder, and settings foundations rather than creating parallel infrastructure.

## Implementation steps

1. **Confirm the SCH-001 extension points**
   - Change: Identify the schedule application contract, saved-office reference, default-week resolution, persistence schema, HTTP error format, Angular settings route, and reusable integration-test support delivered by SCH-001. Extend those contracts rather than introducing a second schedule representation.
   - Files: `../../../backend/src/main/java/nl/codestar/myvibedapp/schedule`, `backend/src/test/java/nl/codestar/myvibedapp/schedule/`, `frontend/src/app/work-schedule/`, `frontend/src/app/app.routes.ts`

2. **Model recurrence and rule outcomes in the domain**
   - Change: Add immutable values for recurrence level (`days`, `weeks`, `months`), positive interval, selected weekdays, monthly calendar-day or weekday-occurrence pattern, required start date, optional end date, and one outcome (`office`, `work from home`, or `non-working`). Validate that weekly rules select at least one weekday, monthly rules select exactly one valid pattern, end dates are not before start dates, office outcomes reference a saved office, and monthly calendar days are valid positive calendar days. Keep a fourth occurrence distinct from a last occurrence even when they identify the same date in a particular month.
   - Files: `../../../backend/src/main/java/nl/codestar/myvibedapp/schedule/domain`, `backend/src/main/java/nl/codestar/myvibedapp/schedule/application/`

3. **Implement pure recurrence evaluation and conflict detection**
   - Change: Add a domain evaluator that determines whether a candidate `LocalDate` is an occurrence, using the start date as the cycle anchor and the inclusive end date as an upper bound. Support every _x_ days, every _x_ weeks on selected weekdays, and every _x_ months on a calendar day or first/second/third/fourth/last selected weekday; skip months that lack a configured calendar day. Add a conflict checker that rejects same-level rules when their actual occurrence dates intersect within their active date ranges, while allowing non-overlapping date ranges. Keep precedence as monthly over weekly over daily and expose the resolved result through an application-owned resolver contract for SCH-004.
   - Files: `../../../backend/src/main/java/nl/codestar/myvibedapp/schedule/domain`, `backend/src/main/java/nl/codestar/myvibedapp/schedule/application/`

4. **Persist recurring rules with a versioned PostgreSQL migration**
   - Change: Add a migration for recurring rules with a generated identifier, recurrence-specific columns, date bounds, outcome, optional saved-office reference, and constraints for required fields and valid enum values. Preserve rules after their end date; do not add automatic cleanup or an expiry state. Retain references to soft-deleted offices for existing rules while excluding deleted offices from new office selections. Add jOOQ repository methods and explicit mappers for list, create, update, delete, and conflict-check support.
   - Files: `../../../backend/src/main/resources/db/migration`, `backend/src/main/java/nl/codestar/myvibedapp/schedule/adapters/out/persistence/`, generated jOOQ sources

5. **Expose recurring-rule application endpoints**
   - Change: Add application-owned request and response records and validated `/api/work-schedule/recurring-rules` endpoints for listing, creating, editing, and deleting rules. Translate domain validation into the existing client-facing validation format and translate same-level occurrence conflicts into a conflict response that identifies the existing rule without exposing persistence details. Ensure reads include rules whose end date has passed and writes cause later resolution to use the edited or deleted rule set.
   - Files: `../../../backend/src/main/java/nl/codestar/myvibedapp/schedule/adapters/in/web`, `backend/src/main/java/nl/codestar/myvibedapp/schedule/application/`

6. **Add recurring-rule settings UI**
   - Change: Extend the lazy-loaded SCH-001 settings experience with a rule list and focused editor. Let users choose the recurrence level and interval, selected weekdays or monthly pattern, start/end dates, and one schedule outcome; require a saved office for office outcomes. Show an empty list when no rules exist, preserve ended rules in the list without adding an expiry marker, show field validation before submission, and show the server conflict when a same-level rule overlaps. Keep the dashboard free of rule configuration details.
   - Files: `../../../frontend/src/app/work-schedule`, `frontend/src/app/app.routes.ts`

7. **Document the schedule resolution boundary**
   - Change: Record the recurring-rule persistence and resolution decisions in the architecture documentation where they affect future schedule and commute features: application-owned date resolution, explicit recurrence fields, automatic monthly/weekly/daily precedence, and no manual conflict priority. Keep story-specific interaction and wording in SCH-002.
   - Files: `../../ARCHITECTURE.md`

8. **Verify the feature through public boundaries**
   - Change: Run the backend and frontend checks, search for stale references to superseded schedule contracts, and verify that no mocking framework or credentials are introduced. Confirm every acceptance criterion against automated evidence before changing this plan's status to `Implemented`.
   - Files: `../../../backend`, `frontend/`, `docs/plans/SCH-002-configure-recurring-schedule-rules.md`

## Test strategy

- **Functional coverage:** Verify CRUD behavior, recurrence anchoring, inclusive end dates, dates outside the active range, all three recurrence levels, weekly weekday selection, monthly calendar-day rules including skipped invalid months, first/second/third/fourth/last weekday occurrences, all three outcomes, saved-office validation, persistence of ended rules, same-level overlap rejection with conflict details, allowed non-overlapping ranges, monthly-over-weekly-over-daily precedence, and fallback to the SCH-001 default schedule after edit or delete.
- **Test level:** Use backend HTTP integration tests through MockMvc against PostgreSQL Testcontainers with real Flyway migrations and jOOQ repositories. Use focused domain tests for recurrence evaluation and conflict detection because those are pure logic with meaningful date permutations. Reuse user-defined fakes and scenario builders; do not use Mockito or another mocking framework. Add Angular component/service tests at the HTTP and component boundaries for loading, empty, validation, successful CRUD, and conflict-error states. Add an end-to-end test only if the existing frontend setup supports a stable settings journey.
- **TDD sequence:** For each vertical slice, add the public-boundary test first, run it to confirm the expected failure, implement the smallest behavior, run it green, and then refactor the domain or adapter without changing observable behavior.
- **Commands:** `cd backend && ./mvnw verify`; `cd frontend && npm run check`

## Technical decisions

- Use `LocalDate` for schedule dates. Recurrence is date-based; time-of-day and timezone concerns do not belong in this story.
- Treat the start date as the recurrence anchor. A candidate date must be on or after the start date and, when present, on or before the inclusive end date.
- Treat monthly calendar-day rules and monthly weekday-occurrence rules as separate patterns. A missing calendar day is skipped; a fourth occurrence remains distinct from a last occurrence.
- Define precedence by recurrence level—monthly, then weekly, then daily—rather than by interval length or creation order. Do not expose manual priority controls.
- Reject same-level conflicts based on actual occurrence-date intersection and active date ranges. Allow same-level rules whose active ranges do not overlap. Return a conflict response instead of silently choosing a winner.
- Preserve ended rules in storage and in the settings list. Editing or deleting a rule immediately exposes the next applicable recurring rule or the SCH-001 default through the resolver.
- Keep saved-office references application-owned and preserve references to soft-deleted offices for existing rules, matching SCH-001's office-assignment behavior.
- Use jOOQ and Flyway rather than JPA or an in-memory production substitute. Keep generated jOOQ types inside the persistence adapter and map them explicitly to domain/application values.
- Use JSpecify with non-null-by-default packages and NullAway enforcement, immutable records/values, and user-defined test doubles in accordance with the repository backend conventions.
