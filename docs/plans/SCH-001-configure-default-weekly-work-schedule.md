# Implementation plan: SCH-001 — Configure the default weekly work schedule

Related story: [SCH-001 — Configure the default weekly work schedule](../features/schedule/SCH-001-configure-default-weekly-work-pattern.md)
Status: Implemented

## Approach

Add a backend-owned work-schedule configuration with PostgreSQL persistence through jOOQ and a small Angular settings experience. The backend will expose the application contract for reading and replacing the weekly schedule, managing saved offices, and searching address suggestions. Domain models and application services will remain independent of HTTP, generated jOOQ records, and Photon-specific response models. Implement the backend in the repository's feature-oriented, JSpecify-annotated, functional style.

Use Photon as the initial geocoding adapter behind an application-owned geocoding interface. The backend will proxy searches so the frontend does not depend on Photon directly and can later switch to a self-hosted Photon instance or another provider. When an office is saved, persist the selected address and coordinates; schedule reads will not call the geocoder.

Use a versioned PostgreSQL migration for the schedule and office data. Store soft-deleted offices rather than removing them, so existing day assignments remain valid while deleted offices are excluded from new selections.

## Implementation steps

1. **Add PostgreSQL, Flyway, and jOOQ infrastructure**
   - Change: Add the PostgreSQL runtime/test dependencies, Flyway migration support, jOOQ code generation, and JSpecify with NullAway build-time nullness checking. Configure the database connection and generation settings through external or build configuration, and add the initial versioned migration for saved offices, weekly schedule settings, and seven day assignments. Generate jOOQ sources from a PostgreSQL schema created from the Flyway migrations; generated sources are reproducible build output and are never edited manually.
   - Files: `backend/pom.xml`, `backend/src/main/resources/application.properties`, `backend/src/main/resources/db/migration/`, generated jOOQ sources

2. **Model the work-schedule domain**
   - Change: Add immutable domain types for weekly hours, day-of-week work status, saved office references, and the complete work schedule. Enforce that office days reference an office and that working hours are valid. Represent soft deletion without exposing persistence entities. Mark the feature package non-null by default with JSpecify, use explicit nullable annotations where needed, return `Optional` for genuinely absent query results, and use streams for collection transformations rather than mutable loops.
   - Files: `backend/src/main/java/nl/codestar/myvibedapp/schedule/domain/`, `backend/src/main/java/nl/codestar/myvibedapp/schedule/application/`

3. **Implement jOOQ persistence behind repositories**
   - Change: Add jOOQ-based repositories and explicit mappers for the single-user schedule and offices; do not expose generated records beyond the persistence adapter. Initialize a missing schedule with all seven days non-working and the default `09:00–17:00` hours. Preserve references to soft-deleted offices and exclude them from selectable-office queries.
   - Files: `backend/src/main/java/nl/codestar/myvibedapp/schedule/adapters/out/persistence/`, generated jOOQ sources, `backend/src/main/java/nl/codestar/myvibedapp/schedule/application/`

4. **Expose schedule and office APIs**
   - Change: Add validated `/api/work-schedule` endpoints to read and update the weekly schedule, plus endpoints to list, add, edit, and soft-delete offices. Return application-owned request/response records and clear validation errors; do not expose persistence entities.
   - Files: `backend/src/main/java/nl/codestar/myvibedapp/schedule/adapters/in/web/`

5. **Add Photon address search**
   - Change: Add a geocoding port and Photon adapter with configured base URL, timeout, result limit, and a descriptive user agent. Expose a backend `/api/offices/search` endpoint that returns normalized selectable suggestions. Do not save an office until the user selects a suggestion. Handle provider failure as an unavailable search response without leaking provider details.
   - Files: `backend/src/main/java/nl/codestar/myvibedapp/schedule/application/`, `backend/src/main/java/nl/codestar/myvibedapp/schedule/adapters/out/photon/`, `backend/src/main/java/nl/codestar/myvibedapp/schedule/adapters/in/web/`, `backend/src/main/resources/application.properties`

