package nl.codestar.myvibedapp.schedule.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class OneOffOverrideTest {

    @Test
    void officeOutcomeRequiresAnOffice() {
        assertThatThrownBy(() -> new OneOffOverride(
                UUID.randomUUID(), LocalDate.of(2026, 10, 7), null, ScheduleStatus.OFFICE, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("An office override requires an office");
    }

    @Test
    void nonOfficeOutcomeCannotReferenceAnOffice() {
        assertThatThrownBy(() -> new OneOffOverride(
                UUID.randomUUID(), LocalDate.of(2026, 10, 7), null, ScheduleStatus.NON_WORKING, UUID.randomUUID()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Only office overrides can reference an office");
    }

    @Test
    void rejectsEndDateBeforeStartDate() {
        assertThatThrownBy(() -> new OneOffOverride(
                UUID.randomUUID(),
                LocalDate.of(2026, 10, 8),
                LocalDate.of(2026, 10, 7),
                ScheduleStatus.NON_WORKING,
                null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Override end date cannot be before its start date");
    }

    @Test
    void omittedEndDateCoversOnlyTheStartDate() {
        final OneOffOverride override = new OneOffOverride(
                UUID.randomUUID(), LocalDate.of(2026, 10, 8), null, ScheduleStatus.NON_WORKING, null);

        assertThat(override.covers(LocalDate.of(2026, 10, 8))).isTrue();
        assertThat(override.covers(LocalDate.of(2026, 10, 9))).isFalse();
    }
}
