package org.drools.solver.core.score.definition;

import org.drools.solver.core.score.Score;
import org.drools.solver.core.score.comparator.ScoreComparator;

/**
 * A ScoreDefinition knows how to compare scores and what the perfect maximum/minimum Score is.
 * @see AbstractScoreDefinition
 * @see HardAndSoftScoreDefinition
 * @author Geoffrey De Smet
 */
public interface ScoreDefinition<S extends Score> {

//    ScoreComparator getScoreComparator();
//
//    ScoreComparator getShiftingPenaltyScoreComparator();

    /**
     * The perfect maximum score is the score of which there is no better in any problem instance.
     * This doesn't mean that the current problem instance, or any problem instance for that matter,
     * could ever attain that score.
     * </p>
     * For example, most cases have a perfect maximum score of zero, as most use cases only have negative scores.
     * @return null if not supported
     */
    S getPerfectMaximumScore();

    /**
     * The perfect minimum score is the score of which there is no worser in any problem instance.
     * This doesn't mean that the current problem instance, or any problem instance for that matter,
     * could ever attain such a bad score.
     * </p>
     * For example, most cases have a perfect minimum score of negative infinity.
     * @return null if not supported
     */
    S getPerfectMinimumScore();

    /**
     * Parses the String and returns a Score.
     * @param scoreString never null
     * @return never null
     */
    Score parseScore(String scoreString);

    /**
     * @TODO rename because
     *       org.drools.solver.core.localsearch.decider.accepter.simulatedannealing.SimulatedAnnealingAccepter
     *       also uses it
     * @param startScore never null
     * @param endScore never null
     * @param score never null
     * @return between 0.0 and 1.0
     */
    double calculateTimeGradient(S startScore, S endScore, S score);

}