6. **Add the Angular work-schedule settings experience**
   - Change: Add a lazy-loaded settings route and focused components/services using Angular signals and Signal Forms. Load the backend schedule as the source of truth, edit all seven days and the shared hours, manage offices, search Photon suggestions through the backend, require a selected saved office for office days, and show loading, empty, validation, save, and provider-unavailable states.
   - Files: `frontend/src/app/work-schedule/`, `frontend/src/app/app.routes.ts`

7. **Document lasting boundaries and attribution**
   - Change: Record the persistence/migration and provider-adapter decisions in the architecture documentation, including that office search is backend-mediated and Photon/OpenStreetMap attribution and provider-switching requirements must be respected.
   - Files: `docs/ARCHITECTURE.md`, `docs/features/schedule/SCH-001-configure-default-weekly-work-pattern.md`

8. **Create reusable integration-test scenarios**
   - Change: Add test-only fixture definitions, one composable scenario builder, a scenario persister, and a database cleaner. Fixtures prepare valid sub-entity data; the persister owns ordering, stable fixture-key resolution, relationships, and writes through test-only jOOQ support. Provide named presets and flat overrides such as `standard()`, `withOffice(...)`, `withDay(...)`, and `withHours(...)`; do not create recursively nested builders. Clear application-owned tables before each test. Keep assertions at public application boundaries; use direct persistence setup only as test arrangement, not as the behavior under test.
   - Files: `backend/src/test/java/nl/codestar/myvibedapp/support/scenario/`, `backend/src/test/java/nl/codestar/myvibedapp/support/persistence/`, backend integration tests using the builders

## Test strategy

- **Functional coverage:** Verify default initialization, valid weekly updates, all seven day statuses, shared working hours, office-day validation, work-from-home/non-working behavior, office add/edit/soft-delete, exclusion of deleted offices from new assignments, preservation of existing deleted-office assignments, persistence across requests, address suggestion normalization, and graceful Photon unavailability. Use the scenario builders to compose non-default persisted states for the stateful cases.
- **Test level:** Use backend HTTP integration tests through MockMvc against PostgreSQL Testcontainers, exercising the real Flyway migrations and jOOQ repositories, with a user-defined controllable HTTP stub for Photon. Add focused domain tests only for schedule validation rules. Do not use Mockito or another mocking framework; use user-defined fakes, stubs, and test adapters when a double is necessary. Include NullAway build verification for JSpecify annotations. Use Angular component/service tests at the public component and HTTP-service boundaries, including loading, validation, save, empty, and unavailable states. Add an end-to-end test only if the existing frontend test setup supports a stable settings journey.
- **Commands:** `cd backend && ./mvnw verify`; `cd frontend && npm run check`

## Technical decisions

- Use a single application-owned work schedule because the product currently targets one private user; do not introduce authentication or multi-user ownership in this story.
- Use JSpecify with non-null-by-default packages, explicit `@Nullable` annotations, and NullAway build enforcement. Treat nullness violations as build failures rather than relying only on runtime checks.
- Use a functional core and imperative shell: immutable values, pure transformations, `Optional` for valid absence, and `Stream` for collection transformations in backend domain and application code; keep HTTP, persistence, and provider calls at explicit side-effecting boundaries. Do not use `Optional` for required parameters or fields, and do not force streams where they make the code less readable.
- Use jOOQ rather than JPA for persistence. Keep generated database types and SQL construction inside the persistence adapter, while mapping explicitly to application/domain models at the boundary.
- Use Photon for initial address autocomplete because it is open source, OpenStreetMap-based, and explicitly supports search-as-you-type. Keep it behind an application interface and configurable base URL so public-service use can be replaced by a self-hosted instance later.
- Persist the selected geocoding result rather than performing geocoding during schedule reads or commute planning.
- Soft-delete offices with a separate active/deleted state. Existing assignments retain their office reference; new assignments can select active offices only.
- Use Flyway versioned migrations and PostgreSQL Testcontainers once persistence is introduced; do not substitute an in-memory database for production behavior. Treat Flyway migrations as the schema source of truth and generate jOOQ types from a PostgreSQL schema built from those migrations. Generated jOOQ sources are reproducible build output and are not edited manually. Clean application-owned tables before each integration test rather than relying on cross-request transaction rollback.
