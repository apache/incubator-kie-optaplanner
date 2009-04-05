package org.drools.solver.core.score.comparator;

import java.io.Serializable;

import org.drools.solver.core.score.Score;

/**
 * Abstract superclass for {@link ScoreComparator}.
 * @see ScoreComparator
 * @author Geoffrey De Smet
 */
public abstract class AbstractScoreComparator<S extends Score> implements ScoreComparator<S>, Serializable {

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    public int compare(S score1, S score2) {
        return score1.compareTo(score2);
    }

    public int compareWithShiftingPenalty(S score1, S score2) {
        // Hook which can be optionally overwritten by subclasses.
        return compare(score1, score2);
    }

    public S getPerfectScore() {
        // Hook which can be optionally overwritten by subclasses.
        return null;
    }

    public double calculateTimeGradient(S startScore, S endScore, S score) {
        // Hook which can be optionally overwritten by subclasses.
        if (startScore.compareTo(score) <= 0) {
            return 0.0;
        } else if (score.compareTo(endScore) >= 0) {
            return 1.0;
        } else {
            return 0.5;
        }
    }

}