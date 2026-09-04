# WEA-002: Use the browser location

| Field | Value |
|---|---|
| Status | Draft |
| Priority | Low |

## Description

As the dashboard user, I want the dashboard to use my browser's current location so that location-dependent information reflects where I am.

The dashboard will request the browser's location when it loads and supply the resulting coordinates when requesting weather. If a location cannot be obtained, the dashboard will show that the location is unavailable.

## Scope

- **In scope:**
  - Requesting the current location through the browser when the dashboard loads
  - Supplying the browser-provided coordinates when requesting weather
  - Showing a location-unavailable state when permission is denied, location services fail, or browser geolocation is unsupported
- **Out of scope:**
  - Searching for or manually entering a city
  - Falling back to Amsterdam or another default location
  - Saving a location for later visits
  - Tracking location changes after the dashboard loads
  - Displaying a precise address

## Acceptance criteria

- [ ] The dashboard requests the current location from the browser when it loads.
- [ ] The dashboard uses the coordinates supplied by the browser when requesting weather.
- [ ] The dashboard shows that the location is unavailable when permission is denied.
- [ ] The dashboard shows that the location is unavailable when geolocation fails or is unsupported.
- [ ] The dashboard does not fall back to a default location.
- [ ] The dashboard does not persist the supplied location.
- [ ] The dashboard does not monitor location changes after loading.

## Dependencies

- **Blocked by:** [WEA-001](./WEA-001-current-weather.md)
- **Related:** None

## Notes

- Browser permission behavior is controlled by the browser. The application requests location on each dashboard load, but the browser may remember the user's permission choice.
