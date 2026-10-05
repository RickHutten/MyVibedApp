# WEA-001: Show current weather

| Field | Value |
|---|---|
| Status | Approved |
| Priority | High |

## Description

As the dashboard user, I want to see the current weather for Amsterdam so that I can understand the conditions outside at a glance.

The dashboard will retrieve weather when it loads and present only the most useful current conditions. If weather cannot be retrieved, it will clearly show that weather is unavailable.

## Scope

- **In scope:**
  - A weather card showing Amsterdam's current temperature in °C
  - A human-readable weather condition
  - The current hour's chance of rain as a percentage
  - Wind speed in km/h and wind direction
  - A fixed Amsterdam location defined by the frontend and sent to the backend
  - Weather retrieval when the dashboard loads
  - An unavailable state when weather retrieval fails
- **Out of scope:**
  - Automatic location detection
  - A location settings screen
  - Forecasts for later today or future days
  - Automatic background refresh
  - Displaying cached or stale weather after a failure

## Acceptance criteria

- [x] The dashboard shows the current temperature for Amsterdam in °C.
- [x] The dashboard shows a human-readable current weather condition.
- [x] The dashboard shows the current hour's chance of rain as a percentage.
- [x] The dashboard shows wind speed in km/h and wind direction.
- [x] The frontend supplies the fixed Amsterdam location when requesting weather.
- [x] Weather is requested when the dashboard loads.
- [x] The dashboard shows that weather is unavailable when the request fails.
- [x] The dashboard does not refresh weather automatically after loading.

## Dependencies

- **Blocked by:** None
- **Related:** None

## Notes

- The weather provider and exact API contract will be selected in the implementation plan.
- Amsterdam is the fixed location for this feature, but the location source should be replaceable with browser geolocation in a later feature.
