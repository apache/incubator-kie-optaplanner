package org.drools.planner.core.score.definition;

import org.drools.planner.core.score.SimpleScore;
import org.drools.planner.core.score.DefaultSimpleScore;
import org.drools.planner.core.score.Score;

/**
 * @author Geoffrey De Smet
 */
public class SimpleScoreDefinition extends AbstractScoreDefinition<SimpleScore> {

    private SimpleScore perfectMaximumScore = new DefaultSimpleScore(0);
    private SimpleScore perfectMinimumScore = new DefaultSimpleScore(Integer.MIN_VALUE);

    public void setPerfectMaximumScore(SimpleScore perfectMaximumScore) {
        this.perfectMaximumScore = perfectMaximumScore;
    }

    public void setPerfectMinimumScore(SimpleScore perfectMinimumScore) {
        this.perfectMinimumScore = perfectMinimumScore;
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    public SimpleScore getPerfectMaximumScore() {
        return perfectMaximumScore;
    }

    public SimpleScore getPerfectMinimumScore() {
        return perfectMinimumScore;
    }

    public Score parseScore(String scoreString) {
        return DefaultSimpleScore.parseScore(scoreString);
    }

    public double calculateTimeGradient(SimpleScore startScore, SimpleScore endScore, SimpleScore score) {
        if (score.getScore() >= endScore.getScore()) {
            return 1.0;
        } else if (startScore.getScore() >= score.getScore()) {
            return 0.0;
        }
        int scoreTotal = endScore.getScore() - startScore.getScore();
        int scoreDelta = score.getScore() - startScore.getScore();
        return ((double) scoreDelta) / ((double) scoreTotal);
    }

}
