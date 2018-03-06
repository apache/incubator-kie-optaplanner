package org.optaplanner.persistence.jackson.api.score.constraint;

import java.io.IOException;
import java.util.Arrays;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.junit.Before;
import org.junit.Test;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.score.constraint.ConstraintMatch;
import org.optaplanner.persistence.jackson.api.score.ScoreJacksonJsonSerializer;
import org.optaplanner.persistence.jackson.api.score.buildin.hardsoft.HardSoftScoreJacksonJsonDeserializer;

import static org.junit.Assert.assertEquals;

public class ConstraintMatchJacksonJsonSerializerAndDeserializerTest {

    private ObjectMapper mapper;

    @Before
    public void setUp() throws Exception {
        mapper = new ObjectMapper();
        mapper.registerModule(new SimpleModule()
                .addSerializer(HardSoftScore.class, new ScoreJacksonJsonSerializer<HardSoftScore>())
                .addDeserializer(HardSoftScore.class, new HardSoftScoreJacksonJsonDeserializer()));
    }

    private void assertSerializeAndDeserialize(String packageName, String name, Score score, Object... list)
            throws IOException {
        ConstraintMatch constraintMatch = new ConstraintMatch(packageName, name, Arrays.asList(list), score);
        TestConstraintMatchWrapper wrapper = new TestConstraintMatchWrapper(constraintMatch);

        String json = mapper.writeValueAsString(wrapper);

        TestConstraintMatchWrapper other = mapper.readValue(json, TestConstraintMatchWrapper.class);

        assertEquals(constraintMatch, other.getConstraintMatch());
    }

    @Test
    public void testSerializeAndDeserialize() throws IOException {
        assertSerializeAndDeserialize("package", "name", HardSoftScore.valueOf(0, 0));
        assertSerializeAndDeserialize("package", "name", HardSoftScore.valueOf(0, 0), "Justification");
        assertSerializeAndDeserialize("package", "name", HardSoftScore.valueOf(0, 0), "Hello", Integer.valueOf(10));
    }

    public static class TestConstraintMatchWrapper {

        @JsonSerialize(using = ConstraintMatchJacksonJsonSerializer.class)
        @JsonDeserialize(using = ConstraintMatchJacksonJsonDeserializer.class)
        ConstraintMatch constraintMatch;

        @SuppressWarnings("unused")
        private TestConstraintMatchWrapper() {
        }

        public TestConstraintMatchWrapper(ConstraintMatch constraintMatch) {
            this.constraintMatch = constraintMatch;
        }

        public ConstraintMatch getConstraintMatch() {
            return constraintMatch;
        }

        public void setConstraintMatch(ConstraintMatch constraintMatch) {
            this.constraintMatch = constraintMatch;
        }
    }
}
