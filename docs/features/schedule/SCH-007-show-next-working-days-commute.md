# SCH-007: Show the next office day’s commute

| Field | Value |
|---|---|
| Status | Approved |
| Priority | Medium |

## Description

As a user, I want to see my next office day’s outbound commute in advance so that I know which day it is, when to wake up, when to leave home, and how to get there.

After the configured commute-visibility cutoff, this preview replaces today’s outbound-commute card for the remainder of the local day.

## Scope

- **In scope:**
  - The next office day’s weekday and calendar date
  - The next office day’s resolved office location
  - Wake-up time
  - Leave-home time
  - Planned arrival time
  - The complete door-to-door public-transport itinerary, including walking, transfers, stops, and platforms when provided
  - Display after the persisted commute-visibility cutoff from [SCH-005](./SCH-005-configure-commute-preferences.md)
- **Out of scope:**
  - Return commute
  - Work-from-home and non-working days as commute targets
  - Calendar integration

## Acceptance criteria

- [ ] After the persisted commute-visibility cutoff, the dashboard replaces today’s outbound-commute card with the next office day’s commute preview until the end of the local day.
- [ ] The next office day is the earliest strictly future date whose resolved schedule status is an office day; work-from-home and non-working days are skipped.
- [ ] The dashboard shows the next office day’s weekday, calendar date, and resolved office location.
- [ ] The dashboard shows the recommended wake-up time, leave-home time, planned arrival time, and destination office.
- [ ] The dashboard shows the complete selected itinerary in chronological order by default, including every walking and public-transport segment provided by the transit provider.
- [ ] Each walking segment is identified as walking and shows its departure and arrival locations and times when those locations are provided.
- [ ] Each public-transport segment shows its transport mode, service, direction, departure time and boarding stop, arrival time and exit stop, and platform when provided.
- [ ] Transfer walking or transfer actions are shown between public-transport segments when provided, including the transfer location and duration when available.
- [ ] When the provider does not provide a mode, stop, or platform, the dashboard omits that detail rather than showing an unknown, undefined, or placeholder value; it does not invent a value.
- [ ] The selected route targets the next office day’s scheduled working-hours start minus the configured office-arrival lead time from [SCH-005](./SCH-005-configure-commute-preferences.md).
- [ ] The selected route arrives at the office no later than the target arrival time.
- [ ] When multiple qualifying routes are available, the dashboard uses the transit provider’s default route selection.
- [ ] When no route can arrive by the target arrival time, the dashboard omits commute times and route details and shows `No public transport route available`.
- [ ] When no future office day is scheduled, the dashboard shows `No upcoming office day scheduled`.

## Dependencies

- **Blocked by:** [SCH-004](./SCH-004-resolve-upcoming-work-locations.md), [SCH-005](./SCH-005-configure-commute-preferences.md)
- **Related:** [SCH-006](./SCH-006-show-todays-outbound-commute.md)

## Notes

- The visibility cutoff is read from the persisted commute preferences defined by [SCH-005](./SCH-005-configure-commute-preferences.md), not hard-coded in the dashboard.
- The next office day is selected strictly after today; work-from-home and non-working days are skipped.
- The itinerary and unavailable-route behavior match [SCH-006](./SCH-006-show-todays-outbound-commute.md), but are calculated for the next office day.
