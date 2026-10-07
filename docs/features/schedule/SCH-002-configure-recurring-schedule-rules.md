# SCH-002: Configure recurring schedule rules

| Field | Value |
|---|---|
| Status | Approved |
| Priority | High |

## Description

As a user, I want to configure recurring exceptions to my default work schedule so that regular changes in location or work status are applied automatically.

## Scope

- **In scope:**
  - Creating, editing, and deleting recurring rules in schedule settings
  - Rules that repeat every _x_ days
  - Rules that repeat every _x_ weeks on one or more selected weekdays
  - Rules that repeat every _x_ months either on a calendar day or on the first, second, third, fourth, or last occurrence of a selected weekday
  - A required start date that anchors the recurrence cycle
  - An optional, inclusive end date
  - Setting one rule outcome: office at a saved office, work from home, or non-working
  - Automatic precedence between applicable recurring rules
- **Out of scope:**
  - One-off date changes
  - Calendar integration
  - Commute calculation
  - Showing recurring rules themselves on the dashboard

## Acceptance criteria

- [ ] The user can add, edit, and delete a recurring rule from schedule settings.
- [ ] The user can configure a rule to repeat every _x_ days.
- [ ] The user can configure a rule to repeat every _x_ weeks and select one or more weekdays.
- [ ] The user can configure a rule to repeat every _x_ months on a calendar day.
- [ ] The user can configure a rule to repeat every _x_ months on the first, second, third, fourth, or last occurrence of a selected weekday.
- [ ] A recurring rule requires a start date, and the start date anchors when its recurrence cycle begins.
- [ ] The user can leave the end date unset or set an end date that is inclusive.
- [ ] A monthly calendar-day rule skips a month when that month does not contain the configured day.
- [ ] The user can set the rule outcome to office, work from home, or non-working.
- [ ] An office outcome requires one of the saved offices from [SCH-001](./SCH-001-configure-default-weekly-work-pattern.md).
- [ ] A rule does not affect dates before its start date or after its inclusive end date.
- [ ] The system rejects a rule when it overlaps another rule at the same recurrence level during any of their active dates, and identifies the conflicting rule.
- [ ] Rules at different recurrence levels may overlap; an applicable monthly rule overrides an applicable weekly rule, and an applicable weekly rule overrides an applicable daily rule.
- [ ] Deleting or editing a rule causes the next applicable rule, including the SCH-001 default schedule, to be used automatically.
- [ ] A rule remains visible in schedule settings after its end date and is not deleted automatically.

## Dependencies

- **Blocked by:** [SCH-001](./SCH-001-configure-default-weekly-work-pattern.md)
- **Related:** None

## Notes

- Users configure rules in schedule settings; the dashboard shows only the resolved result described by [SCH-004](./SCH-004-resolve-upcoming-work-locations.md).
- The system resolves rules automatically; the user does not choose a rule priority.
- Same-level rules are allowed when their active dates do not overlap.
