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
import org.optaplanner.core.api.score.constraint.Indictment;
import org.optaplanner.persistence.jackson.api.score.DeserializationUtils;

public class IndictmentJacksonJsonDeserializer extends JsonDeserializer<Indictment> {

    private ConstraintMatchJacksonJsonDeserializer constraintMatchDeserializer;

    public IndictmentJacksonJsonDeserializer() {
        constraintMatchDeserializer = new ConstraintMatchJacksonJsonDeserializer();
    }

    @Override
    public Indictment deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        JsonNode tree = parser.getCodec().readTree(parser);

        Object justification = DeserializationUtils.deserializePolymorphicProperty(parser, context, tree,
                "justification");

        Score<?> scoreTotal = DeserializationUtils.deserializePolymorphicProperty(parser, context, tree, "scoreTotal");

        Indictment out = new Indictment(justification, scoreTotal);

        Set<ConstraintMatch> constraintMatchSet = new HashSet<>();
        for (JsonNode t : tree.get("constraintMatchSet")) {
            constraintMatchSet.add(constraintMatchDeserializer.deserialize(parser.getCodec().getFactory().createParser(t
                    .toString()), context));
        }
        out.getConstraintMatchSet().addAll(constraintMatchSet);

        return out;
    }

}
