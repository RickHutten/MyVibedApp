# SCH-006: Show today’s outbound commute

| Field | Value |
|---|---|
| Status | Approved |
| Priority | High |

## Description

As a user, I want to see how to get to today’s office so that I know when to wake up and leave home.

The commute card is a morning view: it is available from local midnight until the configurable cutoff in the commute settings, which defaults to 12:00.

## Scope

- **In scope:**
  - Outbound commute only
  - Today’s resolved office day
  - Visibility from local midnight until the configured cutoff
  - Wake-up time
  - Leave-home time
  - Planned arrival time
  - First public-transport departure, route, direction, service, and platform details when provided
  - Details for each transfer in the selected route
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
- [ ] The dashboard shows the recommended wake-up time, leave-home time, and planned arrival time.
- [ ] The dashboard shows the first departure with its line or service and direction, followed by each transfer in the selected route.
- [ ] The primary route instruction uses the form `Take [service] toward [direction] at [time] from platform [platform]` when a platform is provided, such as `Take bus 34 toward Central Station at 07:25 from platform 3.`
- [ ] When the provider does not provide a platform, the primary route instruction omits the platform rather than showing an unknown or placeholder value.
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
- Example wording: `Take bus 34 toward Central Station at 07:25 from platform 3.` When no platform is provided, omit the final platform phrase.
