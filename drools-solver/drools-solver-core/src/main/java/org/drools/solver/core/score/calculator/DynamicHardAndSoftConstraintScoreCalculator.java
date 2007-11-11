package org.drools.solver.core.score.calculator;

/**
 * @author Geoffrey De Smet
 */
public class DynamicHardAndSoftConstraintScoreCalculator extends AbstractHardAndSoftConstraintScoreCalculator {

    private double decisionHardConstraintsWeight;
    private double minHardConstraintsWeight;
    private double maxHardConstraintsWeight;
    private double hardConstraintsWeightStepAdjustment;

    public DynamicHardAndSoftConstraintScoreCalculator() {
        this(1000000.0);
    }

    public DynamicHardAndSoftConstraintScoreCalculator(double startHardConstraintsWeight) {
        this(startHardConstraintsWeight, startHardConstraintsWeight / 1000.0, startHardConstraintsWeight);
    }

    public DynamicHardAndSoftConstraintScoreCalculator(double startHardConstraintsWeight, double minHardConstraintsWeight, double maxHardConstraintsWeight) {
        this(startHardConstraintsWeight, minHardConstraintsWeight, maxHardConstraintsWeight, 1.2);
    }

    public DynamicHardAndSoftConstraintScoreCalculator(double startHardConstraintsWeight,
            double minHardConstraintsWeight, double maxHardConstraintsWeight,
            double hardConstraintsWeightStepAdjustment) {
        if (minHardConstraintsWeight > startHardConstraintsWeight) {
            throw new IllegalArgumentException("minHardConstraintsWeight (" + minHardConstraintsWeight
                    + ") cannot be larger than startHardConstraintsWeight(" + startHardConstraintsWeight + ").");
        }
        if (startHardConstraintsWeight > maxHardConstraintsWeight) {
            throw new IllegalArgumentException("startHardConstraintsWeight (" + startHardConstraintsWeight
                    + ") cannot be larger than maxHardConstraintsWeight(" + maxHardConstraintsWeight + ").");
        }
        this.decisionHardConstraintsWeight = startHardConstraintsWeight;
        this.minHardConstraintsWeight = minHardConstraintsWeight;
        this.maxHardConstraintsWeight = maxHardConstraintsWeight;
        this.hardConstraintsWeightStepAdjustment = hardConstraintsWeightStepAdjustment;
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    public double calculateStepScore() {
        adjustDecisionHardConstraintsWeight();
        return calculateHardAndSoftConstraintScore(maxHardConstraintsWeight); // the step score is always calculated on the maximum
    }

    private void adjustDecisionHardConstraintsWeight() {
        if (hardConstraintsBroken == 0) {
            decisionHardConstraintsWeight /= hardConstraintsWeightStepAdjustment;
            decisionHardConstraintsWeight = Math.max(decisionHardConstraintsWeight, minHardConstraintsWeight);
        } else {
            decisionHardConstraintsWeight *= hardConstraintsWeightStepAdjustment;
            decisionHardConstraintsWeight = Math.min(decisionHardConstraintsWeight, maxHardConstraintsWeight);
        }
    }

    @Override
    public double calculateDecisionScore() {
        return calculateHardAndSoftConstraintScore(decisionHardConstraintsWeight);
    }
    
}
