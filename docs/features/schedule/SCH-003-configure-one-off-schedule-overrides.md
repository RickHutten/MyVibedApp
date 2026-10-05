# SCH-003: Configure one-off schedule overrides

| Field | Value |
|---|---|
| Status | Draft |
| Priority | To be decided |

## Description

As a user, I want to change my work location for a specific date so that holidays and exceptional days do not create incorrect commute plans.

## Scope

- **In scope:**
  - One-off office-day changes
  - One-off work-from-home days
  - One-off non-working days, including holidays
  - One-off alternative office locations
- **Out of scope:**
  - Calendar integration
  - Public-transport information

## Acceptance criteria

- [ ] The user can override the schedule for a specific date.
- [ ] The user can mark a specific date as a work-from-home day.
- [ ] The user can mark a specific date as a non-working day.
- [ ] The one-off override is used instead of recurring schedule rules for that date.

## Dependencies

- **Blocked by:** [SCH-002](./SCH-002-configure-recurring-schedule-rules.md)
- **Related:** None

## Notes

- The dashboard should not require a separate holiday integration for the initial version.
