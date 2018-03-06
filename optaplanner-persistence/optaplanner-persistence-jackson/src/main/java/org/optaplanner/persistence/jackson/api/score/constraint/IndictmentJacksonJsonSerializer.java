package org.optaplanner.persistence.jackson.api.score.constraint;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.optaplanner.core.api.score.constraint.Indictment;
import org.optaplanner.persistence.jackson.api.score.SerializationUtils;

public class IndictmentJacksonJsonSerializer extends JsonSerializer<Indictment> {

    @Override
    public void serialize(Indictment indictment, JsonGenerator generator, SerializerProvider serializers)
            throws IOException,
            JsonProcessingException {
        generator.writeStartObject();

        SerializationUtils.writePolymorphicProperty(generator, serializers, "justification", indictment
                .getJustification());
        SerializationUtils.writePolymorphicProperty(generator, serializers, "scoreTotal", indictment.getScoreTotal());

        generator.writeArrayFieldStart("constraintMatchSet");
        for (Object constraintMatch : indictment.getConstraintMatchSet()) {
            generator.writeObject(constraintMatch);
        }
        generator.writeEndArray();
        generator.writeObjectField("constraintMatchSet", indictment.getConstraintMatchSet());

        generator.writeEndObject();
    }

}
