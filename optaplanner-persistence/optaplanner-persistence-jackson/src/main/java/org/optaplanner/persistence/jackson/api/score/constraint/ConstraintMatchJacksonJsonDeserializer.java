package org.optaplanner.persistence.jackson.api.score.constraint;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.constraint.ConstraintMatch;
import org.optaplanner.persistence.jackson.api.score.DeserializationUtils;

public class ConstraintMatchJacksonJsonDeserializer extends JsonDeserializer<ConstraintMatch> {

    @Override
    public ConstraintMatch deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        JsonNode tree = parser.getCodec().readTree(parser);

        String constraintPackage = tree.get("constraintPackage").asText();
        String constraintName = tree.get("constraintName").asText();

        Score<?> score = DeserializationUtils.deserializePolymorphicProperty(parser, context, tree, "score");

        List<Object> justificationList = new ArrayList<>();
        for (JsonNode t : tree.get("justificationList")) {
            justificationList.add(DeserializationUtils.deserializePolymorphicProperty(parser, context, t,
                    "justification"));
        }
        return new ConstraintMatch(constraintPackage, constraintName, justificationList, score);
    }

}
