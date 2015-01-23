package org.optaplanner.core.config.score.director;

import org.junit.Ignore;
import org.junit.Test;
import org.optaplanner.core.api.score.buildin.bendable.BendableScore;
import org.optaplanner.core.config.score.definition.ScoreDefinitionType;
import org.optaplanner.core.impl.score.buildin.bendable.BendableScoreDefinition;
import org.optaplanner.core.impl.score.buildin.bendablebigdecimal.BendableBigDecimalScoreDefinition;
import org.optaplanner.core.impl.score.buildin.hardmediumsoft.HardMediumSoftScoreDefinition;
import org.optaplanner.core.impl.score.buildin.hardmediumsoftlong.HardMediumSoftLongScoreDefinition;
import org.optaplanner.core.impl.score.buildin.hardsoft.HardSoftScoreDefinition;
import org.optaplanner.core.impl.score.buildin.hardsoftbigdecimal.HardSoftBigDecimalScoreDefinition;
import org.optaplanner.core.impl.score.buildin.hardsoftdouble.HardSoftDoubleScoreDefinition;
import org.optaplanner.core.impl.score.buildin.hardsoftlong.HardSoftLongScoreDefinition;
import org.optaplanner.core.impl.score.buildin.simple.SimpleScoreDefinition;
import org.optaplanner.core.impl.score.buildin.simplebigdecimal.SimpleBigDecimalScoreDefinition;
import org.optaplanner.core.impl.score.buildin.simpledouble.SimpleDoubleScoreDefinition;
import org.optaplanner.core.impl.score.buildin.simplelong.SimpleLongScoreDefinition;

import static org.junit.Assert.assertTrue;

public class ScoreDirectorFactoryConfigTest {

    @Test
    public void buildScoreDefinition() {

        ScoreDirectorFactoryConfig factory = new ScoreDirectorFactoryConfig();
        assertTrue(factory.buildScoreDefinition() instanceof SimpleScoreDefinition);

        //based on score definition class
        factory.setScoreDefinitionClass(SimpleScoreDefinition.class);
        assertTrue(factory.buildScoreDefinition() instanceof SimpleScoreDefinition);

        factory.setScoreDefinitionClass(SimpleLongScoreDefinition.class);
        assertTrue(factory.buildScoreDefinition() instanceof SimpleLongScoreDefinition);

        factory.setScoreDefinitionClass(SimpleDoubleScoreDefinition.class);
        assertTrue(factory.buildScoreDefinition() instanceof SimpleDoubleScoreDefinition);

        factory.setScoreDefinitionClass(SimpleBigDecimalScoreDefinition.class);
        assertTrue(factory.buildScoreDefinition() instanceof SimpleBigDecimalScoreDefinition);

        factory.setScoreDefinitionClass(HardSoftScoreDefinition.class);
        assertTrue(factory.buildScoreDefinition() instanceof HardSoftScoreDefinition);

        factory.setScoreDefinitionClass(HardSoftLongScoreDefinition.class);
        assertTrue(factory.buildScoreDefinition() instanceof HardSoftLongScoreDefinition);

        factory.setScoreDefinitionClass(HardSoftDoubleScoreDefinition.class);
        assertTrue(factory.buildScoreDefinition() instanceof HardSoftDoubleScoreDefinition);

        factory.setScoreDefinitionClass(HardSoftBigDecimalScoreDefinition.class);
        assertTrue(factory.buildScoreDefinition() instanceof HardSoftBigDecimalScoreDefinition);

        factory.setScoreDefinitionClass(HardMediumSoftScoreDefinition.class);
        assertTrue(factory.buildScoreDefinition() instanceof HardMediumSoftScoreDefinition);

        factory.setScoreDefinitionClass(HardMediumSoftLongScoreDefinition.class);
        assertTrue(factory.buildScoreDefinition() instanceof HardMediumSoftLongScoreDefinition);

        // based on score definition type
        factory = new ScoreDirectorFactoryConfig();

        factory.setScoreDefinitionType(ScoreDefinitionType.SIMPLE);
        assertTrue(factory.buildScoreDefinition() instanceof SimpleScoreDefinition);

        factory.setScoreDefinitionType(ScoreDefinitionType.SIMPLE_LONG);
        assertTrue(factory.buildScoreDefinition() instanceof SimpleLongScoreDefinition);

        factory.setScoreDefinitionType(ScoreDefinitionType.SIMPLE_DOUBLE);
        assertTrue(factory.buildScoreDefinition() instanceof SimpleDoubleScoreDefinition);

        factory.setScoreDefinitionType(ScoreDefinitionType.SIMPLE_BIG_DECIMAL);
        assertTrue(factory.buildScoreDefinition() instanceof SimpleBigDecimalScoreDefinition);

        factory.setScoreDefinitionType(ScoreDefinitionType.HARD_SOFT);
        assertTrue(factory.buildScoreDefinition() instanceof HardSoftScoreDefinition);

        factory.setScoreDefinitionType(ScoreDefinitionType.HARD_SOFT_LONG);
        assertTrue(factory.buildScoreDefinition() instanceof HardSoftLongScoreDefinition);

        factory.setScoreDefinitionType(ScoreDefinitionType.HARD_SOFT_DOUBLE);
        assertTrue(factory.buildScoreDefinition() instanceof HardSoftDoubleScoreDefinition);

        factory.setScoreDefinitionType(ScoreDefinitionType.HARD_SOFT_BIG_DECIMAL);
        assertTrue(factory.buildScoreDefinition() instanceof HardSoftBigDecimalScoreDefinition);

        factory.setScoreDefinitionType(ScoreDefinitionType.HARD_MEDIUM_SOFT);
        assertTrue(factory.buildScoreDefinition() instanceof HardMediumSoftScoreDefinition);

        factory.setScoreDefinitionType(ScoreDefinitionType.HARD_MEDIUM_SOFT_LONG);
        assertTrue(factory.buildScoreDefinition() instanceof HardMediumSoftLongScoreDefinition);

        factory.setScoreDefinitionType(ScoreDefinitionType.BENDABLE);
        factory.setBendableHardLevelsSize(5);
        factory.setBendableSoftLevelsSize(6);
        assertTrue(factory.buildScoreDefinition() instanceof BendableScoreDefinition);

        factory.setScoreDefinitionType(ScoreDefinitionType.BENDABLE_BIG_DECIMAL);
        factory.setBendableHardLevelsSize(5);
        factory.setBendableSoftLevelsSize(6);
        assertTrue(factory.buildScoreDefinition() instanceof BendableBigDecimalScoreDefinition);
    }

