alter table commute_preferences
    add column visibility_cutoff time not null default '12:00';
