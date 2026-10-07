# SCH-005: Configure commute timing preferences

| Field | Value |
|---|---|
| Status | Draft |
| Priority | High |

## Description

As a user, I want to configure how much time I need before leaving for work and how early I want to arrive so that the dashboard gives me realistic commute and wake-up recommendations.

The commute origin is the shared dashboard location used by the weather and clock. The user does not configure a separate home location for commute planning.

## Scope

- **In scope:**
  - Reusing the shared dashboard location coordinates as the commute origin
  - A configurable wake-up lead time in whole minutes
  - A default wake-up lead time of 45 minutes before the recommended leave-home time
  - A configurable transit-access buffer in whole minutes
  - A default transit-access buffer of 5 minutes
  - A configurable office-arrival lead time in whole minutes
  - A default office-arrival lead time of 5 minutes before the scheduled working-hours start
  - Applying the transit-access buffer only before the first bus, tram, or train departure
  - Rounding the calculated wake-up time down to the nearest 15-minute boundary for display
  - Persisting the commute timing preferences
- **Out of scope:**
  - Configuring home or office locations
  - Return commute planning
  - Calendar integration
  - Transport-provider or route preferences

## Acceptance criteria

- [ ] The user can configure the wake-up lead time in whole minutes.
- [ ] The default wake-up lead time is 45 minutes.
- [ ] The user can configure the transit-access buffer in whole minutes.
- [ ] The default transit-access buffer is 5 minutes.
- [ ] The user can configure the office-arrival lead time in whole minutes.
- [ ] The default office-arrival lead time is 5 minutes.
- [ ] All three preferences accept zero or any larger whole-minute value.
- [ ] The commute origin is the same shared dashboard location used by the weather and clock; the user is not asked to configure a separate home location.
- [ ] The target arrival time is the scheduled working-hours start minus the configured office-arrival lead time; for example, a 09:00 start and a 5-minute lead produce an 08:55 target arrival.
- [ ] The recommended route arrives at the office no later than the target arrival time.
- [ ] The recommended leave-home time is the selected first transit departure minus the route's walking time to the first stop and the configured transit-access buffer.
- [ ] The transit-access buffer is not added to transfers or later transit legs.
- [ ] The recommended wake-up time is the exact recommended leave-home time minus the configured wake-up lead time.
- [ ] The displayed wake-up time is rounded down to the nearest 15-minute boundary; for example, 08:07 is displayed as 08:00 and 08:45 remains 08:45.
- [ ] The settings remain available after the dashboard is refreshed or reopened.

## Dependencies

- **Blocked by:** None
- **Related:** [WEA-001](../weather/WEA-001-current-weather.md), [CLK-001](../clock/CLK-001-show-current-time-and-date.md)

## Notes

- Saved office locations remain part of [SCH-001](./SCH-001-configure-default-weekly-work-pattern.md).
- For example, a 09:00 first tram with a 3-minute walk from the shared dashboard location and a 5-minute buffer produces a recommended leave-home time of 08:52. With a 45-minute wake-up lead, the exact wake-up time is 08:07 and the dashboard displays 08:00.
- For a 09:00 working-hours start and a 5-minute office-arrival lead, the route planner targets arrival by 08:55 rather than by 09:00.
