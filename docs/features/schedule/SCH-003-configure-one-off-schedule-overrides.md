# SCH-003: Configure one-off schedule overrides

| Field | Value |
|---|---|
| Status | Approved |
| Priority | High |

## Description

As a user, I want to override my schedule for today or a future date so that holidays and exceptional days do not create incorrect work-location or commute plans.

## Scope

- **In scope:**
  - Creating, editing, and deleting one-off overrides in schedule settings
  - One override for a specific calendar date
  - One-off office days at a saved office
  - One-off work-from-home days
  - One-off non-working days, including holidays
  - Automatic precedence over the default schedule and recurring rules
- **Out of scope:**
  - Creating new overrides for dates in the past
  - Calendar integration
  - Public-transport information

## Acceptance criteria

- [ ] The user can create an override for today or a future date from schedule settings.
- [ ] The system rejects an override whose date is in the past.
- [ ] A date can have at most one one-off override.
- [ ] The system rejects a second override for a date and leaves the existing override unchanged.
- [ ] The user can set an override to office, work from home, or non-working.
- [ ] An office override requires selecting an active saved office from [SCH-001](./SCH-001-configure-default-weekly-work-pattern.md).
- [ ] A soft-deleted office is not available for new override selections.
- [ ] An existing override continues to use its assigned office after that office is soft-deleted.
- [ ] The user can edit an existing one-off override for today or a future date, including changing its date to another today or future date.
- [ ] The system rejects changing an override to a date that already has another override and leaves both existing overrides unchanged.
- [ ] A one-off override whose date has passed cannot be edited.
- [ ] The user can delete an existing one-off override.
- [ ] A one-off override takes precedence over every applicable recurring rule and the default weekly schedule for its date.
- [ ] After an override is deleted, the applicable recurring rule or default weekly schedule is used for that date.
- [ ] A one-off override remains visible in schedule settings after its date has passed and is not marked as expired.

## Dependencies

- **Blocked by:** [SCH-002](./SCH-002-configure-recurring-schedule-rules.md)
- **Related:** None

## Notes

- The dashboard should not require a separate holiday integration for the initial version.
- Users configure one-off overrides in schedule settings; the dashboard shows only the resolved result described by [SCH-004](./SCH-004-resolve-upcoming-work-locations.md).
- The system resolves one-off overrides automatically; the user does not choose a rule priority.
- Soft-deleted offices cannot be selected for new overrides, but existing overrides continue to use them.
