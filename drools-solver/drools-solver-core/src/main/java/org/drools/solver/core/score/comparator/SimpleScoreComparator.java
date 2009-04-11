package org.drools.solver.core.score.comparator;

import java.io.Serializable;

import org.drools.solver.core.score.Score;
import org.drools.solver.core.score.SimpleScore;
import org.drools.solver.core.score.DefaultSimpleScore;

/**
 * A SimpleScoreComparator is a ScoreComparator for SimpleScores.
 * @see SimpleScore
 * @author Geoffrey De Smet
 */
public class SimpleScoreComparator extends AbstractScoreComparator<SimpleScore> {

    private SimpleScore perfectScore = new DefaultSimpleScore(0);
    private SimpleScore worstScore = new DefaultSimpleScore(Integer.MIN_VALUE);

    public void setPerfectScore(SimpleScore perfectScore) {
        this.perfectScore = perfectScore;
    }

    public void setWorstScore(SimpleScore worstScore) {
        this.worstScore = worstScore;
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    public int compare(SimpleScore score1, SimpleScore score2) {
        return score1.compareTo(score2);
    }

    public boolean better(SimpleScore score1, SimpleScore score2) {
        return score1.getScore() > score2.getScore();
    }

    public boolean betterOrEqual(SimpleScore score1, SimpleScore score2) {
        return score1.getScore() >= score2.getScore();
    }

    public boolean equal(SimpleScore score1, SimpleScore score2) {
        return score1.getScore() == score2.getScore();
    }

    public boolean notEqual(SimpleScore score1, SimpleScore score2) {
        return score1.getScore() != score2.getScore();
    }

    public boolean worse(SimpleScore score1, SimpleScore score2) {
        return score1.getScore() < score2.getScore();
    }

    public boolean worseOrEqual(SimpleScore score1, SimpleScore score2) {
        return score1.getScore() <= score2.getScore();
    }

    public int compareWithShiftingPenalty(SimpleScore score1, SimpleScore score2) {
        // TODO implement me
        return compare(score1, score2);
    }

    public SimpleScore getPerfectScore() {
        return perfectScore;
    }

    public SimpleScore getWorstScore() {
        return worstScore;
    }

    public double calculateTimeGradient(SimpleScore startScore, SimpleScore endScore, SimpleScore score) {
        double timeGradient = 0.0;
        int totalScore = Math.max(0, endScore.getScore() - startScore.getScore());
        if (totalScore > 0) {
            int deltaScore = Math.max(0, score.getScore() - startScore.getScore());
            timeGradient = Math.min(1.0, (double) deltaScore / (double) totalScore);
        }
        return timeGradient;
    }

}