package org.drools.planner.core.localsearch.decider;

import org.drools.planner.core.localsearch.LocalSearchSolverAware;
import org.drools.planner.core.localsearch.LocalSearchSolverLifecycleListener;
import org.drools.planner.core.localsearch.StepScope;
import org.drools.planner.core.localsearch.decider.forager.Forager;
import org.drools.planner.core.localsearch.decider.deciderscorecomparator.DeciderScoreComparatorFactory;

/**
 * A decider decides the next step.
 * @see DefaultDecider
 * @author Geoffrey De Smet
 */
public interface Decider extends LocalSearchSolverAware, LocalSearchSolverLifecycleListener {

    /**
     * Decides the next step
     * @param stepScope never null
     */
    void decideNextStep(StepScope stepScope);

    /**
     * @return never null
     */
    DeciderScoreComparatorFactory getDeciderScoreComparator();

    /**
     * @return never null
     */
    Forager getForager();

}
