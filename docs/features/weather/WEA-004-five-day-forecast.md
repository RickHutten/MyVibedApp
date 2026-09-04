# WEA-004: Show a five-day weather forecast

| Field | Value |
|---|---|
| Status | Draft |
| Priority | Low |

## Description

As the dashboard user, I want to see the weather forecast for the next five days so that I can anticipate upcoming conditions at a glance.

The forecast starts tomorrow and displays five full days in one horizontal row.

## Scope

- **In scope:**
  - Forecasts for the five full calendar days starting tomorrow
  - The day or date for each forecast
  - Daily high and low temperatures in °C
  - A human-readable weather condition for each day
  - A weather illustration for each day's condition
  - Daily chance of rain as a percentage
  - One horizontal row containing all five days
  - An unavailable state when the forecast cannot be retrieved
- **Out of scope:**
  - Hourly forecasts
  - Forecasts beyond five days
  - Wind details for forecast days


## Acceptance criteria

- [ ] The dashboard shows five forecast days beginning tomorrow.
- [ ] Each forecast shows its day or date.
- [ ] Each forecast shows its high and low temperatures in °C.
- [ ] Each forecast shows an illustration for its weather condition.
- [ ] Each forecast shows its chance of rain as a percentage.
- [ ] All five forecast days appear in one horizontal row.
- [ ] The dashboard shows that the forecast is unavailable when it cannot be retrieved.

## Dependencies

Link every referenced story identifier to its Markdown file using a relative link.

- **Blocked by:** [WEA-003](./WEA-003-weather-illustration.md)
- **Related:** None

## Notes

- This story uses the same location and weather provider integration as the current-weather story.
