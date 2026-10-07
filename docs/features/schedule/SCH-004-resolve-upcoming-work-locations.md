# SCH-004: Resolve upcoming work locations

| Field | Value |
|---|---|
| Status | Approved |
| Priority | High |

## Description

As a user, I want the dashboard to resolve my schedule automatically so that I can see where I need to be without interpreting rules myself.

## Scope

- **In scope:**
  - Today’s resolved work status and location
  - The next strictly future working day’s resolved work status and location
  - Automatic application of default, recurring, and one-off schedule information
  - User-facing labels for office, work-from-home, and non-working days
- **Out of scope:**
  - Commute calculations
  - Calendar integration

## Acceptance criteria

- [ ] The dashboard always shows today’s resolved status, including when today is non-working.
- [ ] When today is an office day, the dashboard shows `Working from [office label]`.
- [ ] When today is a work-from-home day, the dashboard shows `Working from home`.
- [ ] When today is non-working, the dashboard shows `Day off`.
- [ ] The dashboard resolves the next working day as the earliest strictly future date whose resolved status is office or work from home, skipping non-working dates.
- [ ] The dashboard shows the next working day’s weekday and calendar date together with its resolved status.
- [ ] The next working day uses the same user-facing status and office-location labels as today.
- [ ] When no future office or work-from-home day is scheduled, the dashboard shows `No upcoming work scheduled` instead of a next-working-day result.
- [ ] The dashboard resolves each date automatically using the applicable one-off override, recurring rule, or default weekly schedule; the user does not choose between applicable rules.
- [ ] When multiple schedule layers could apply, the one-off override wins over recurring rules, recurring-rule precedence follows [SCH-002](./SCH-002-configure-recurring-schedule-rules.md), and the default weekly schedule is the fallback.

## Dependencies

- **Blocked by:** [SCH-003](./SCH-003-configure-one-off-schedule-overrides.md)
- **Related:** None

## Notes

- Users configure schedules in settings; the dashboard shows only the resolved status and location.
- The next working day is strictly after today and includes both office and work-from-home dates.
