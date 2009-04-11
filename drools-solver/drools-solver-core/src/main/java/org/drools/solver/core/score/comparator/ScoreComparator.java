package org.drools.solver.core.score.comparator;

import java.util.Comparator;

import org.drools.solver.core.score.Score;
import org.drools.solver.core.score.HardAndSoftScore;

/**
 * A ScoreComparator can compare 2 Scores, optionally taking shifting penalties into account.
 * <p/>
 * Most implementations are writting specifically for a certain Score implementation.
 * @see Score
 * @see AbstractScoreComparator
 * @see SimpleScoreComparator
 * @see HardAndSoftScoreComparator
 * @author Geoffrey De Smet
 */
public interface ScoreComparator<S extends Score> extends Comparator<S> {

    /**
     * Compares 2 scores. The better score is greater than the other score.
     * This method follows the conventions of {@link Comparator#compare}.
     *
     * @param score1 the first score to be compared, never null.
     * @param score2 the second score to be compared, never null.
     * @return a negative integer, zero, or a positive integer as the
     * 	       first score is worse than, equal to, or better than the
     *	       second score.
     * @see #compareWithShiftingPenalty
     */
    int compare(S score1, S score2);

    boolean better(S score1, S score2);
    boolean betterOrEqual(S score1, S score2);
    boolean equal(S score1, S score2);
    boolean notEqual(S score1, S score2);
    boolean worse(S score1, S score2);
    boolean worseOrEqual(S score1, S score2);

    /**
     * Compares 2 scores, taking a shifting penalty into consideration if a shifting penalty is applied.
     * The better score is greater than the other score.
     * This method follows the conventions of {@link Comparator#compare}.
     * </p>
     * A shifting penalty can for example lower (resp. highten) the weight of a constraint
     * if for a number of steps that constraint is satisfied (resp. not satisfied).
     *
     * @param score1 the first score to be compared, never null.
     * @param score2 the second score to be compared, never null.
     * @return a negative integer, zero, or a positive integer as the
     * 	       first score is worse than, equal to, or better than the
     *	       second score.
     * @see #compare
     */
    int compareWithShiftingPenalty(S score1, S score2);

    /**
     * The perfect score is the score of which there is no better in any problem instance.
     * This doesn't mean that the current problem instance, or any problem instance for that matter,
     * could ever attain that score.
     * </p>
     * For example, most cases have a perfect score of zero, as most use cases only have negative scores.
     * @return null if not supported
     */
    S getPerfectScore();

    /**
     * The worst score is the score of which there is no worser in any problem instance.
     * This doesn't mean that the current problem instance, or any problem instance for that matter,
     * could ever attain such a bad score.
     * </p>
     * For example, most cases have a worst score of negative infinity.
     * @return null if not supported
     */
    S getWorstScore();

    /**
     *
     * @param startScore never null
     * @param endScore never null
     * @param score never null
     * @return between 0.0 and 1.0
     */
    double calculateTimeGradient(S startScore, S endScore, S score);

}
