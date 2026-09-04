# WEA-003: Illustrate the current weather

| Field | Value |
|---|---|
| Status | Draft |
| Priority | Medium |

## Description

As the dashboard user, I want a clear weather illustration so that I can recognize the current conditions at a glance.

The weather card will show a large illustration that represents both the current weather condition and whether it is day or night.

## Scope

- **In scope:**
  - A large illustrated weather icon in the weather card
  - Distinct illustrations for the supported weather conditions
  - Day and night variants where appropriate
  - A neutral fallback illustration for an unknown or unsupported condition
- **Out of scope:**
  - Animated weather effects
  - Full-card background artwork
  - Forecast illustrations
  - User-selectable illustration styles

## Acceptance criteria

- [ ] The weather card shows a large illustration for the current weather condition.
- [ ] The illustration changes when the current weather condition changes.
- [ ] Day and night use distinct illustrations where appropriate.
- [ ] An unknown or unsupported condition shows a neutral fallback illustration.
- [ ] The illustration does not obscure the textual weather information.

## Dependencies

- **Blocked by:** [WEA-001](./WEA-001-current-weather.md)
- **Related:** None

## Notes

- The illustration set and condition-to-illustration mapping will be selected in the implementation plan.
