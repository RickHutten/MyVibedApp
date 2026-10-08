create table one_off_schedule_overrides (
    id uuid primary key,
    start_date date not null,
    end_date date,
    status varchar(20) not null check (status in ('OFFICE', 'WORK_FROM_HOME', 'NON_WORKING')),
    office_id uuid references saved_offices(id),
    check (end_date is null or end_date >= start_date),
    check ((status = 'OFFICE') = (office_id is not null))
);
