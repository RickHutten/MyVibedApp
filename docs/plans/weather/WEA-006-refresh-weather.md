# Implementation plan: WEA-006 — Refresh weather automatically

Related story: [WEA-006 — Refresh weather automatically](../../features/weather/WEA-006-refresh-weather.md)
Status: Draft

## Approach

Keep the existing `/api/weather/current` contract and backend provider integration unchanged. Automatic refresh, retry scheduling, stale retention, and the last-successful-refresh timestamp are client-side concerns for the Angular weather experience; no server cache or backend retry is needed.

Extend the frontend weather data layer with a single refresh stream that performs the initial request, waits 10 minutes after each success, and retries failures without overlapping requests. A failed request emits a failure state before scheduling the next attempt at 1 second, then 2 seconds, 4 seconds, and so on, capped at 10 minutes. The weather card will retain the last successful response in signals, show a stale state and client-local successful-response time after later failures, and reset to the normal 10-minute cadence after success. The stream will be disposed when the card is destroyed.

## Implementation steps

1. **Define refresh events and timing policy**
   - Change: Add frontend-owned types/constants for successful refresh events, failed refresh notifications, the 10-minute refresh interval, the 1-second initial retry delay, the doubling factor, and the 10-minute retry cap. Capture the last-successful-refresh timestamp when the client receives a successful response.
   - Files: `../../../frontend/src/app/weather/current-weather.ts`, `frontend/src/app/weather/weather.service.ts` (or a focused weather refresh model alongside them).

2. **Implement one non-overlapping refresh stream**
   - Change: Extend `WeatherService` with a refresh operation that requests weather immediately, emits failures for state handling, retries after the exponential delays, and waits 10 minutes after success before starting the next scheduled request. Keep the existing Amsterdam coordinates and `/api/weather/current` request unchanged. Continue retrying at the 10-minute cap until a request succeeds, then reset the retry sequence. Ensure timers and in-flight requests are canceled when the consumer is destroyed.
   - Files: `../../../frontend/src/app/weather/weather.service.ts` and related frontend weather types.

3. **Model ready, stale, unavailable, and loading states**
   - Change: Update `WeatherCard` to subscribe to the refresh stream and keep the last successful `CurrentWeather` value. On a failure with no successful response, retain the existing unavailable state; on a later failure, retain the weather, mark it stale, and retain its last-successful timestamp. On success, replace the weather, clear stale state, record the new timestamp, and reset the retry schedule. Use Angular signals and lifecycle-safe subscription cleanup.
   - Files: `../../../frontend/src/app/weather/weather-card.ts`.

4. **Render stale status and refresh time accessibly**
   - Change: Add a concise stale indicator that includes the local date and time of the last successful refresh. Keep the current loading, unavailable, and successful weather content intact. Use semantic status/live-region markup and styles that remain readable on the tablet dashboard without making stale data look current.
   - Files: `../../../frontend/src/app/weather/weather-card.html`, `frontend/src/app/weather/weather-card.scss`.

5. **Record the refresh policy**
   - Change: Update the architecture notes to distinguish WEA-001's initial-load-only behavior from WEA-006's browser-side 10-minute refresh, in-memory stale retention, and exponential retry policy. Document that the timestamp represents the client time at which the last response was successfully received and that no weather data is persisted.
   - Files: `../../ARCHITECTURE.md`.

## Test strategy

- **Functional coverage:**
  - Angular component tests use `HttpTestingController`, Vitest fake timers, and a controlled system clock to verify the initial request, a new request after 10 minutes, and replacement of displayed weather after a successful refresh.
  - Verify failed requests retry after 1 second, then double the delay on subsequent failures, cap retries at 10 minutes, and continue retrying at the cap until success. Verify a successful retry resets the next scheduled request to 10 minutes.
  - Verify an initial failure remains in the existing unavailable state while retries continue, without stale data or a fabricated refresh timestamp.
  - Verify a failure after success retains the previous weather, renders a stale indicator and the exact last-successful-refresh time, and that a later success removes the stale indicator and updates the timestamp.
  - Verify the weather request still uses the fixed Amsterdam coordinates and that the component does not create overlapping requests while a retry is pending.
  - Extend the existing accessibility test to cover the stale state and its status messaging.
- **Test level:** Angular component/integration tests through the rendered component and HTTP testing boundary; no backend changes or new end-to-end test is required for this client-only behavior.
- **Commands:**
  - `cd frontend && npm run check`
  - `cd backend && ./mvnw verify`
  - `git diff --check`

## Technical decisions

- Keep retry scheduling in the frontend refresh layer rather than the backend. This preserves the existing API contract, avoids duplicate retry loops, and lets the dashboard display stale state immediately after a failed response.
- Use an RxJS timer-based refresh cycle rather than `setInterval`, so each retry and scheduled refresh follows the completion of the previous request and cannot overlap it. The browser may throttle timers in background tabs; this story does not add visibility-specific catch-up behavior.
- Use the client receipt time for “last successfully refreshed,” because the current API does not expose a provider refresh timestamp and the story concerns dashboard freshness.
- Retain stale weather only in the running page. Do not add persistence, server caching, manual refresh controls, notifications, or forecast refreshes.
- Keep the retry delay capped at 10 minutes; after reaching the cap, failed attempts continue every 10 minutes until success. A successful response resets the delay to the 10-minute normal refresh interval.
