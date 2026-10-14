package nl.codestar.myvibedapp.commute.application;

import java.util.Optional;
import nl.codestar.myvibedapp.commute.domain.TransitJourney;

@FunctionalInterface
public interface TransitProvider {

    Optional<TransitJourney> findJourney(TransitRequest request);
}
