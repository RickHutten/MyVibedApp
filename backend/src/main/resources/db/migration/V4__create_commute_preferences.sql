create table commute_preferences (
    id boolean primary key default true check (id),
    wake_up_lead_minutes bigint not null default 45 check (wake_up_lead_minutes >= 0),
    transit_access_buffer_minutes bigint not null default 5 check (transit_access_buffer_minutes >= 0),
    office_arrival_lead_minutes bigint not null default 5 check (office_arrival_lead_minutes >= 0)
);

insert into commute_preferences (
    id,
    wake_up_lead_minutes,
    transit_access_buffer_minutes,
    office_arrival_lead_minutes
) values (true, 45, 5, 5);