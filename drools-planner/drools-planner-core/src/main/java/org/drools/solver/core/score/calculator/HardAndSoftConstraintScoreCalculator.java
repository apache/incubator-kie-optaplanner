package org.drools.solver.core.score.calculator;

/**
 * @TODO rename to HardAndSoftScoreCalculator if it survives the score-in-solution refactor
 * @author Geoffrey De Smet
 */
public interface HardAndSoftConstraintScoreCalculator extends ScoreCalculator {

    int getHardConstraintsBroken();

    void setHardConstraintsBroken(int hardConstraintsBroken);

    int getSoftConstraintsBroken();

    void setSoftConstraintsBroken(int softConstraintsBroken);
    
}
