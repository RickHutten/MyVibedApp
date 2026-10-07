# SCH-009: Refresh outbound commute information

| Field | Value |
|---|---|
| Status | Draft |
| Priority | Low |

## Description

As a user, I want commute information to refresh periodically so that the dashboard reflects changes in routes, delays, and disruptions.

## Scope

- **In scope:**
  - Periodic refresh of today’s outbound commute
  - Periodic refresh of the next working day’s commute when displayed
  - Updated route, departure, and disruption information
- **Out of scope:**
  - Return commute refreshes
  - User-configurable refresh intervals

## Acceptance criteria

- [ ] The displayed outbound commute is refreshed periodically.
- [ ] The dashboard updates the recommendation when relevant transport data changes.
- [ ] A failed refresh does not remove the last usable commute information.

## Dependencies

- **Blocked by:** [SCH-008](./SCH-008-surface-future-transit-disruptions.md)
- **Related:** None

## Notes

- The refresh interval and stale-data behavior will be refined later.
