# Implementation plan: [KEY]-NNN — [story]

Related story: [KEY-NNN — story title](../features/[feature]/[KEY]-NNN-short-title.md)
Status: Draft

## Approach

Describe the proposed solution, the boundaries it affects, and the main technical decisions. Explain meaningful trade-offs rather than repeating the ticket.

## Implementation steps

List the work in execution order. Each step should describe a concrete change and where it will be made.

1. **[Short step name]**
   - Change: `[concrete implementation work]`
   - Files: `[expected paths]`
2. **[Short step name]**
   - Change: `[concrete implementation work]`
   - Files: `[expected paths]`

## Test strategy

Describe how the feature's behavior will be verified automatically. Prefer a small number of integration or component tests that exercise complete functionality through stable public boundaries. Tests should ideally remain valid when internal implementation details are refactored. Add unit tests only for complex logic with meaningful input permutations.

- **Functional coverage:** `[behaviors and acceptance criteria covered]`
- **Test level:** `[integration, component, end-to-end, or justified unit test]`
- **Commands:** `[exact automated test, lint, and build commands]`

## Technical decisions

- Decision or trade-off specific to this feature
- Lasting system-wide decisions must also be recorded in `docs/ARCHITECTURE.md`.
