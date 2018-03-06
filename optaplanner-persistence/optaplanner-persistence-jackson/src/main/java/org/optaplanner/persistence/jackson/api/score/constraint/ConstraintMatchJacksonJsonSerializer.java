package org.optaplanner.persistence.jackson.api.score.constraint;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.optaplanner.core.api.score.constraint.ConstraintMatch;
import org.optaplanner.persistence.jackson.api.score.SerializationUtils;

public class ConstraintMatchJacksonJsonSerializer extends JsonSerializer<ConstraintMatch> {

    @Override
    public void serialize(ConstraintMatch constraintMatch, JsonGenerator generator, SerializerProvider serializers)
            throws IOException,
            JsonProcessingException {
        generator.writeStartObject();
        generator.writeStringField("constraintPackage", constraintMatch.getConstraintPackage());
        generator.writeStringField("constraintName", constraintMatch.getConstraintName());
        SerializationUtils.writePolymorphicProperty(generator, serializers, "score", constraintMatch.getScore());
        generator.writeArrayFieldStart("justificationList");
        for (Object justification : constraintMatch.getJustificationList()) {
            generator.writeStartObject();

            SerializationUtils.writePolymorphicProperty(generator, serializers, "justification", justification);

            generator.writeEndObject();
        }
        generator.writeEndArray();
        generator.writeEndObject();
    }

}
