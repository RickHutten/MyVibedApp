# Implementation plan: WEA-001 — Show current weather

Related story: [WEA-001 — Show current weather](../../features/weather/WEA-001-current-weather.md)
Status: Approved

## Approach

Add a weather vertical slice across the existing Spring Boot API and Angular dashboard. The frontend will request `GET /api/weather/current` once during initial dashboard loading, supplying fixed Amsterdam coordinates as query parameters. The backend will validate those coordinates, fetch current conditions from Open-Meteo, translate provider-specific data into an application-owned response, and return temperature, condition, current-hour rain probability, wind speed, and compass direction.

The backend will follow the backend-specific hexagonal structure. Weather values belong in the domain layer; orchestration and the outbound provider port belong in the application layer; the HTTP and Open-Meteo adapters belong in the interface layer. Domain and application code will have no Spring MVC, HTTP-client, or provider DTO dependencies. The Open-Meteo adapter will request metric units and Amsterdam-local timestamps explicitly, select the hourly precipitation probability corresponding to the provider's current observation hour, and map WMO weather codes and wind degrees to application-owned values. Provider failures or unusable responses will become an application exception that the HTTP adapter translates to HTTP 503. The frontend will treat any failed request as an unavailable weather state and will not retain stale values or retry automatically.

The Angular root will remain a small router shell and lazy-load an initial dashboard route containing a focused weather-card component. A single-responsibility weather service will own the fixed Amsterdam location and backend request, while the card will use signals for local loading, success, and unavailable state. The app will use a relative `/api` URL, with an Angular development proxy forwarding requests to Spring Boot so production can remain same-origin.

## Implementation steps

1. **Define the backend weather boundary and API contract**
   - Change: Add immutable domain values for a weather location and current conditions. Define an application-owned `WeatherProvider` port with a `getCurrentWeather(location)` operation, and have the application layer coordinate that operation without depending on HTTP or provider-specific models. Add a thin HTTP controller in the interface layer that validates latitude and longitude with Jakarta Validation, delegates to the application layer, and translates the result into an interface-owned response record. Expose `GET /api/weather/current?latitude={latitude}&longitude={longitude}` returning `temperatureC`, `condition`, `precipitationProbabilityPercent`, `windSpeedKmh`, and `windDirection`.
   - Areas: backend weather domain, application service and provider port, and HTTP interface adapter.

2. **Integrate Open-Meteo behind the provider interface**
   - Change: Implement the application-owned provider port in a separate Open-Meteo adapter in the interface layer. Use Spring's existing `RestClient`; do not add another HTTP-client dependency. Request `temperature_2m`, `weather_code`, `wind_speed_10m`, and `wind_direction_10m` as current values plus hourly `precipitation_probability`, using `temperature_unit=celsius`, `wind_speed_unit=kmh`, `timezone=auto`, and `forecast_days=1`. Keep Open-Meteo response records private to the adapter, match the current observation timestamp to its hour, translate WMO codes and wind degrees into application-owned values, and reject incomplete or inconsistent responses. Bind the provider base URL and finite connection/response timeouts from configuration.
   - Areas: Open-Meteo interface adapter and backend provider configuration.

3. **Define predictable backend failure behavior**
   - Change: Translate provider timeout, transport, and invalid-payload failures into an application-owned `WeatherUnavailableException`. Handle that exception in the HTTP interface layer as a sanitized HTTP 503 response without provider payloads, stack traces, or internal details. Preserve Spring's HTTP 400 response for invalid coordinates. Do not add retries, caching, fallback behavior, or failure logging containing precise coordinates.
   - Areas: application-owned weather failure model and HTTP exception translation.

4. **Add the frontend weather data layer**
   - Change: Register Angular `HttpClient`, define the backend response type, and add a weather service whose fixed Amsterdam coordinates are the single replaceable location source. Make one request to the relative `/api/weather/current` endpoint and pass the coordinates as query parameters.
   - Areas: Angular application configuration and weather data service/model.

