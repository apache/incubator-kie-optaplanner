package org.drools.solver.core.score.calculator;

import org.drools.solver.core.score.Score;
import org.drools.solver.core.score.DefaultHardAndSoftScore;

/**
 * @author Geoffrey De Smet
 */
public class DefaultHardAndSoftConstraintScoreCalculator extends AbstractScoreCalculator
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

    public Score calculateScore() {
        return DefaultHardAndSoftScore.valueOf(-hardConstraintsBroken, -softConstraintsBroken);
    }

}
