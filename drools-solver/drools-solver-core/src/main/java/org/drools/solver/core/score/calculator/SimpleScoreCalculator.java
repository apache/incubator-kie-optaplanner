package org.drools.solver.core.score.calculator;

/**
 * @author Geoffrey De Smet
 */
public class SimpleScoreCalculator extends AbstractScoreCalculator {

    private double score;

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    public double calculateStepScore() {
        return score;
    }

}
