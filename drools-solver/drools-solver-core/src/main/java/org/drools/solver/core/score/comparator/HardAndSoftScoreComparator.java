package org.drools.solver.core.score.comparator;

import java.io.Serializable;

import org.drools.solver.core.score.Score;
import org.drools.solver.core.score.HardAndSoftScore;
import org.drools.solver.core.score.DefaultHardAndSoftScore;

/**
 * A HardAndSoftScoreComparator is a ScoreComparator for HardAndSoftScores.
 * @see HardAndSoftScore
 * @author Geoffrey De Smet
 */
public class HardAndSoftScoreComparator extends AbstractScoreComparator<HardAndSoftScore> {
    
    private double hardScoreTimeGradientWeight = 0.5;

    public void setHardScoreTimeGradientWeight(double hardScoreTimeGradientWeight) {
        this.hardScoreTimeGradientWeight = hardScoreTimeGradientWeight;
        if (hardScoreTimeGradientWeight < 0.0 || hardScoreTimeGradientWeight > 1.0) {
            throw new IllegalArgumentException("Property hardScoreTimeGradientWeight (" + hardScoreTimeGradientWeight
                    + ") must be greater or equal to 0.0jg and smaller or equal to 1.0.");
        }
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    public int compare(HardAndSoftScore score1, HardAndSoftScore score2) {
        return score1.compareTo(score2);
    }

    public boolean better(HardAndSoftScore score1, HardAndSoftScore score2) {
        return (score1.getHardScore() > score2.getHardScore())
                || (score1.getHardScore() == score2.getHardScore() && score1.getSoftScore() > score2.getSoftScore());
    }

    public boolean betterOrEqual(HardAndSoftScore score1, HardAndSoftScore score2) {
        return (score1.getHardScore() > score2.getHardScore())
                || (score1.getHardScore() == score2.getHardScore() && score1.getSoftScore() >= score2.getSoftScore());
    }

    public boolean equal(HardAndSoftScore score1, HardAndSoftScore score2) {
        return score1.getHardScore() == score2.getHardScore() && score1.getSoftScore() == score2.getSoftScore();
    }

    public boolean notEqual(HardAndSoftScore score1, HardAndSoftScore score2) {
        return score1.getHardScore() != score2.getHardScore() || score1.getSoftScore() != score2.getSoftScore();
    }

    public boolean worse(HardAndSoftScore score1, HardAndSoftScore score2) {
        return (score1.getHardScore() < score2.getHardScore())
                || (score1.getHardScore() == score2.getHardScore() && score1.getSoftScore() < score2.getSoftScore());
    }

    public boolean worseOrEqual(HardAndSoftScore score1, HardAndSoftScore score2) {
        return (score1.getHardScore() < score2.getHardScore())
                || (score1.getHardScore() == score2.getHardScore() && score1.getSoftScore() <= score2.getSoftScore());
    }

    public int compareWithShiftingPenalty(HardAndSoftScore score1, HardAndSoftScore score2) {
        // TODO implement me
        return compare(score1, score2);
    }

    public double calculateTimeGradient(HardAndSoftScore startScore, HardAndSoftScore endScore,
            HardAndSoftScore score) {
        double timeGradient = 0.0;
        int totalHardScore = Math.max(0, endScore.getHardScore() - startScore.getHardScore());
        if (totalHardScore > 0) {
            int deltaHardScore = Math.max(0, score.getHardScore() - startScore.getHardScore());
            double hardTimeGradient = Math.min(1.0, (double) deltaHardScore / (double) totalHardScore);
            timeGradient += (hardTimeGradient * hardScoreTimeGradientWeight);
        }
        int totalSoftScore = Math.max(0, endScore.getSoftScore() - startScore.getSoftScore());
        if (totalSoftScore > 0) {
            int deltaSoftScore = Math.max(0, score.getSoftScore() - startScore.getSoftScore());
            double softTimeGradient = Math.min(1.0, (double) deltaSoftScore / (double) totalSoftScore);
            timeGradient += (softTimeGradient * (1.0 - hardScoreTimeGradientWeight)); 
        }
        return timeGradient;
    }

}