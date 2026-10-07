# WEA-006: Refresh weather automatically

| Field | Value |
|---|---|
| Status | Approved |
| Priority | Medium |

## Description

As the dashboard user, I want the weather to refresh automatically so that the information remains useful without manual interaction.

The dashboard will request updated weather every 10 minutes while it remains open. If a request fails, it will retry with exponential backoff, starting after 1 second and increasing up to a maximum delay of 10 minutes. If a refresh fails after weather has been loaded successfully, the dashboard will keep showing the last successful weather, clearly indicate that it is stale, and show when that weather was last successfully refreshed. A later successful refresh will replace the stale data, remove the stale indication, and resume the normal 10-minute refresh cadence.

## Scope

- **In scope:**
  - Automatic weather refresh every 10 minutes while the dashboard is open
  - Retrying failed weather requests with exponential backoff
  - Starting the retry delay at 1 second and capping it at 10 minutes
  - Replacing displayed weather with the latest successful response
  - Retaining the last successful weather when a later refresh fails
  - Clearly indicating when the displayed weather is stale
  - Showing the date and time of the last successful refresh while weather is stale
  - Removing the stale indication after a later refresh succeeds
  - Continuing refresh attempts after a failed request using the backoff schedule
  - Preserving the existing unavailable state when no weather has ever been loaded successfully
- **Out of scope:**
  - A user-configurable refresh interval
  - A manual refresh control
  - Notifications about refreshed or stale weather
  - Persisting weather data after the dashboard is closed
  - Automatic refresh for forecasts or other weather-related capabilities not yet implemented

## Acceptance criteria

- [ ] The dashboard requests updated weather every 10 minutes while it remains open.
- [ ] The dashboard does not require user interaction to trigger a scheduled refresh.
- [ ] After a failed weather request, the dashboard retries after 1 second.
- [ ] Each subsequent failed retry doubles the previous delay, up to a maximum delay of 10 minutes.
- [ ] A successful retry resets the retry schedule and resumes the normal 10-minute refresh cadence.
- [ ] After a successful refresh, the dashboard shows the newly retrieved weather conditions.
- [ ] When a refresh fails after a successful weather response, the dashboard continues showing the last successful weather.
- [ ] The dashboard clearly indicates that the displayed weather is stale after a failed refresh.
- [ ] While weather is stale, the dashboard shows when it was last successfully refreshed.
- [ ] A later successful refresh replaces the stale weather and removes the stale indication.
- [ ] Refresh attempts continue after a failed request according to the backoff schedule.
- [ ] If no weather response has ever succeeded, the dashboard shows the existing unavailable state until a refresh succeeds.

## Dependencies

- **Blocked by:** [WEA-001](./WEA-001-current-weather.md)
- **Related:** None

## Notes

- The 10-minute interval applies while the dashboard is open; refresh behavior when the page is hidden or suspended will be decided in the implementation plan.
- Retry delays double after each failed attempt, beginning at 1 second (1s, 2s, 4s, …) and capping at 10 minutes; the exact timer behavior will be decided in the implementation plan.
- Stale weather is retained in memory only and is not persisted after the dashboard is closed.
- The stale indicator is a small, muted disclaimer: “Weather may be outdated · Last updated [date and time]”.
