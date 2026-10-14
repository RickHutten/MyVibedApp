# SCH-006: Show today’s outbound commute

| Field | Value |
|---|---|
| Status | Approved |
| Priority | High |

## Description

As a user, I want to see the complete journey to today’s office so that I know when to wake up, when to leave home, and exactly how to get there.

The commute card is a morning view: it is available from local midnight until the configurable cutoff in the commute settings, which defaults to 12:00.

## Scope

- **In scope:**
  - Outbound commute only
  - Today’s resolved office day
  - Visibility from local midnight until the configured cutoff
  - Wake-up time
  - Leave-home time
  - Planned arrival time and destination office
  - The complete door-to-door itinerary, including walking, public-transport, transfer, and final walking segments when provided
  - Transport mode, service, direction, boarding stop, exit stop, departure time, arrival time, and platform details for each public-transport segment when provided
  - Clear transfer actions and locations
  - The complete itinerary shown by default without requiring interaction
  - A clear unavailable state when no qualifying route can be found
- **Out of scope:**
  - Return commute
  - Next-working-day commute
  - Calendar integration
  - Periodic commute refreshes
  - Public-transport disruption information

## Acceptance criteria

- [ ] During the visibility window, the dashboard shows today’s outbound commute when today’s resolved status is an office day and a qualifying public-transport route is available.
- [ ] The visibility window starts at local midnight and ends at the configurable commute-visibility cutoff; the default cutoff is 12:00, and the commute settings own that configuration.
- [ ] Outside the visibility window, the dashboard does not show today’s outbound-commute card.
- [ ] The selected route targets the scheduled working-hours start minus the configured office-arrival lead time from [SCH-005](./SCH-005-configure-commute-preferences.md).
- [ ] The selected route arrives at the office no later than the target arrival time.
- [ ] When multiple qualifying routes are available, the dashboard uses the transit provider’s default route selection.
- [ ] The dashboard shows the recommended wake-up time, leave-home time, planned arrival time, destination office, total journey duration, and number of transfers when available.
- [ ] The dashboard shows the complete selected itinerary in chronological order by default, including every walking and public-transport segment provided by the transit provider.
- [ ] Each walking segment is identified as walking and shows its departure and arrival locations and times when those locations are provided.
- [ ] Each public-transport segment shows its transport mode, service, direction, departure time and boarding stop, arrival time and exit stop, and platform when provided.
- [ ] A public-transport instruction clearly identifies the action and locations, such as `Board Metro 53 toward Gaasperplas at 07:34 at [boarding stop], platform 1; get off at [exit stop] at 07:38.`
- [ ] The dashboard labels departure and arrival locations so that a time range is never shown without explaining what happens during that interval.
- [ ] Transfer walking or transfer actions are shown between public-transport segments when provided, including the transfer location and duration when available.
- [ ] The final walking segment to the destination office is shown when provided.
- [ ] When the provider does not provide a mode, stop, or platform, the dashboard omits that detail rather than showing an unknown, undefined, or placeholder value; it does not invent a value.
- [ ] The dashboard shows no commute information when today’s resolved status is work from home or non-working.
- [ ] When no route can arrive by the target arrival time, the dashboard omits commute times and route details and shows `No public transport route available`.

## Dependencies

- **Blocked by:** [SCH-004](./SCH-004-resolve-upcoming-work-locations.md), [SCH-005](./SCH-005-configure-commute-preferences.md)
- **Related:** None

## Notes

- The visibility cutoff is a commute preference, not a fixed rule in the dashboard; SCH-005 defines its default and persistence.
- Wake-up display rounding and leave-home calculation follow [SCH-005](./SCH-005-configure-commute-preferences.md).
- The dashboard consumes the resolved office location from [SCH-004](./SCH-004-resolve-upcoming-work-locations.md); it does not ask the user to select a location while viewing the commute.
- The route provider’s default selection is used; this story does not add a user-configurable transport or route preference.
- The complete itinerary is visible by default because the user needs to understand each action, not only the departure and arrival times.
- The application-owned route model must preserve all provider-supplied walking and public-transport segments. It must not reduce the journey to public-transport legs or retain only the initial walking duration.
- Transport mode and boarding/exit stop names are provider data translated into user-facing route details; provider response models must not cross the application or HTTP boundary.
