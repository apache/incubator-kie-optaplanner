package org.drools.solver.core.score.calculator;

import java.io.Serializable;

/**
 * Makes it easier to implement a ScoreCalculator.
 * @author Geoffrey De Smet
 */
public abstract class AbstractScoreCalculator implements ScoreCalculator, Serializable {

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    public double calculateDecisionScore() {
        return calculateStepScore();
    }

}
