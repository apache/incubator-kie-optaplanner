package org.drools.solver.core.solution;

import java.util.Collection;

/**
 * @author Geoffrey De Smet
 */
public interface Solution {

    Solution cloneSolution();

    /**
     * Called when the solution needs to be asserted into an empty WorkingMemory.
     * @return all the facts of this solution
     */
    Collection<? extends Object> getFacts();

}
