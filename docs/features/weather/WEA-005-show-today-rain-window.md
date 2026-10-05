# WEA-005: Show today’s expected rain

| Field | Value |
|---|---|
| Status | Approved |
| Priority | Medium |

## Description

As the dashboard user, I want to know clearly whether rain is expected today and when it will occur so that I can plan my day without interpreting an hourly forecast.

The dashboard will show an additional rain indicator only when meaningful rain is expected today. The indicator uses concise, natural-language sentences that lead with the rain intensity. For example: “Light rain expected later today, between 15:00 and 17:00.” When rain is already occurring, it can say: “Light rain now, continuing until 17:00.” When there are multiple meaningful rain periods, they are combined into a concise sentence, such as: “Rain expected later today: light rain between 15:00 and 17:00, followed by moderate rain between 21:00 and 22:00.” When no meaningful rain is expected, the indicator is not shown.

## Scope

- **In scope:**
  - An additional today-only rain indicator in the weather experience when meaningful rain is expected
  - A human-readable expected rain start time
  - A human-readable expected rain end time when the forecast provides one
  - A plain-language rain intensity for the displayed rain period, such as light, moderate, or heavy rain
  - Natural-language handling for rain that is currently occurring
  - A concise natural-language summary of multiple meaningful rain periods
  - Treating completely dry gaps of 15 minutes or less as part of the surrounding rain period
  - Ignoring tiny precipitation amounts and low-probability rain
  - Hiding the rain indicator when no rain is forecast
  - Silently omitting the indicator when the required today forecast cannot be retrieved
  - Times shown in the dashboard location’s local time
- **Out of scope:**
  - Rain forecasts for future days
  - A full hourly forecast or precipitation chart
  - Numeric rainfall amounts in millimeters
  - Notifications or reminders about upcoming rain
  - Automatic background refresh

## Acceptance criteria

- [ ] The dashboard shows an additional rain indicator when meaningful rain is expected during the current local calendar day.
- [ ] When rain is expected, the indicator shows the expected start time in a human-readable format.
- [ ] When the expected rain period has a forecast end time, the indicator shows both times in a natural-language sentence, such as “Light rain expected later today, between 15:00 and 17:00.”
- [ ] When rain is currently occurring, the indicator uses present-tense wording, such as “Light rain now, continuing until 17:00.”
- [ ] When rain is expected, the indicator describes the expected rain intensity in plain language, such as light, moderate, or heavy rain.
- [ ] When multiple meaningful rain periods are expected, the indicator summarizes them in one concise natural-language sentence.
- [ ] Completely dry gaps of 15 minutes or less do not split one displayed rain period into separate periods.
- [ ] A gap with any forecast rain, including very low rain, is not treated as completely dry.
- [ ] Tiny precipitation amounts and low-probability rain do not cause the indicator to appear.
- [ ] When no rain is expected today, the rain indicator is not shown.
- [ ] The indicator uses the dashboard location’s local time.
- [ ] The dashboard silently omits the rain indicator when today’s rain forecast is unavailable.

## Dependencies

- **Blocked by:** [WEA-001](./WEA-001-current-weather.md)
- **Related:** [WEA-004](./WEA-004-five-day-forecast.md)

## Notes

- The implementation should map the provider’s precipitation data to understandable intensity labels rather than exposing millimeters to the user.
- The exact thresholds for meaningful precipitation, low probability, and intensity labels will be selected in the implementation plan.
- WEA-001 is a dependency because this story extends the existing weather widget and weather-provider integration rather than introducing a separate dashboard element.
- The weather provider and exact forecast data contract will be selected in the implementation plan.
