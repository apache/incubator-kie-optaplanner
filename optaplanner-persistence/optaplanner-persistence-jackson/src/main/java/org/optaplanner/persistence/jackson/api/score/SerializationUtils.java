package org.optaplanner.persistence.jackson.api.score;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;

public class SerializationUtils {

    @SuppressWarnings("unchecked")
    public static void writePolymorphicProperty(JsonGenerator generator, SerializerProvider serializers,
            String fieldName, Object value)
            throws IOException {
        generator.writeStringField(fieldName + "Class", value.getClass().getName());
        generator.writeFieldName(fieldName);
        serializers.findValueSerializer(value.getClass()).serialize(value, generator, serializers);
    }
}
