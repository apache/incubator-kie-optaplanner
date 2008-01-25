package org.drools.solver.core.localsearch;

import org.drools.solver.core.Solver;
import org.drools.solver.core.localsearch.decider.Decider;
import org.drools.solver.core.solution.Solution;

/**
 * @author Geoffrey De Smet
 */
public interface LocalSearchSolver extends Solver {

    /**
     * @return the number of steps already taken.
     */
    int getStepIndex();

    /**
     * @return the score of the last taken step.
     */
    double getStepScore();

    /**
     * @return the solution as it currently is - which might be temperarly modified
     * by a selected move which is being decided upon.
     */
    Solution getCurrentSolution();

    int getBestSolutionStepIndex();

    double calculateTimeGradient();

    Decider getDecider();

}
