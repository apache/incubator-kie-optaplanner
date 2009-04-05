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

    public void setPerfectScore(SimpleScore perfectScore) {
        this.perfectScore = perfectScore;
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

    public double calculateTimeGradient(SimpleScore startScore, SimpleScore endScore, SimpleScore score) {
        double timeGradient = 0.0;
        int totalSoftScore = Math.max(0, endScore.getScore() - startScore.getScore());
        if (totalSoftScore > 0) {
            int softScoreDelta = Math.max(0, score.getScore() - startScore.getScore());
            timeGradient = Math.min(1.0, (double) softScoreDelta / (double) totalSoftScore);
        }
        return timeGradient;
    }

}