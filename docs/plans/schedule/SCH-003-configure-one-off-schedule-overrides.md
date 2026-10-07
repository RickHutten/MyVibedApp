# Implementation plan: SCH-003 — Configure one-off schedule overrides

Related story: [SCH-003 — Configure one-off schedule overrides](../../features/schedule/SCH-003-configure-one-off-schedule-overrides.md)
Status: Draft

## Approach

Extend the schedule feature established by SCH-001 and SCH-002 with a persisted, application-owned one-off override model and a settings experience. A one-off override is keyed by its effective calendar date and has exactly one outcome: office at a saved office, work from home, or non-working. The date is unique, so the database and application both enforce that only one override can apply to a date.

Keep date validation and resolution in the schedule domain/application layers, independent of Spring MVC, jOOQ, and Angular. Use an injected `Clock` with the schedule timezone to make the today/future boundary deterministic in tests. One-off overrides take precedence over recurring rules and the SCH-001 default schedule; editing or deleting an override immediately changes the resolved result. Implementation is blocked until SCH-001 and SCH-002 provide the schedule persistence, saved-office contract, recurring-rule resolver, HTTP error format, and settings foundations described by their plans.

## Implementation steps

1. **Confirm the existing schedule extension points**
   - Change: Identify the SCH-001 schedule and saved-office application contracts, the SCH-002 recurring-rule model and resolver, the persistence schema, the client-facing validation/conflict format, the application clock/timezone policy, and the Angular settings route. Extend those contracts rather than introducing a parallel schedule representation.
   - Files: `../../../backend/src/main/java/nl/codestar/myvibedapp/schedule`, `backend/src/test/java/nl/codestar/myvibedapp/schedule/`, `frontend/src/app/work-schedule/`, `frontend/src/app/app.routes.ts`

2. **Model one-off overrides and date rules**
   - Change: Add immutable domain values for an override date, outcome, and optional saved-office reference. Validate that an office outcome has an active saved office when creating or changing an override, that new and edited dates are today or later, and that past overrides cannot be edited. Keep past overrides valid for listing and deletion. Use the injected clock rather than the system clock directly.
   - Files: `../../../backend/src/main/java/nl/codestar/myvibedapp/schedule/domain`, `backend/src/main/java/nl/codestar/myvibedapp/schedule/application/`

3. **Persist overrides with a versioned PostgreSQL migration**
   - Change: Add a migration for one-off overrides with a generated identifier, unique effective date, outcome, optional saved-office reference, and constraints for required fields and valid outcome values. Preserve references to soft-deleted offices for existing overrides while excluding deleted offices from new selections. Add jOOQ repository methods and explicit mappers for listing, creating, updating, and deleting overrides. Translate unique-date violations into the application conflict error rather than leaking database details.
   - Files: `../../../backend/src/main/resources/db/migration`, `backend/src/main/java/nl/codestar/myvibedapp/schedule/adapters/out/persistence/`, generated jOOQ sources

4. **Implement application use cases and resolution precedence**
   - Change: Add application operations for listing all overrides, creating an override, updating its date/outcome/office, and deleting it. Reject duplicate dates on create and on date changes without modifying the existing records. Reject edits to past overrides, allow deletion of past overrides, and make deletion fall back to the applicable recurring rule or SCH-001 default. Extend the date resolver so the one-off override wins before SCH-002 recurring precedence and the SCH-001 default.
   - Files: `../../../backend/src/main/java/nl/codestar/myvibedapp/schedule/application`, `backend/src/main/java/nl/codestar/myvibedapp/schedule/domain/`

5. **Expose validated override endpoints**
   - Change: Add application-owned request and response records and validated `/api/work-schedule/overrides` endpoints for listing, creating, updating, and deleting. Support changing an override's date during update, return a conflict response identifying the existing override when the target date is occupied, and return a clear validation response for past-date creation/editing, invalid outcomes, and unavailable offices. Include past overrides in reads without an expiry marker.
   - Files: `../../../backend/src/main/java/nl/codestar/myvibedapp/schedule/adapters/in/web`, `backend/src/main/java/nl/codestar/myvibedapp/schedule/application/`