    @Test
    public void buildScoreDefinitionIllegalState() {
        boolean thrownException = false;
        try {
            ScoreDirectorFactoryConfig factory = new ScoreDirectorFactoryConfig();
            factory.setScoreDefinitionClass(BendableScoreDefinition.class);
            factory.setBendableHardLevelsSize(4);
            factory.setBendableSoftLevelsSize(2);
            factory.buildScoreDefinition();
        } catch(Exception e) {
            assertTrue(e instanceof IllegalArgumentException);
            thrownException = true;
        }
        assertTrue("Exception was not thrown", thrownException);
        thrownException = false;
        try {
            ScoreDirectorFactoryConfig factory = new ScoreDirectorFactoryConfig();
            factory.setScoreDefinitionClass(SimpleScoreDefinition.class);
            factory.setScoreDefinitionType(ScoreDefinitionType.SIMPLE);
            factory.buildScoreDefinition();
        } catch(Exception e) {
            assertTrue(e.getMessage(), e instanceof IllegalStateException);
            thrownException = true;
        }
        assertTrue("Exception was not thrown", thrownException);
        thrownException = false;
        try {
            ScoreDirectorFactoryConfig factory = new ScoreDirectorFactoryConfig();
            factory.setScoreDefinitionType(ScoreDefinitionType.BENDABLE);
            factory.buildScoreDefinition();
        } catch(Exception e) {
            assertTrue(e instanceof IllegalArgumentException);
            thrownException = true;
        }
        assertTrue("Exception was not thrown", thrownException);
        thrownException = false;
        try {
            ScoreDirectorFactoryConfig factory = new ScoreDirectorFactoryConfig();
            factory.setScoreDefinitionType(ScoreDefinitionType.BENDABLE_BIG_DECIMAL);
            factory.buildScoreDefinition();
        } catch(Exception e) {
            assertTrue(e instanceof IllegalArgumentException);
            thrownException = true;
        }
        assertTrue("Exception was not thrown", thrownException);
    }



}
