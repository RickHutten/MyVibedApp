package nl.codestar.myvibedapp.commute.adapters.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import nl.codestar.myvibedapp.commute.application.TransitProvider;
import nl.codestar.myvibedapp.commute.application.TransitRequest;
import nl.codestar.myvibedapp.commute.domain.PublicTransportSegment;
import nl.codestar.myvibedapp.commute.domain.TransitJourney;
import nl.codestar.myvibedapp.commute.domain.WalkingSegment;
import nl.codestar.myvibedapp.schedule.application.ScheduleStore;
import nl.codestar.myvibedapp.schedule.domain.SavedOffice;
import nl.codestar.myvibedapp.schedule.domain.ScheduleStatus;
import nl.codestar.myvibedapp.support.postgresql.PostgresIntegrationTest;
import nl.codestar.myvibedapp.support.scenario.ScheduleScenario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

@Import(TodayCommuteControllerTest.CommuteConfiguration.class)
class TodayCommuteControllerTest extends PostgresIntegrationTest {

    private static final UUID OFFICE_ID = UUID.fromString("00000000-0000-0000-0000-000000000010");
    private static final ZoneId ZONE = ZoneId.of("Europe/Amsterdam");
    private static final String COMMUTE_ENDPOINT = "/api/work-schedule/commute/today";
    private static final String LATITUDE_PARAMETER = "latitude";
    private static final String LONGITUDE_PARAMETER = "longitude";
    private static final String ORIGIN_LATITUDE = "52.3676";
    private static final String ORIGIN_LONGITUDE = "4.9041";
    private static final String STATUS_PATH = "$.status";
    private static final String AMSTERDAM_ZUID = "Amsterdam Zuid";
    private static final String ROKIN = "Rokin";
    private static final String AMSTERDAM_OFFICE = "Amsterdam office";
    private static final String BUS = "BUS";
    private static final String SERVICE_34 = "34";
    private static final String TODAY_DATE = "2026-10-07";
    private static final String NEXT_OFFICE_DATE = "2026-10-09";
    private static final String TARGET_PATH = "$.target";
    private static final String SEGMENTS_PATH = "$.segments";

    private final ScheduleStore scheduleStore;
    private final ControllableTransitProvider transitProvider;

    @Autowired
    TodayCommuteControllerTest(
            final ScheduleStore scheduleStore,
            final ControllableTransitProvider transitProvider) {
        super();
        this.scheduleStore = scheduleStore;
        this.transitProvider = transitProvider;
    }

    @BeforeEach
    void resetTransitProvider() {
        transitProvider.reset();
    }

    @Test
    void returnsTheTodayJourneyWithCompleteItineraryAndUsesWalkingDurationForLeaveTime() throws Exception {
        persistOfficeDay();
        transitProvider.returnJourney(new TransitJourney(
                LocalDateTime.of(2026, 10, 7, 8, 20),
                LocalDateTime.of(2026, 10, 7, 8, 55),
                List.of(
                        new WalkingSegment(
                                LocalDateTime.of(2026, 10, 7, 8, 20),
                                LocalDateTime.of(2026, 10, 7, 8, 22),
                                Optional.of("Home"),
                                Optional.of(AMSTERDAM_ZUID)),
                        new PublicTransportSegment(
                                0,
                                Optional.of(BUS),
                                Optional.of(SERVICE_34),
                                Optional.of("Central Station"),
                                LocalDateTime.of(2026, 10, 7, 8, 25),
                                LocalDateTime.of(2026, 10, 7, 8, 45),
                                Optional.of(AMSTERDAM_ZUID),
                                Optional.of(ROKIN),
                                Optional.of("3")),
                        new WalkingSegment(
                                LocalDateTime.of(2026, 10, 7, 8, 45),
                                LocalDateTime.of(2026, 10, 7, 8, 48),
                                Optional.of(ROKIN),
                                Optional.of("Rokin tram stop")),
                        new PublicTransportSegment(
                                1,
                                Optional.of("TRAM"),
                                Optional.of("2"),
                                Optional.of("Museumplein"),
                                LocalDateTime.of(2026, 10, 7, 8, 48),
                                LocalDateTime.of(2026, 10, 7, 8, 50),
                                Optional.of("Rokin tram stop"),
                                Optional.of("Museumplein"),
                                Optional.empty()),
                        new WalkingSegment(
                                LocalDateTime.of(2026, 10, 7, 8, 50),
                                LocalDateTime.of(2026, 10, 7, 8, 55),
                                Optional.of("Museumplein"),
                                Optional.of(AMSTERDAM_OFFICE)))));

        mockMvc.perform(get(COMMUTE_ENDPOINT)
                        .param(LATITUDE_PARAMETER, ORIGIN_LATITUDE)
                        .param(LONGITUDE_PARAMETER, ORIGIN_LONGITUDE))
                .andExpect(status().isOk())
                .andExpect(jsonPath(STATUS_PATH).value("AVAILABLE"))
                .andExpect(jsonPath(TARGET_PATH).value("TODAY"))
                .andExpect(jsonPath("$.date").value(TODAY_DATE))
                .andExpect(jsonPath("$.wakeUpTime").value("07:30:00"))
                .andExpect(jsonPath("$.leaveHomeTime").value("08:18:00"))
                .andExpect(jsonPath("$.plannedArrivalTime").value("08:55:00"))
                .andExpect(jsonPath("$.destinationOffice").value(AMSTERDAM_OFFICE))
                .andExpect(jsonPath("$.totalDurationMinutes").value(35))
                .andExpect(jsonPath("$.transferCount").value(1))
                .andExpect(jsonPath("$.segments[0].type").value("WALKING"))
                .andExpect(jsonPath("$.segments[0].from").value("Home"))
                .andExpect(jsonPath("$.segments[1].type").value("PUBLIC_TRANSPORT"))
                .andExpect(jsonPath("$.segments[1].mode").value("BUS"))
                .andExpect(jsonPath("$.segments[1].service").value("34"))
                .andExpect(jsonPath("$.segments[1].from").value(AMSTERDAM_ZUID))
                .andExpect(jsonPath("$.segments[1].to").value(ROKIN))
                .andExpect(jsonPath("$.segments[1].platform").value("3"))
                .andExpect(jsonPath("$.segments[2].type").value("WALKING"))
                .andExpect(jsonPath("$.segments[4].to").value(AMSTERDAM_OFFICE));

        assertThat(transitProvider.request()).isPresent();
        assertThat(transitProvider.request().orElseThrow().origin().latitude()).isEqualTo(52.3676);
        assertThat(transitProvider.request().orElseThrow().targetArrival())
                .isEqualTo(LocalDateTime.of(2026, 10, 7, 8, 55));
    }

