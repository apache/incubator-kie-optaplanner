package org.optaplanner.examples.tsp.domain.location;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.optaplanner.examples.vehiclerouting.domain.location.RoadLocation;

import java.io.IOException;

final class RoadLocationKeySerializer extends JsonSerializer<org.optaplanner.examples.vehiclerouting.domain.location.RoadLocation> {

    @Override
    public void serialize(RoadLocation roadLocation, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
            throws IOException {
        jsonGenerator.writeFieldId(roadLocation.getId());
    }
}
