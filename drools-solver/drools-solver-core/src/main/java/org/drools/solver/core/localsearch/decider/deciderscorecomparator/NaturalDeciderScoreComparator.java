package org.drools.solver.core.localsearch.decider.deciderscorecomparator;

import java.util.Comparator;

import org.drools.solver.core.score.Score;

/**
 * Compares by the natural order of scores.
 * @author Geoffrey De Smet
 */
public class NaturalDeciderScoreComparator implements Comparator<Score> {

    public int compare(Score score1, Score score2) {
        return score1.compareTo(score2);
    }

}
