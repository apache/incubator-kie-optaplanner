package org.optaplanner.persistence.jackson.api.score.constraint;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.constraint.ConstraintMatch;
import org.optaplanner.core.api.score.constraint.ConstraintMatchTotal;
import org.optaplanner.persistence.jackson.api.score.DeserializationUtils;

public class ConstraintMatchTotalJacksonJsonDeserializer extends JsonDeserializer<ConstraintMatchTotal> {

    private ConstraintMatchJacksonJsonDeserializer constraintMatchDeserializer;

    public ConstraintMatchTotalJacksonJsonDeserializer() {
        constraintMatchDeserializer = new ConstraintMatchJacksonJsonDeserializer();
    }

    @Override
    public ConstraintMatchTotal deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        JsonNode tree = parser.getCodec().readTree(parser);

        String constraintPackage = tree.get("constraintPackage").asText();
        String constraintName = tree.get("constraintName").asText();

        Score<?> scoreTotal = DeserializationUtils.deserializePolymorphicProperty(parser, context, tree, "scoreTotal");

        ConstraintMatchTotal out = new ConstraintMatchTotal(constraintPackage, constraintName, scoreTotal);

        Set<ConstraintMatch> constraintMatchSet = new HashSet<>();
        for (JsonNode t : tree.get("constraintMatchSet")) {
            constraintMatchSet.add(constraintMatchDeserializer.deserialize(parser.getCodec().getFactory().createParser(t
                    .toString()), context));
        }
        out.getConstraintMatchSet().addAll(constraintMatchSet);

        return out;
    }

}
