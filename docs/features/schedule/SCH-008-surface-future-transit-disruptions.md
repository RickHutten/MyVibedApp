# SCH-008: Surface future public-transport disruptions

| Field | Value |
|---|---|
| Status | Draft |
| Priority | Medium |

## Description

As a user, I want important disruptions to be clearly visible before my commute so that I can prepare or leave earlier.

## Scope

- **In scope:**
  - Disruptions affecting today’s outbound commute
  - Disruptions affecting the next working day’s outbound commute
  - Clear, prominent disruption information
  - Suggested impact on departure time when available
- **Out of scope:**
  - Return commute disruptions
  - External notifications

## Acceptance criteria

- [ ] The dashboard shows a clear disruption indicator when a relevant disruption is known.
- [ ] The dashboard identifies the affected route or service when available.
- [ ] The dashboard shows an updated departure recommendation when the disruption changes the journey.
- [ ] Irrelevant disruptions are not shown as commute warnings.

## Dependencies

- **Blocked by:** [SCH-006](./SCH-006-show-todays-outbound-commute.md), [SCH-007](./SCH-007-show-next-working-days-commute.md)
- **Related:** None

## Notes

- The disruption provider and exact wording will be decided later.