    @Test
    void returnsUnavailableWhenTheJourneyArrivesAfterTheTarget() throws Exception {
        persistOfficeDay();
        transitProvider.returnJourney(new TransitJourney(
                LocalDateTime.of(2026, 10, 7, 8, 25),
                LocalDateTime.of(2026, 10, 7, 8, 56),
                List.of(new PublicTransportSegment(
                        0,
                        Optional.of(BUS),
                        Optional.of(SERVICE_34),
                        Optional.of("Central Station"),
                        LocalDateTime.of(2026, 10, 7, 8, 25),
                        LocalDateTime.of(2026, 10, 7, 8, 56),
                        Optional.of(AMSTERDAM_ZUID),
                        Optional.of(ROKIN),
                        Optional.empty()))));

        mockMvc.perform(get(COMMUTE_ENDPOINT)
                        .param(LATITUDE_PARAMETER, ORIGIN_LATITUDE)
                        .param(LONGITUDE_PARAMETER, ORIGIN_LONGITUDE))
                .andExpect(status().isOk())
                .andExpect(jsonPath(STATUS_PATH).value("UNAVAILABLE"))
                .andExpect(jsonPath(SEGMENTS_PATH).isEmpty())
                .andExpect(jsonPath("$.plannedArrivalTime").doesNotExist());
    }

    @Test
    void returnsUnavailableWhenTheOriginCoordinatesAreInvalid() throws Exception {
        mockMvc.perform(get(COMMUTE_ENDPOINT)
                        .param(LATITUDE_PARAMETER, "91")
                        .param(LONGITUDE_PARAMETER, ORIGIN_LONGITUDE))
                .andExpect(status().isOk())
                .andExpect(jsonPath(STATUS_PATH).value("UNAVAILABLE"))
                .andExpect(jsonPath(TARGET_PATH).value("TODAY"))
                .andExpect(jsonPath(SEGMENTS_PATH).isEmpty());
    }

    @Test
    void selectsTheNextOfficeDayAtTheVisibilityCutoff() throws Exception {
        jdbcTemplate.update("update commute_preferences set visibility_cutoff = '08:00:00'");
        persistOfficeDaysOnWednesdayAndFriday();
        transitProvider.returnJourney(new TransitJourney(
                LocalDateTime.of(2026, 10, 9, 8, 20),
                LocalDateTime.of(2026, 10, 9, 8, 55),
                List.of(new PublicTransportSegment(
                        0,
                        Optional.of(BUS),
                        Optional.of(SERVICE_34),
                        Optional.of("Central Station"),
                        LocalDateTime.of(2026, 10, 9, 8, 20),
                        LocalDateTime.of(2026, 10, 9, 8, 55),
                        Optional.of(AMSTERDAM_ZUID),
                        Optional.of(ROKIN),
                        Optional.empty()))));

        mockMvc.perform(get(COMMUTE_ENDPOINT)
                        .param(LATITUDE_PARAMETER, ORIGIN_LATITUDE)
                        .param(LONGITUDE_PARAMETER, ORIGIN_LONGITUDE))
                .andExpect(status().isOk())
                .andExpect(jsonPath(STATUS_PATH).value("AVAILABLE"))
                .andExpect(jsonPath(TARGET_PATH).value("NEXT_OFFICE_DAY"))
                .andExpect(jsonPath("$.date").value(NEXT_OFFICE_DATE))
                .andExpect(jsonPath(SEGMENTS_PATH).isNotEmpty());
        assertThat(transitProvider.request()).hasValueSatisfying(request -> {
            assertThat(request.date()).isEqualTo(LocalDateTime.of(2026, 10, 9, 0, 0).toLocalDate());
            assertThat(request.targetArrival()).isEqualTo(LocalDateTime.of(2026, 10, 9, 8, 55));
        });
    }

