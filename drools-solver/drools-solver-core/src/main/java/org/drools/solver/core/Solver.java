package org.drools.solver.core;

import java.util.Random;

import org.drools.solver.core.evaluation.EvaluationHandler;
import org.drools.solver.core.solution.Solution;

/**
 * @author Geoffrey De Smet
 */
public interface Solver {

    void setStartingSolution(Solution solution);

    Random getRandom();
    @Deprecated
    EvaluationHandler getEvaluationHandler();

    double getBestScore();
    Solution getBestSolution();

    /**
     * @return the amount of millis spend since this solver started
     */
    long getTimeMillisSpend();

    void solve();
    
}
