create table recurring_schedule_rules (
    id uuid primary key,
    recurrence_level varchar(10) not null check (recurrence_level in ('DAYS', 'WEEKS', 'MONTHS')),
    interval_value integer not null check (interval_value > 0),
    weekday_mask integer not null default 0,
    monthly_pattern_type varchar(30),
    monthly_calendar_day smallint,
    monthly_weekday smallint,
    monthly_occurrence varchar(10),
    start_date date not null,
    end_date date,
    status varchar(20) not null check (status in ('OFFICE', 'WORK_FROM_HOME', 'NON_WORKING')),
    office_id uuid references saved_offices(id),
    check (end_date is null or end_date >= start_date),
    check ((status = 'OFFICE') = (office_id is not null)),
    check (
        (recurrence_level = 'MONTHS' and monthly_pattern_type is not null)
        or (recurrence_level <> 'MONTHS' and monthly_pattern_type is null)
    )
);
