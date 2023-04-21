package org.optaplanner.persistence.jsonb.api;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbException;

public abstract class AbstractJsonbJsonAdapterTest {

    // ************************************************************************
    // Helper methods
    // ************************************************************************

    protected static <W> W serializeAndDeserialize(W input) {
        return serializeAndDeserialize(JsonbBuilder.create(), input);
    }

    protected static <W> W serializeAndDeserialize(Jsonb jsonb, W input) {
        String jsonString;
        W output;
        try {
            jsonString = jsonb.toJson(input);
            output = (W) jsonb.fromJson(jsonString, input.getClass());
        } catch (JsonbException e) {
            throw new IllegalStateException("Marshalling or unmarshalling for input (" + input + ") failed.", e);
        }

        return output;
    }
}
