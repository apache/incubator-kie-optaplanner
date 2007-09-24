package org.drools.solver.core.evaluation.scorecalculator;

/**
 * Makes it easier to implement a ScoreCalculator.
 * @author Geoffrey De Smet
 */
public abstract class AbstractScoreCalculator implements ScoreCalculator {

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    public double calculateDecisionScore() {
        return calculateStepScore();
    }

}
