package org.drools.solver.core.localsearch.decider.deciderscorecomparator;

import java.util.Comparator;

import org.drools.solver.core.score.Score;

/**
 * Implementation of {@link DeciderScoreComparatorFactory}.
 * Compares by the natural order of scores.
 * @see DeciderScoreComparatorFactory
 * @author Geoffrey De Smet
 */
public class NaturalDeciderScoreComparatorFactory extends AbstractDeciderScoreComparatorFactory {

    private Comparator<Score> naturalDeciderScoreComparator = new NaturalDeciderScoreComparator();

    public Comparator<Score> createDeciderScoreComparator() {
        return naturalDeciderScoreComparator;
    }

}