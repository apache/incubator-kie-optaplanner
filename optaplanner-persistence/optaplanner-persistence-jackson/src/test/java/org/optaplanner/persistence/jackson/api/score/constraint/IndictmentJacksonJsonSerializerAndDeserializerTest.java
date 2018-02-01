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
import org.optaplanner.core.api.score.constraint.Indictment;
import org.optaplanner.persistence.jackson.api.score.ScoreJacksonJsonSerializer;
import org.optaplanner.persistence.jackson.api.score.buildin.hardsoft.HardSoftScoreJacksonJsonDeserializer;

import static org.junit.Assert.assertEquals;

public class IndictmentJacksonJsonSerializerAndDeserializerTest {

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

    private void assertSerializeAndDeserialize(Object cause, Score zeroScore, ConstraintMatch... constraints)
            throws IOException {
        Indictment indictment = new Indictment(cause, zeroScore);
        for (ConstraintMatch constraint : constraints) {
            indictment.addConstraintMatch(constraint);
        }
        TestIndictmentWrapper wrapper = new TestIndictmentWrapper(indictment);

        String json = mapper.writeValueAsString(wrapper);

        TestIndictmentWrapper other = mapper.readValue(json, TestIndictmentWrapper.class);

        assertEquals(indictment, other.getIndictment());
    }

    @Test
    public void testSerializeAndDeserialize() throws IOException {
        assertSerializeAndDeserialize("Hello", S(0, 0));
        assertSerializeAndDeserialize(1, S(0, 1), CM("package", "name", S(0, 10), "Bad"));
        assertSerializeAndDeserialize("What", S(0, 1), CM("package", "name", S(0, 10), "Bad"), CM("package", "b", S(10,
                20),
                "Very Bad"));
    }

    private static HardSoftScore S(int h, int s) {
        return HardSoftScore.valueOf(h, s);
    }

    private static ConstraintMatch CM(String p, String n, Score score, Object... justificationList) {
        return new ConstraintMatch(p, n, Arrays.asList(justificationList), score);
    }

    public static class TestIndictmentWrapper {

        @JsonSerialize(using = IndictmentJacksonJsonSerializer.class)
        @JsonDeserialize(using = IndictmentJacksonJsonDeserializer.class)
        Indictment indictment;

        @SuppressWarnings("unused")
        private TestIndictmentWrapper() {
        }

        public TestIndictmentWrapper(Indictment indictment) {
            this.indictment = indictment;
        }

        public Indictment getIndictment() {
            return indictment;
        }

        public void setIndictment(Indictment indictment) {
            this.indictment = indictment;
        }
    }
}
