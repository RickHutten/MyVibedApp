package nl.codestar.myvibedapp.schedule.adapters.in.web;

import java.util.UUID;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import nl.codestar.myvibedapp.schedule.domain.OneOffOverride;
import nl.codestar.myvibedapp.schedule.domain.ScheduleStatus;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class OneOffOverrideRequestMapper {

    static OneOffOverride toDomain(final UUID id, final OneOffOverrideRequest request) {
        final ScheduleStatus status = ScheduleStatus.valueOf(request.status());
        return new OneOffOverride(id, request.startDate(), request.endDate(), status, request.officeId());
    }
}
