# Agent Guidelines

## Project purpose

Build a personal ambient dashboard that presents the information most useful to the user right now. Read `docs/PRODUCT.md` before making product decisions and `docs/ARCHITECTURE.md` before changing system boundaries or infrastructure.

## Repository structure

- `backend/` — Spring Boot backend, built with Maven and Java 25
- `frontend/` — Angular frontend (to be created)
- `docs/` — product and architecture documentation

Add a more specific `AGENTS.md` inside `backend/` or `frontend/` only when that area needs rules that do not apply to the whole repository. The nearest `AGENTS.md` takes precedence.

## Working principles

- Inspect existing code and configuration before changing it.
- Keep changes focused on the requested task; avoid unrelated refactoring.
- Do not invent requirements. Record unresolved product questions in `docs/PRODUCT.md`.
- Record significant architectural decisions and trade-offs in `docs/ARCHITECTURE.md`.
- Never commit, push, or rewrite Git history unless explicitly asked.
- Never store credentials, tokens, personal data, or local `.env` files in Git.
- Prefer small, reviewable changes with tests.
- Fix root causes rather than suppressing errors.

## Backend conventions

- Use Java 25 and Spring Boot.
- Keep domain logic separate from HTTP controllers and infrastructure adapters.
- Validate input at system boundaries.
- Use PostgreSQL for persistent application data.
- Manage schema changes with versioned database migrations once persistence is introduced.
- Add automated tests for behavior changes.

Run backend verification from `backend/`:

```sh
./mvnw test
```

Until the Maven wrapper is added, use:

```sh
mvn test
```

## Frontend conventions

- Use Angular with TypeScript strict mode.
- Keep components focused on presentation and interaction; place API and state concerns in services or dedicated state layers.
- Design for an always-on tablet display first, while retaining normal browser usability.
- Include loading, empty, offline, and error states for external data.
- Add tests for behavior changes.

Document exact frontend commands here after the Angular workspace is created.

## Completion criteria

Before calling work complete:

1. Run the relevant tests, linters, and build.
2. Report the commands run and their actual results.
3. Update product or architecture documentation if the change introduced a lasting decision.
4. Mention any unresolved risks or follow-up work.
