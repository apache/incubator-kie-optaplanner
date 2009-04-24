package org.drools.solver.core;

import org.drools.solver.core.solution.Solution;
import org.drools.solver.core.score.Score;

/**
 * A Solver solves planning problems.
 * @author Geoffrey De Smet
 */
public interface Solver {

    void setStartingSolution(Solution startingSolution);

    Score getBestScore();
    Solution getBestSolution();

    /**
     * @return the amount of millis spend since this solver started
     */
    long getTimeMillisSpend();

    void solve();
    
}