5. **Build the dashboard weather card and states**
   - Change: Replace the Angular starter screen with a small router shell and lazy-load a tablet-first dashboard component containing the weather card. Use signals for the card's local loading, success, and unavailable state, native template control flow, and an injected weather service. Render accessible status messaging and WCAG AA styling, followed by either all required current conditions or a clear unavailable state. Do not add polling, timers, retries, stale-data retention, forecasts, or location controls.
   - Areas: Angular root routing shell, dashboard feature route, weather card, and shared dashboard styling.

6. **Connect local frontend development to the backend**
   - Change: Add an Angular development proxy for `/api` to the local Spring Boot server and reference it from the serve configuration, while retaining relative URLs for same-origin deployment.
   - Areas: Angular serve configuration and development proxy configuration.

7. **Record the provider decision**
   - Change: Update the architecture documentation to record Open-Meteo as the first weather provider, the adapter boundary that contains provider-specific behavior, the absence of caching or refresh in WEA-001, and any attribution requirement confirmed from the provider terms during implementation.
   - Area: `../../ARCHITECTURE.md`.

## Test strategy

- **Functional coverage:**
  - A backend integration test drives `GET /api/weather/current` through MockMvc with a controllable provider implementation and verifies the public success contract, coordinate forwarding, invalid-coordinate rejection, and sanitized HTTP 503 behavior. It will not directly invoke or mock controller methods.
  - An Open-Meteo adapter test uses Spring Test's controllable HTTP support with `RestClient` to verify the outgoing query and the application-visible mapping, including current-hour rain selection, WMO condition text, compass direction, and malformed-response handling. This avoids adding a test HTTP-server dependency and does not assert private methods or client call order.
  - Angular route/component tests render the lazy-loaded dashboard through TestBed with HTTP testing support and verify the request is made once with Amsterdam's fixed coordinates, all returned measurements are shown with their units, a failed request replaces loading content with the unavailable state, and the rendered states pass the configured accessibility checks.
  - No end-to-end test is planned for this first isolated card: the backend API and Angular component boundaries cover the acceptance criteria without depending on the live third-party service.
- **Test level:** Backend integration/adapter tests and Angular component tests; no narrow unit tests unless mapping complexity discovered during implementation warrants parameterized coverage.
- **Commands:**
  - `cd backend && ./mvnw verify`
  - `cd frontend && npm run check`

## Technical decisions

- Use Open-Meteo because its forecast API provides all WEA-001 fields in one request and does not require a client-side credential. The backend remains the only caller so provider details stay replaceable and later integrations have a consistent application API.
- Organize the slice into domain, application, and interface layers as required by `../../../backend/AGENTS.md`, without fixing the exact package or file breakdown before implementation. Keep the HTTP and provider adapters separate; expose only the contracts that genuinely cross layer boundaries.
- Use immutable records for the simple domain, API, and provider DTO values and constructor injection for Spring collaborators. Use Lombok constructor annotations where they remove repetitive dependency-injection boilerplate; do not use Lombok for mutable state or incidental convenience methods.
- Keep the frontend-to-backend contract provider-neutral; raw WMO codes, provider field names, and wind degrees do not cross the application API boundary.
- Pass latitude and longitude on each frontend request rather than hard-coding Amsterdam in the backend. This deliberately prepares the same endpoint for WEA-002 while keeping Amsterdam's fixed coordinates owned by the frontend in this story.
- Represent wind direction as a human-readable compass label from the backend so display semantics are consistent and the Angular component remains presentational.
- Treat missing required provider fields as an unavailable response rather than displaying partial weather. WEA-001 has no cache, background refresh, automatic retry, or database work.
- Use relative `/api` requests and a development proxy instead of enabling broad cross-origin access in Spring Boot.
- Confirm and satisfy Open-Meteo's current attribution terms during implementation; any required attribution will be minimal and visible without changing the weather information hierarchy.
