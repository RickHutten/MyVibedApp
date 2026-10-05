# SCH-004: Resolve upcoming work locations

| Field | Value |
|---|---|
| Status | Draft |
| Priority | To be decided |

## Description

As a user, I want the dashboard to resolve my schedule automatically so that I can see where I need to be without interpreting rules myself.

## Scope

- **In scope:**
  - Today’s resolved work status and location
  - The next working day’s resolved work status and location
  - Automatic application of default, recurring, and one-off schedule information
- **Out of scope:**
  - Commute calculations
  - Calendar integration

## Acceptance criteria

- [ ] The dashboard shows whether today is an office day, work-from-home day, or non-working day.
- [ ] The dashboard shows the applicable office location when today is an office day.
- [ ] The dashboard shows the same information for the next working day.
- [ ] The user does not need to resolve overlapping schedule rules manually.

## Dependencies

- **Blocked by:** [SCH-003](./SCH-003-configure-one-off-schedule-overrides.md)
- **Related:** None

## Notes

- More detailed rule-resolution behavior will be refined later.
