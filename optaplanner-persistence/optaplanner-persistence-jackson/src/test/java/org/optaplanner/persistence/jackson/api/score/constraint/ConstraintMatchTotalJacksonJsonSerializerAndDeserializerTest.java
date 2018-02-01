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
import org.optaplanner.core.api.score.constraint.ConstraintMatchTotal;
import org.optaplanner.persistence.jackson.api.score.ScoreJacksonJsonSerializer;
import org.optaplanner.persistence.jackson.api.score.buildin.hardsoft.HardSoftScoreJacksonJsonDeserializer;

import static org.junit.Assert.assertEquals;

public class ConstraintMatchTotalJacksonJsonSerializerAndDeserializerTest {

    private ObjectMapper mapper;

    @Before
    public void setUp() throws Exception {
        mapper = new ObjectMapper();
        mapper.registerModule(new SimpleModule()
                .addSerializer(ConstraintMatch.class, new ConstraintMatchJacksonJsonSerializer())
                .addDeserializer(ConstraintMatch.class, new ConstraintMatchJacksonJsonDeserializer())
                .addSerializer(HardSoftScore.class, new ScoreJacksonJsonSerializer<HardSoftScore>())
                .addDeserializer(HardSoftScore.class, new HardSoftScoreJacksonJsonDeserializer()));
    }

    private void assertSerializeAndDeserialize(String packageName, String name, Score zeroScore, Score... scores)
            throws IOException {
        ConstraintMatchTotal constraintMatchTotal = new ConstraintMatchTotal(packageName, name, zeroScore);
        int i = 0;
        for (Score score : scores) {
            constraintMatchTotal.addConstraintMatch(Arrays.asList(i), score);
            i++;
        }
        TestConstraintMatchTotalWrapper wrapper = new TestConstraintMatchTotalWrapper(constraintMatchTotal);

        String json = mapper.writeValueAsString(wrapper);
        mapper.writeValueAsString(wrapper);

        TestConstraintMatchTotalWrapper other = mapper.readValue(json, TestConstraintMatchTotalWrapper.class);

        assertEquals(constraintMatchTotal, other.getConstraintMatchTotal());
    }

    @Test
    public void testSerializeAndDeserialize() throws IOException {
        assertSerializeAndDeserialize("package", "name", S(0, 0));
        assertSerializeAndDeserialize("package", "name", S(0, 0), S(0, 1));
        assertSerializeAndDeserialize("package", "name", S(0, 0), S(0, 1), S(1, 0), S(1, 1));
    }

    private static HardSoftScore S(int h, int s) {
        return HardSoftScore.valueOf(h, s);
    }

    public static class TestConstraintMatchTotalWrapper {

        @JsonSerialize(using = ConstraintMatchTotalJacksonJsonSerializer.class)
        @JsonDeserialize(using = ConstraintMatchTotalJacksonJsonDeserializer.class)
        ConstraintMatchTotal constraintMatchTotal;

        @SuppressWarnings("unused")
        private TestConstraintMatchTotalWrapper() {
        }

        public TestConstraintMatchTotalWrapper(ConstraintMatchTotal constraintMatchTotal) {
            this.constraintMatchTotal = constraintMatchTotal;
        }

        public ConstraintMatchTotal getConstraintMatchTotal() {
            return constraintMatchTotal;
        }

        public void setConstraintMatchTotal(ConstraintMatchTotal constraintMatchTotal) {
            this.constraintMatchTotal = constraintMatchTotal;
        }
    }
}
