package org.drools.solver.core.localsearch.decider.deciderscorecomparator;

import java.util.Comparator;

import org.drools.solver.core.score.Score;
import org.drools.solver.core.localsearch.LocalSearchSolverLifecycleListener;

/**
 * A DeciderScoreComparatorFactory creates a new DeciderScoreComparator each step,
 * which compares 2 scores to decide the next step.
 * That Score Comparator can consider shifting penalty, aging penalty, ...
 * in which case it differs from the natural ordering of scores.
 * @author Geoffrey De Smet
 */
public interface DeciderScoreComparatorFactory extends LocalSearchSolverLifecycleListener {

    /**
     * @return never null
     */
    Comparator<Score> createDeciderScoreComparator();

}
