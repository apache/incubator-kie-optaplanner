package org.optaplanner.examples.tsp.domain.location;

import org.optaplanner.examples.tsp.persistence.TspSolutionFileIO;
import org.optaplanner.examples.vehiclerouting.domain.location.RoadLocation;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.KeyDeserializer;

/**
 * @see TspSolutionFileIO
 */
final class RoadLocationKeyDeserializer extends KeyDeserializer {

    @Override
    public Object deserializeKey(String value, DeserializationContext deserializationContext) {
        return new RoadLocation(Long.parseLong(value)); // Need to be de-duplicated.
    }
}
