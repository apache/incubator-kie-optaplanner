package org.drools.solver.core.solution;

import java.util.Collection;

/**
 * A Solution represents a problem and a possible solution of that problem.
 * A possible solution does not need to optimal or even feasible.
 * A Solution does not even have to be initialized with a possible solution.
 * <p/>
 * A Solution is mutable.
 * Drools-solver will continiously modify the same solution for scalibility reasons
 * and clone it to recall the best solution.
 * @author Geoffrey De Smet
 */
public interface Solution {

    /**
     * Called when the solution needs to be asserted into an empty WorkingMemory.
     * These facts can be used by the score rules.
     * @return all the facts of this solution
     */
    Collection<? extends Object> getFacts();

    /**
     * Called when the solution needs to be cloned during solving,
     * for example to store a clone of the current solution as the best solution. 
     * @return a clone of which the properties that change during solving are deep cloned
     */
    Solution cloneSolution();

}
