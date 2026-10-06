# SCH-001: Configure the default weekly work schedule

| Field | Value |
|---|---|
| Status | Approved |
| Priority | High |

## Description

As a user, I want to define my normal weekly work schedule and saved office locations so that the dashboard knows where I am expected to work on each day.

## Scope

- **In scope:**
  - All seven days configured independently
  - Office, work-from-home, or non-working status for each day
  - One weekly working-hours range, initially 09:00–17:00
  - A default schedule with every day set to non-working
  - Saved offices with a user-provided label and selected location
  - Adding, editing, and soft-deleting saved offices
  - Assigning saved offices to office days
  - Backend persistence of the schedule and saved offices
- **Out of scope:**
  - Recurring exceptions
  - One-off overrides and holidays
  - Personal schedules
  - Public-transport information

## Acceptance criteria

- [x] The user can configure one working-hours range for the whole week.
- [x] The user can set each day, including Saturday and Sunday, to office, work from home, or non-working.
- [x] A new schedule starts with every day set to non-working.
- [x] An office day requires a selected saved office.
- [x] Work-from-home and non-working days do not require an office.
- [x] The user can add an office with a label and select its location from address suggestions.
- [x] The user can edit a saved office.
- [x] The user can soft-delete a saved office.
- [x] A soft-deleted office cannot be selected for new assignments.
- [x] Existing assignments to a soft-deleted office remain valid.
- [x] The user can reopen and edit the saved weekly work schedule.
- [x] The backend stores the weekly work schedule and saved offices.
- [x] The saved configuration remains available after the dashboard is refreshed or reopened.
- [x] The frontend does not treat local browser state as the source of truth.

## Dependencies

- **Blocked by:** None
- **Related:** None

## Notes

- The schedule is a work schedule, not a personal schedule.
- Office addresses are entered as free-form search text, then selected from matching dropdown suggestions.
- Soft-deleted offices remain valid for existing schedule assignments but cannot be selected for new assignments.
