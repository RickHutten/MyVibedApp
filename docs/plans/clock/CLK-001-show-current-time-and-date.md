# Implementation plan: CLK-001 — Show the current time and date

Related story: [CLK-001 — Show the current time and date](../../features/clock/CLK-001-show-current-time-and-date.md)
Status: Implemented

## Approach

Add a focused clock component to the Angular dashboard and introduce one shared frontend location dependency for dashboard-wide location context. A root-provided `DashboardLocationService` will expose the initial Amsterdam location as an immutable value containing its display name, coordinates, and IANA timezone (`Europe/Amsterdam`). The existing weather service will obtain its request coordinates from this dependency instead of owning a private Amsterdam constant, and the new clock will use the same dependency for timezone-aware formatting.

The clock will derive both its time and date from one instant, formatting them explicitly for the shared location’s timezone with the browser’s `Intl.DateTimeFormat` support. It will display 24-hour time without seconds and an English weekday/day/month date. A timer will keep the instant current while the component is mounted, including across midnight, and will be cleaned up when the component is destroyed. No backend endpoint, persistence, timezone selection, browser-location behavior, or external time service will be added.

The dashboard layout will be adjusted from a single centered weather card to a tablet-first composition containing the clock and weather card. The clock remains a calm, non-interactive element with semantic time markup and accessible text. Styling will retain normal-browser usability at narrower widths.

## Implementation steps

1. **Add the shared dashboard location dependency**
   - Change: Define an immutable dashboard location value with display name, latitude, longitude, and IANA timezone, and expose the initial Amsterdam value through a root-provided `DashboardLocationService`. Keep the API limited to the current fixed location; do not add location selection, persistence, or browser geolocation.
   - Areas: a shared frontend location model/service under the Angular application.

2. **Move weather coordinates to the shared location**
   - Change: Inject `DashboardLocationService` into the existing weather service and use its latitude and longitude for `/api/weather/current`. Remove the weather service’s private Amsterdam constant without changing the weather request contract or behavior.
   - Areas: `../../../frontend/src/app/weather/weather.service.ts` and existing weather component coverage.

3. **Build the timezone-aware clock**
   - Change: Add a focused clock component that reads the shared location timezone, derives the displayed time and date from one current instant, and formats them with `Intl.DateTimeFormat` using explicit `Europe/Amsterdam`-compatible timezone handling, 24-hour time, and English weekday/day/month output. Keep the display current with a lifecycle-managed timer and update both values when the location-local date crosses midnight.
   - Areas: a new clock component, template, styles, and component tests under `../../../frontend/src/app/clock`.

4. **Compose the dashboard for clock and weather**
   - Change: Render the clock alongside the existing weather card and adapt the dashboard grid for an always-on tablet display with a responsive single-column fallback. Use semantic markup, preserve readable hierarchy, and do not add controls or interaction.
   - Areas: `../../../frontend/src/app/dashboard/dashboard.ts` and clock/weather presentation styles as needed.

5. **Record the shared location boundary**
   - Change: Update the architecture documentation to state that frontend location-dependent features consume one application-owned dashboard location source, initially fixed to Amsterdam, while provider-specific weather behavior remains isolated behind the backend API.
   - Area: `../../ARCHITECTURE.md`.

## Test strategy

- **Functional coverage:**
  - A clock component test uses fake time to render a known instant and verifies the visible 24-hour Amsterdam time and English date, then advances time across an Amsterdam-local midnight and verifies both values update without a reload or interaction.
  - The clock test runs under a deliberately different process/browser timezone where practical, or supplies an instant whose Amsterdam result is unambiguous, so it proves formatting uses the shared IANA timezone rather than the browser default.
  - Existing weather component coverage continues to verify that the backend request contains Amsterdam’s coordinates after ownership moves to `DashboardLocationService`, protecting WEA-001 behavior through the public HTTP boundary rather than testing service internals.
  - A dashboard-level component or route test verifies that both the clock and Amsterdam weather experience are rendered together. Clock markup is included in the existing automated accessibility check or receives equivalent Axe coverage.
  - No backend or end-to-end tests are planned because this story changes only frontend composition and time formatting; component and route boundaries cover the acceptance criteria without a live clock or provider.
- **Test level:** Angular component and dashboard route tests, with fake timers for deterministic time behavior; no narrow unit test for the location service because its observable use is covered by clock rendering and the weather HTTP request.
- **Commands:**
  - `cd frontend && npm run check`

## Technical decisions

- Use one root-provided `DashboardLocationService` as the frontend source for location name, coordinates, and IANA timezone. This removes duplicated Amsterdam knowledge while keeping the dependency replaceable for WEA-002; it does not pre-design asynchronous geolocation behavior.
- Represent the timezone with the IANA identifier `Europe/Amsterdam` rather than a fixed UTC offset so daylight-saving changes are handled by the platform.
- Use the platform `Intl.DateTimeFormat` API rather than adding a date library. Formatting will specify its locale, timezone, and 24-hour behavior explicitly so output does not inherit the browser’s timezone or locale.
- Derive the displayed time and date from the same instant to prevent inconsistent values around midnight.
- Keep timer ownership inside the clock component and clean it up with Angular lifecycle support. The implementation may align updates to minute boundaries, but correctness must not depend on the component being created exactly at the start of a minute.
- Keep the location source in the frontend for now because WEA-001 already owns request coordinates there and CLK-001 needs no backend data. Moving user-configurable location to persistence remains outside this story.
- Treat the shared location boundary as a lasting system decision and record it in `../../ARCHITECTURE.md` during implementation.