6. **Add the one-off override settings UI**
   - Change: Extend the SCH-001 settings experience with a list and focused editor for date, outcome, and saved office. Load overrides from the backend as the source of truth; allow create and edit for today/future dates, allow deletion of any listed override, exclude soft-deleted offices from new selections, and show server validation/conflict errors without losing the current form state. Keep past overrides visible without an expiry label and disable their editing while retaining deletion. Show loading, empty, save, validation, conflict, and unavailable states consistent with the existing settings foundation.
   - Files: `../../../frontend/src/app/work-schedule`, `frontend/src/app/app.routes.ts`

7. **Document the schedule resolution boundary**
   - Change: Record the one-off persistence and resolution decisions in the architecture documentation where they affect SCH-004 and later commute features: unique date constraint, one-off-over-recurring-over-default precedence, clock-based date validation, and preservation of soft-deleted office references. Keep settings interaction details in SCH-003.
   - Files: `../../ARCHITECTURE.md`

8. **Verify the complete behavior and update plan status**
   - Change: Run the backend and frontend checks, verify each acceptance criterion against automated evidence, check that no mocking framework or credentials were introduced, and only then change this plan's status to `Implemented`.
   - Files: `../../../backend`, `frontend/`, `docs/plans/SCH-003-configure-one-off-schedule-overrides.md`

## Test strategy

- **Functional coverage:** Verify creating today/future overrides, rejecting past dates, all three outcomes, active saved-office validation, exclusion of soft-deleted offices from new selections, continued resolution through existing soft-deleted office references, unique-date rejection on create and date change, editing date/outcome/office, rejecting edits to past overrides, deleting current and past overrides, visibility of past overrides without an expiry marker, one-off-over-recurring-over-default precedence, and fallback after deletion.
- **Test level:** Use backend HTTP integration tests through MockMvc against PostgreSQL Testcontainers with real Flyway migrations and jOOQ repositories. Use a focused domain/application test for date-boundary and precedence permutations because those are pure rules with meaningful cases. Reuse the schedule scenario builders and user-defined test doubles; do not use Mockito or another mocking framework. Add Angular component/service tests at the HTTP and component boundaries for loading, empty, validation, conflict, successful CRUD, disabled editing of past overrides, and deletion. Add an end-to-end test only if the existing frontend setup supports a stable settings journey.
- **TDD sequence:** For each vertical slice, add the public-boundary test first, run it to confirm the expected failure, implement the smallest behavior, run it green, and then refactor without changing observable behavior.
- **Commands:** `cd backend && ./mvnw verify`; `cd frontend && npm run check`

## Technical decisions

- Use `LocalDate` for override dates. Schedule applicability is date-based; time-of-day does not belong in this story.
- Inject `java.time.Clock` and derive the current date using the schedule timezone so today/future validation is deterministic and does not depend on the machine's default timezone.
- Use the effective date as a database-unique business key while retaining a generated identifier for stable API and persistence references. Update operations must handle date changes atomically.
- Reject duplicate dates rather than selecting a winner. The conflict response identifies the existing override and leaves both existing records unchanged.
- Preserve past overrides in storage and settings. They remain visible without an expiry status, cannot be edited, and can be deleted.
- Preserve references to soft-deleted offices for existing overrides, matching SCH-001 and SCH-002; only active offices can be selected for new or changed office outcomes.
- Extend the application-owned resolver rather than duplicating recurrence logic. The precedence order is one-off override, monthly recurring, weekly recurring, daily recurring, then SCH-001 default.
- Use jOOQ and Flyway rather than JPA or an in-memory production substitute. Keep generated jOOQ types inside the persistence adapter and map them explicitly to domain/application values.
- Use JSpecify with non-null-by-default packages, NullAway enforcement, immutable records/values, and user-defined test doubles in accordance with the repository backend conventions.
