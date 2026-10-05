# SCH-002: Configure recurring schedule rules

| Field | Value |
|---|---|
| Status | Draft |
| Priority | To be decided |

## Description

As a user, I want to add recurring work-location rules so that regular exceptions to my default week are handled automatically.

## Scope

- **In scope:**
  - Rules for specific weekdays
  - Monthly weekday patterns such as the first Friday
  - Rule start and end dates
  - Alternative office locations and work-from-home rules
- **Out of scope:**
  - One-off date changes
  - Calendar integration
  - Commute calculation

## Acceptance criteria

- [ ] The user can add a recurring rule for a weekday pattern.
- [ ] The user can add a recurring rule for a monthly weekday occurrence.
- [ ] The user can limit a rule to a date range.
- [ ] The dashboard applies an applicable recurring rule instead of the default schedule.

## Dependencies

- **Blocked by:** [SCH-001](./SCH-001-configure-default-weekly-work-pattern.md)
- **Related:** None

## Notes

- The system resolves rules automatically; the user does not choose a rule priority.
