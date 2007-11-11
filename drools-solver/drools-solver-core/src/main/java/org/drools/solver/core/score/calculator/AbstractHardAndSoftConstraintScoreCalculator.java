package org.drools.solver.core.score.calculator;

/**
 * @author Geoffrey De Smet
 */
public abstract class AbstractHardAndSoftConstraintScoreCalculator extends AbstractScoreCalculator
        implements HardAndSoftConstraintScoreCalculator {

    protected int hardConstraintsBroken;
    protected int softConstraintsBroken;

    public int getHardConstraintsBroken() {
        return hardConstraintsBroken;
    }

    public void setHardConstraintsBroken(int hardConstraintsBroken) {
        this.hardConstraintsBroken = hardConstraintsBroken;
    }

    public int getSoftConstraintsBroken() {
        return softConstraintsBroken;
    }

    public void setSoftConstraintsBroken(int softConstraintsBroken) {
        this.softConstraintsBroken = softConstraintsBroken;
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    public double calculateHardAndSoftConstraintScore(double hardConstraintsWeight) {
        double constraintsBroken = ((double) hardConstraintsBroken) * hardConstraintsWeight;
        constraintsBroken += (double) softConstraintsBroken;
        return -constraintsBroken;
    }

}
