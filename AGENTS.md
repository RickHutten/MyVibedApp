# Agent Guidelines

## Project purpose

Build a personal ambient dashboard that presents the information most useful to the user right now. Read `docs/PRODUCT.md` before making product decisions and `docs/ARCHITECTURE.md` before changing system boundaries or infrastructure.

## Repository structure

- `backend/` — Spring Boot backend, built with Maven and Java 25
- `frontend/` — Angular frontend
- `docs/` — product and architecture documentation

Add a more specific `AGENTS.md` inside `backend/` or `frontend/` only when that area needs rules that do not apply to the whole repository. The nearest `AGENTS.md` takes precedence. Don't edit any `AGENTS.md` unless you are explicitly asked to do so.

## Working principles

- Inspect existing code and configuration before changing it.
- Keep changes focused on the requested task; avoid unrelated refactoring.
- Do not invent requirements. Record unresolved product questions in `docs/PRODUCT.md`.
- Record significant architectural decisions and trade-offs in `docs/ARCHITECTURE.md`.
- Never commit, push, or rewrite Git history unless explicitly asked.
- Never store credentials, tokens, personal data, or local `.env` files in Git.
- Prefer small, reviewable changes with tests.
- Fix root causes rather than suppressing errors.

### Scope discipline for all agents

- Implement only behavior required by the approved story and exercised by a current caller or public boundary.
- Do not add speculative abstractions, future-facing integrations, unused APIs, or code for a later story.
- Do not use `@SuppressWarnings("unused")` or equivalent suppressions to hide dead code. Delete the dead code instead.
- Before adding a class, method, port, or adapter, identify its current production caller and the test that proves its behavior. If neither exists, do not add it.
- Keep future behavior in the story or implementation plan until the story that consumes it is implemented.

## Decision-making with the user

- Before making an impactful decision, ask the user which direction to take rather than choosing silently.
- Treat architecture, persistence, scope, security, and behavior changes as impactful decisions.
- Routine implementation details may follow the existing project conventions without asking.
- If an impactful decision becomes necessary during implementation, pause and ask before proceeding.

## Feature and story workflow

Group related product capabilities into feature folders under `docs/features/`. Each feature has a three-letter identifier and a `README.md` created from `docs/features/FEATURE_TEMPLATE.md`. Stories within that feature use identifiers such as `WEA-001` and are created from `docs/features/STORY_TEMPLATE.md`.

Do not start story implementation immediately.

1. Discuss and refine the story with the user.
2. Assign the feature's identifier and next story number, then write `docs/features/<feature>/<KEY>-NNN-short-title.md` from `docs/features/STORY_TEMPLATE.md`.
3. Ask the user to review and approve the story.
4. After approval, write an implementation plan from `docs/plans/TEMPLATE.md` with concrete, testable steps.
5. Ask the user to review and approve the plan.
6. Only then implement the story, using tests first for behavior changes.

After implementation and verification, set the implementation plan status to `Implemented`. Once a plan is marked `Implemented`, treat its contents as historical reference and do not edit it; record later conventions in the architecture or agent guidance instead.

Do not silently expand scope while planning or implementing. Put unresolved decisions in the story and stop for user input when they affect behavior.

Use the story identifier in related branch names, plan filenames, pull requests, and commit messages. Example: `WEA-001: add current weather endpoint`. Never create a commit unless explicitly asked.

## Backend conventions

