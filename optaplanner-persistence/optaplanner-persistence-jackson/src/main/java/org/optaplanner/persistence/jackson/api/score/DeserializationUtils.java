package org.optaplanner.persistence.jackson.api.score;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DeserializationUtils {

    @SuppressWarnings("unchecked")
    public static <T> T deserializePolymorphicProperty(JsonParser parser, DeserializationContext context, JsonNode tree,
            String property)
            throws JsonMappingException, JsonProcessingException, IOException {
        if (!tree.has(property) || !tree.has(property + "Class")) {
            throw new IllegalStateException("The tree (" + tree.toString() + ") does not have specified property ("
                    + property + ").");
        }
        if (!tree.has(property + "Class")) {
            throw new IllegalStateException("The tree (" + tree.toString()
                    + ") does not have the class infomation for property (" + property + ").");
        }
        String className = tree.get(property + "Class").asText();
        Class<? extends T> clazz;
        try {
            clazz = (Class<? extends T>) context.findClass(className);
        } catch (ClassNotFoundException e) {
            throw new JsonMappingException("The class (" + className + ") was not found.", e);
        }

        JsonParser propertyParser = parser.getCodec().getFactory().createParser(tree.get(property).toString());
        propertyParser.nextToken();
        return (T) context.findNonContextualValueDeserializer(context.constructType(clazz))
                .deserialize(propertyParser, context);
    }
}
