package org.drools.solver.core.score.calculator;

import org.drools.solver.core.score.Score;
import org.drools.solver.core.score.DefaultSimpleScore;

/**
 * @author Geoffrey De Smet
 */
public class SimpleScoreCalculator extends AbstractScoreCalculator {

    private int score;

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    public Score calculateScore() {
        return DefaultSimpleScore.valueOf(score);
    }

}
