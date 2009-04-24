package org.drools.solver.core.score.definition;

import junit.framework.TestCase;
import org.drools.solver.core.score.Score;
import org.drools.solver.core.score.DefaultHardAndSoftScore;

/**
 * @author Geoffrey De Smet
 */
public class HardAndSoftScoreDefinitionTest extends TestCase {

    public void testCalculateTimeGradient() {
        HardAndSoftScoreDefinition scoreDefinition = new HardAndSoftScoreDefinition();
        scoreDefinition.setHardScoreTimeGradientWeight(0.75);

        // Normal cases
        // Smack in the middle
        assertEquals(0.6, scoreDefinition.calculateTimeGradient(
                DefaultHardAndSoftScore.valueOf(-20,-400), DefaultHardAndSoftScore.valueOf(-10,-300),
                DefaultHardAndSoftScore.valueOf(-14,-340)));
        // No hard broken, total soft broken
        assertEquals(0.75, scoreDefinition.calculateTimeGradient(
                DefaultHardAndSoftScore.valueOf(-20,-400), DefaultHardAndSoftScore.valueOf(-10,-300),
                DefaultHardAndSoftScore.valueOf(-10,-400)));
        // Total hard broken, no soft broken
        assertEquals(0.25, scoreDefinition.calculateTimeGradient(
                DefaultHardAndSoftScore.valueOf(-20,-400), DefaultHardAndSoftScore.valueOf(-10,-300),
                DefaultHardAndSoftScore.valueOf(-20,-300)));
        // No hard broken, more than total soft broken
        assertEquals(0.75, scoreDefinition.calculateTimeGradient(
                DefaultHardAndSoftScore.valueOf(-20,-400), DefaultHardAndSoftScore.valueOf(-10,-300),
                DefaultHardAndSoftScore.valueOf(-10,-900)));
        // More than total hard broken, no soft broken
        assertEquals(0.0, scoreDefinition.calculateTimeGradient(
                DefaultHardAndSoftScore.valueOf(-20,-400), DefaultHardAndSoftScore.valueOf(-10,-300),
                DefaultHardAndSoftScore.valueOf(-90,-300)));

        // Perfect min/max cases
        assertEquals(1.0, scoreDefinition.calculateTimeGradient(
                DefaultHardAndSoftScore.valueOf(-10,-300), DefaultHardAndSoftScore.valueOf(-10,-300),
                DefaultHardAndSoftScore.valueOf(-10,-300)));
        assertEquals(0.0, scoreDefinition.calculateTimeGradient(
                DefaultHardAndSoftScore.valueOf(-20,-400), DefaultHardAndSoftScore.valueOf(-10,-300),
                DefaultHardAndSoftScore.valueOf(-20,-400)));
        assertEquals(1.0, scoreDefinition.calculateTimeGradient(
                DefaultHardAndSoftScore.valueOf(-20,-400), DefaultHardAndSoftScore.valueOf(-10,-300),
                DefaultHardAndSoftScore.valueOf(-10,-300)));

        // Hard total delta is 0
        assertEquals(0.6, scoreDefinition.calculateTimeGradient(
                DefaultHardAndSoftScore.valueOf(-10,-400), DefaultHardAndSoftScore.valueOf(-10,-300),
                DefaultHardAndSoftScore.valueOf(-10,-340)));
        assertEquals(0.0, scoreDefinition.calculateTimeGradient(
                DefaultHardAndSoftScore.valueOf(-10,-400), DefaultHardAndSoftScore.valueOf(-10,-300),
                DefaultHardAndSoftScore.valueOf(-20,-340)));
        assertEquals(1.0, scoreDefinition.calculateTimeGradient(
                DefaultHardAndSoftScore.valueOf(-10,-400), DefaultHardAndSoftScore.valueOf(-10,-300),
                DefaultHardAndSoftScore.valueOf(-0,-340)));

        // Soft total delta is 0
        assertEquals((0.6 * 0.75) + 0.25, scoreDefinition.calculateTimeGradient(
                DefaultHardAndSoftScore.valueOf(-20,-300), DefaultHardAndSoftScore.valueOf(-10,-300),
                DefaultHardAndSoftScore.valueOf(-14,-300)));
        assertEquals(0.6 * 0.75, scoreDefinition.calculateTimeGradient(
                DefaultHardAndSoftScore.valueOf(-20,-300), DefaultHardAndSoftScore.valueOf(-10,-300),
                DefaultHardAndSoftScore.valueOf(-14,-400)));
        assertEquals((0.6 * 0.75) + 0.25, scoreDefinition.calculateTimeGradient(
                DefaultHardAndSoftScore.valueOf(-20,-300), DefaultHardAndSoftScore.valueOf(-10,-300),
                DefaultHardAndSoftScore.valueOf(-14,-0)));
    }

}