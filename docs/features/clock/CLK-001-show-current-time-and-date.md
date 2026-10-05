# CLK-001: Show the current time and date

| Field | Value |
|---|---|
| Status | Approved |
| Priority | Medium |

## Description

As the dashboard user, I want to see the current time and date at a glance so that I can quickly orient myself without checking another device.

The dashboard will show a visible clock and date element using the same location as the weather widget. Initially, that location is Amsterdam and its timezone is `Europe/Amsterdam`. The time will use a 24-hour format, and the date will include a readable weekday, day, and month—for example, “15:00” and “Monday, 5 October.”

## Scope

- **In scope:**
  - A visible current-time display on the dashboard
  - A visible current-date display associated with the clock
  - 24-hour time formatting
  - A readable weekday and date format
  - The timezone from the location shared with the weather widget as the time and date source
  - Keeping the displayed time current while the dashboard is open
  - Updating the displayed date when the configured local date changes
- **Out of scope:**
  - Seconds in the displayed time
  - Alarms, timers, stopwatches, or countdowns
  - Timezone configuration or location management
  - Multiple clocks for different timezones
  - Calendar events or reminders

## Acceptance criteria

- [x] The dashboard shows the current time as a visible clock element.
- [x] The dashboard shows the current date together with the clock.
- [x] The time uses a 24-hour format, such as “15:00.”
- [x] The date includes a readable weekday, day, and month, such as “Monday, 5 October.”
- [x] The clock and weather widget use the same shared dashboard location.
- [x] The clock and date use that location’s timezone rather than the browser’s timezone.
- [x] The displayed time stays current while the dashboard is open without requiring a page reload or user interaction.
- [x] The displayed date changes when the configured local date changes at midnight.

## Dependencies

- **Blocked by:** None
- **Related:** [WEA-001](../weather/WEA-001-current-weather.md)

## Notes

- The clock should remain calm and readable as part of the always-on dashboard rather than behaving like an interactive utility.
- The shared dashboard location initially represents Amsterdam with coordinates `52.3676`, `4.9041` and timezone `Europe/Amsterdam`.
