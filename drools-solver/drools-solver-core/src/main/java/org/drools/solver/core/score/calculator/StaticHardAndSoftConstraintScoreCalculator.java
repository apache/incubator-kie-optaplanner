package org.drools.solver.core.score.calculator;

/**
 * @author Geoffrey De Smet
 */
public class StaticHardAndSoftConstraintScoreCalculator extends AbstractHardAndSoftConstraintScoreCalculator {

    protected double hardConstraintsWeight;

    public StaticHardAndSoftConstraintScoreCalculator() {
        this(1000000.0);
    }

    public StaticHardAndSoftConstraintScoreCalculator(double hardConstraintsWeight) {
        this.hardConstraintsWeight = hardConstraintsWeight;
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    public double calculateStepScore() {
        return calculateHardAndSoftConstraintScore(hardConstraintsWeight);
    }

}
