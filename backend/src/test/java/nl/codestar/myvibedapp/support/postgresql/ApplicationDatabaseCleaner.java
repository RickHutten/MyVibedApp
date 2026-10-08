package nl.codestar.myvibedapp.support.postgresql;

import org.springframework.jdbc.core.JdbcTemplate;

public final class ApplicationDatabaseCleaner {

    private final JdbcTemplate jdbcTemplate;

    public ApplicationDatabaseCleaner(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void resetScheduleData() {
        jdbcTemplate.execute("truncate table one_off_schedule_overrides, recurring_schedule_rules, "
                + "work_schedule_days, saved_offices, work_schedule, commute_preferences");
        jdbcTemplate.execute("insert into work_schedule (id, start_time, end_time) "
                + "values (true, '09:00', '17:00')");
        jdbcTemplate.execute("insert into work_schedule_days (day_of_week, status, office_id) "
                + "select day_number, 'NON_WORKING', null from generate_series(1, 7) as day_number");
        jdbcTemplate.execute("insert into commute_preferences "
                + "(id, wake_up_lead_minutes, transit_access_buffer_minutes, office_arrival_lead_minutes) "
                + "values (true, 45, 5, 5)");
    }
}
