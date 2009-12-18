package org.drools.planner.core.solution;

import java.util.Collection;

import org.drools.planner.core.score.Score;
import org.drools.planner.core.Solver;

/**
 * A Solution represents a problem and a possible solution of that problem.
 * A possible solution does not need to optimal or even feasible.
 * A Solution does not even have to be initialized with a possible solution.
 * <p/>
 * A Solution is mutable.
 * Drools Planner will continuously modify the same solution for scalability reasons
 * and clone it to recall the best solution.
 * @author Geoffrey De Smet
 */
public interface Solution {

    /**
     * Returns the Score of this Solution.
     * @return null if the Solution is uninitialized
     * or the last calculated Score is dirty the new Score has not yet been recalculated
     */
    Score getScore();

    /**
     * Called by the {@link Solver} when the Score of this Solution has been calculated.
     * @param score null if the Solution has changed and the new Score has not yet been recalculated
     */
    void setScore(Score score);

    /**
     * Called by the {@link Solver} when the solution needs to be asserted into an empty WorkingMemory.
     * These facts can be used by the score rules.
     * @return never null (although an empty collection is allowed), all the facts of this solution
     */
    Collection<? extends Object> getFacts();

    /**
     * Called by the {@link Solver} when the solution needs to be cloned,
     * for example to store a clone of the current solution as the best solution.
     * <p/>
     * A clone must also shallow copy the score.
     * @return never null, a clone of which the properties that change during solving are deep cloned
     */
    Solution cloneSolution();

}
