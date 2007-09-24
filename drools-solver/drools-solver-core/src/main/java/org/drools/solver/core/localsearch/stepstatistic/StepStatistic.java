package org.drools.solver.core.localsearch.stepstatistic;

import org.drools.solver.core.solution.Solution;

/**
 * @author Geoffrey De Smet
 */
public interface StepStatistic {

    int getBestSolutionStepIndex();
    Solution getBestSolution();
    double getBestScore();

    /**
     * How much of all the selectable moves should be evaluated for the current step.
     * @return a number > 0 and <= 1.0
     */
    double getSelectorThoroughness();

}
