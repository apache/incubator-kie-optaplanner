package org.drools.planner.core.score.calculator;

import java.io.Serializable;

/**
 * Makes it easier to implement a ScoreCalculator.
 * @author Geoffrey De Smet
 */
public abstract class AbstractScoreCalculator implements ScoreCalculator, Serializable {

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    public ScoreCalculator clone() {
        try {
            return (ScoreCalculator) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException(e);
        }
    }

}
