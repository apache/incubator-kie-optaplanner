package org.optaplanner.persistence.jackson.api.score.constraint;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.optaplanner.core.api.score.constraint.ConstraintMatchTotal;
import org.optaplanner.persistence.jackson.api.score.SerializationUtils;

public class ConstraintMatchTotalJacksonJsonSerializer extends JsonSerializer<ConstraintMatchTotal> {

    @Override
    public void serialize(ConstraintMatchTotal constraintMatchTotal, JsonGenerator generator,
            SerializerProvider serializers)
            throws IOException, JsonProcessingException {
        generator.writeStartObject();

        generator.writeStringField("constraintPackage", constraintMatchTotal.getConstraintPackage());
        generator.writeStringField("constraintName", constraintMatchTotal.getConstraintName());
        SerializationUtils.writePolymorphicProperty(generator, serializers, "scoreTotal", constraintMatchTotal
                .getScoreTotal());

        generator.writeArrayFieldStart("constraintMatchSet");
        for (Object constraintMatch : constraintMatchTotal.getConstraintMatchSet()) {
            generator.writeObject(constraintMatch);
        }
        generator.writeEndArray();

        generator.writeEndObject();
    }

}
