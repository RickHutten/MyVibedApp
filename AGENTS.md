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

## Completion criteria

Before calling work complete:

1. Run the relevant tests, linters, and build.
2. Report the commands run and their actual results.
3. Update product or architecture documentation if the change introduced a lasting decision.
4. Mention any unresolved risks or follow-up work.
