create table saved_offices (
    id uuid primary key,
    label varchar(120) not null,
    address varchar(500) not null,
    latitude double precision not null check (latitude between -90 and 90),
    longitude double precision not null check (longitude between -180 and 180),
    deleted boolean not null default false
);

create table work_schedule (
    id boolean primary key default true check (id),
    start_time time not null,
    end_time time not null,
    check (start_time < end_time)
);

create table work_schedule_days (
    day_of_week smallint primary key check (day_of_week between 1 and 7),
    status varchar(20) not null check (status in ('OFFICE', 'WORK_FROM_HOME', 'NON_WORKING')),
    office_id uuid references saved_offices(id),
    check ((status = 'OFFICE') = (office_id is not null))
);

insert into work_schedule (id, start_time, end_time) values (true, '09:00', '17:00');

insert into work_schedule_days (day_of_week, status, office_id)
select day_number, 'NON_WORKING', null
from generate_series(1, 7) as day_number(day_number);