    @Test
    void reportsNoUpcomingOfficeDayAtTheVisibilityCutoffWithoutCallingTransitProvider() throws Exception {
        jdbcTemplate.update("update commute_preferences set visibility_cutoff = '08:00:00'");
        ScheduleScenario.builder()
                .office(new SavedOffice(
                        OFFICE_ID, AMSTERDAM_OFFICE, "Amsterdam office address", 52.0907, 5.1214, false))
                .build()
                .persist(scheduleStore);

        mockMvc.perform(get(COMMUTE_ENDPOINT)
                        .param(LATITUDE_PARAMETER, ORIGIN_LATITUDE)
                        .param(LONGITUDE_PARAMETER, ORIGIN_LONGITUDE))
                .andExpect(status().isOk())
                .andExpect(jsonPath(STATUS_PATH).value("NO_UPCOMING_OFFICE_DAY"))
                .andExpect(jsonPath(TARGET_PATH).value("NEXT_OFFICE_DAY"))
                .andExpect(jsonPath(SEGMENTS_PATH).isEmpty())
                .andExpect(jsonPath("$.date").doesNotExist());
        assertThat(transitProvider.request()).isEmpty();
    }

    @Test
    void hidesTheCommuteOnAWorkFromHomeDay() throws Exception {
        ScheduleScenario.builder()
                .weeklyDay(DayOfWeek.WEDNESDAY, ScheduleStatus.WORK_FROM_HOME, null)
                .build()
                .persist(scheduleStore);

        mockMvc.perform(get(COMMUTE_ENDPOINT)
                        .param(LATITUDE_PARAMETER, ORIGIN_LATITUDE)
                        .param(LONGITUDE_PARAMETER, ORIGIN_LONGITUDE))
                .andExpect(status().isOk())
                .andExpect(jsonPath(STATUS_PATH).value("HIDDEN"));
    }

    @Test
    void hidesTheCommuteOnANonWorkingDay() throws Exception {
        ScheduleScenario.builder()
                .weeklyDay(DayOfWeek.WEDNESDAY, ScheduleStatus.NON_WORKING, null)
                .build()
                .persist(scheduleStore);

        mockMvc.perform(get(COMMUTE_ENDPOINT)
                        .param(LATITUDE_PARAMETER, ORIGIN_LATITUDE)
                        .param(LONGITUDE_PARAMETER, ORIGIN_LONGITUDE))
                .andExpect(status().isOk())
                .andExpect(jsonPath(STATUS_PATH).value("HIDDEN"));
    }

    private void persistOfficeDay() {
        ScheduleScenario.builder()
                .office(new SavedOffice(
                        OFFICE_ID, AMSTERDAM_OFFICE, "Amsterdam office address", 52.0907, 5.1214, false))
                .weeklyDay(DayOfWeek.WEDNESDAY, ScheduleStatus.OFFICE, OFFICE_ID)
                .build()
                .persist(scheduleStore);
    }

    private void persistOfficeDaysOnWednesdayAndFriday() {
        ScheduleScenario.builder()
                .office(new SavedOffice(
                        OFFICE_ID, AMSTERDAM_OFFICE, "Amsterdam office address", 52.0907, 5.1214, false))
                .weeklyDay(DayOfWeek.WEDNESDAY, ScheduleStatus.OFFICE, OFFICE_ID)
                .weeklyDay(DayOfWeek.THURSDAY, ScheduleStatus.WORK_FROM_HOME, null)
                .weeklyDay(DayOfWeek.FRIDAY, ScheduleStatus.OFFICE, OFFICE_ID)
                .build()
                .persist(scheduleStore);
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class CommuteConfiguration {

        @Bean
        @Primary
        Clock commuteTestClock() {
            return Clock.fixed(Instant.parse("2026-10-07T06:00:00Z"), ZONE);
        }

        @Bean
        @Primary
        ControllableTransitProvider controllableTransitProvider() {
            return new ControllableTransitProvider();
        }
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    static final class ControllableTransitProvider implements TransitProvider {

        private Optional<TransitJourney> journey = Optional.empty();
        private Optional<TransitRequest> request = Optional.empty();

        @Override
        public Optional<TransitJourney> findJourney(final TransitRequest request) {
            this.request = Optional.of(request);
            return journey;
        }

        void returnJourney(final TransitJourney journey) {
            this.journey = Optional.of(journey);
        }

        void reset() {
            journey = Optional.empty();
            request = Optional.empty();
        }

        Optional<TransitRequest> request() {
            return request;
        }
    }
}