- Use Java 25 and Spring Boot.
- Keep domain logic separate from HTTP controllers and infrastructure adapters.
- Validate input at system boundaries.
- Keep Spring MVC controllers thin: bind and validate request data, delegate to the appropriate application service, and return the service result through a small web response mapper. Controllers must not call outbound ports directly, orchestrate use cases, contain business validation, or perform multi-step provider/persistence mapping.
- Prefer Jakarta Bean Validation annotations for structural boundary validation (`@NotBlank`, `@NotNull`, `@Size`, `@DecimalMin`, `@DecimalMax`, nested `@Valid`) instead of manual checks in controllers. Keep semantic and cross-record validation in the application/domain layer. Preserve intentional valid empty inputs explicitly; do not add `@NotBlank` merely to eliminate a branch when blank has defined behavior.
- Application services own use-case orchestration and calls to application ports. Outbound adapters own provider-specific parsing and translation. Keep HTTP request/response records in the inbound web adapter package, with one top-level record per file; do not make application services depend on web DTOs.
- Register ordinary application services and concrete provider adapters with their stereotype annotations (`@Service`, `@Component`, or `@Repository`). Keep provider-specific client construction and property injection in the adapter when it has only one implementation. Use `@Configuration` and `@Bean` for shared infrastructure, conditional/qualified alternatives, or wiring that cannot be expressed cleanly on the component itself—not as a default replacement for component registration.
- Prefer type-safe `@ConfigurationProperties` records for grouped adapter settings over scattered `@Value` annotations. Register them with `@ConfigurationPropertiesScan`, inject the properties record into the owning adapter, and let that adapter construct its provider-specific client.
- Use PostgreSQL for persistent application data.
- Manage schema changes with versioned database migrations once persistence is introduced.
- Add automated tests for behavior changes.

Run backend verification from `backend/`:

```sh
./mvnw verify
```

## Frontend conventions

- Use Angular with TypeScript strict mode.
- Keep components focused on presentation and interaction; place API and state concerns in services or dedicated state layers.
- Design for an always-on tablet display first, while retaining normal browser usability.
- Include loading, empty, offline, and error states for external data.
- Add tests for behavior changes.

Run frontend verification from `frontend/`:

```sh
npm run lint
npm run format:check
npm test -- --watch=false
npm run build
```

Run all frontend checks together with `npm run check`.

## Testing conventions

- Follow the testing honeycomb: emphasize integration tests, use end-to-end tests for key user journeys, and keep unit tests selective.
- All verification must be automated; do not rely on manual checks for completion.
- Test observable functionality through stable public boundaries rather than implementation details.
- Tests must support refactoring: an internal restructuring that preserves behavior should not require test changes.
- Add unit tests when isolated logic is complex or has many meaningful input permutations; do not create a unit test for every small change.
- Prefer a small number of high-value tests over a large suite of narrow, mock-heavy or interaction-heavy tests.
- Do not use Mockito or other mocking frameworks. When test doubles are needed, write small user-defined fakes, stubs, or test adapters that model the relevant behavior.
- Prefer broader unit and integration tests over isolated interaction tests; exercise real application boundaries and use user-defined doubles only for external systems or genuinely unavailable dependencies.
- Build reusable, composable scenario builders for stateful integration-test setup. They should provide sensible defaults and focused overrides so tests describe the state they need without duplicating fixture wiring. Scenario builders persist fixture state directly through test persistence support, and each test must start from cleaned state.
- For behavior changes, write the appropriate functional test before implementation and confirm that it fails for the expected reason.

### TDD sequence

For each story behavior, follow this sequence:

1. Write only functional tests for behavior explicitly required by the story and exercised through a public boundary.
2. Run those tests and confirm they fail for the expected missing-behavior reason.
3. Implement only the code directly required to make those functional tests pass. Do not add speculative abstractions, future-facing integrations, unused APIs, or tests for implementation details.
4. Run the functional tests green.
5. Perform a separate refactor pass to improve structure, readability, reuse, and justified extensibility without adding new product behavior.
6. Only after implementation, add focused unit tests where the implemented logic is complex or has meaningful input permutations not covered by the functional tests.

Do not add a class, method, port, adapter, abstraction, or unit test unless it directly supports implemented story behavior and has a current production caller or public boundary. Keep all tests green during refactoring.

## Completion criteria

Before calling work complete:

1. Run the relevant tests, linters, and build.
2. Report the commands run and their actual results.
3. Update product or architecture documentation if the change introduced a lasting decision.
4. Mention any unresolved risks or follow-up work.
