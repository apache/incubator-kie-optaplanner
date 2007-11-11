package org.drools.solver.core.score.calculator;

/**
 * @author Geoffrey De Smet
 */
public interface HardAndSoftConstraintScoreCalculator extends ScoreCalculator {

    int getHardConstraintsBroken();

    void setHardConstraintsBroken(int hardConstraintsBroken);

    int getSoftConstraintsBroken();

    void setSoftConstraintsBroken(int softConstraintsBroken);
    
}
