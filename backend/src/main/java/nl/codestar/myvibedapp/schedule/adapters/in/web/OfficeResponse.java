package nl.codestar.myvibedapp.schedule.adapters.in.web;

import java.util.UUID;
import nl.codestar.myvibedapp.schedule.domain.SavedOffice;

record OfficeResponse(
        UUID id,
        String label,
        String address,
        double latitude,
        double longitude,
        boolean deleted) {

    static OfficeResponse from(final SavedOffice office) {
        return new OfficeResponse(office.id(), office.label(), office.address(), office.latitude(), office.longitude(), office.deleted());
    }
}
