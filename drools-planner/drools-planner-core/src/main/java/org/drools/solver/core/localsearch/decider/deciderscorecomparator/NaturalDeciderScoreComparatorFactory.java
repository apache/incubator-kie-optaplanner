package org.drools.solver.core.localsearch.decider.deciderscorecomparator;

import java.util.Comparator;

import org.drools.solver.core.score.Score;
import org.drools.solver.core.score.comparator.NaturalScoreComparator;

/**
 * Implementation of {@link DeciderScoreComparatorFactory}.
 * Compares by the natural order of scores.
 * @see DeciderScoreComparatorFactory
 * @author Geoffrey De Smet
 */
public class NaturalDeciderScoreComparatorFactory extends AbstractDeciderScoreComparatorFactory {

    private Comparator<Score> naturalDeciderScoreComparator = new NaturalScoreComparator();

    public Comparator<Score> createDeciderScoreComparator() {
        return naturalDeciderScoreComparator;
    }

}