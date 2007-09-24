package org.drools.solver.core.localsearch.bestsolution;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.drools.solver.core.localsearch.LocalSearchSolver;
import org.drools.solver.core.localsearch.LocalSearchSolverAware;
import org.drools.solver.core.localsearch.LocalSearchSolverLifecycleListener;
import org.drools.solver.core.move.Move;
import org.drools.solver.core.solution.Solution;

/**
 * @author Geoffrey De Smet
 */
public class BestSolutionRecaller implements LocalSearchSolverAware, LocalSearchSolverLifecycleListener {

    protected final transient Log log = LogFactory.getLog(getClass());

    protected LocalSearchSolver localSearchSolver;

    private int bestSolutionStepIndex;
    private Solution bestSolution;
    private double bestScore;

    public void setLocalSearchSolver(LocalSearchSolver localSearchSolver) {
        this.localSearchSolver = localSearchSolver;
    }

    public int getBestSolutionStepIndex() {
        return bestSolutionStepIndex;
    }

    public Solution getBestSolution() {
        return bestSolution;
    }

    public double getBestScore() {
        return bestScore;
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    public void solvingStarted() {
        double initialScore = localSearchSolver.getStepScore();
        if (log.isInfoEnabled()) {
            log.info("Initial score (" + initialScore + ") is starting best score. "
                    + "Updating best solution and best score.");
        }
        bestSolutionStepIndex = localSearchSolver.getStepIndex();
        bestSolution = localSearchSolver.getCurrentSolution().cloneSolution();
        bestScore = initialScore;
    }

    public void beforeDeciding() {
    }

    public void stepDecided(Move step) {
    }

    public void stepTaken() {
        double newScore = localSearchSolver.getStepScore();
        if (newScore > bestScore) {
            if (log.isInfoEnabled()) {
                log.info("New score (" + newScore + ") is better then last best score (" + bestScore + "). "
                        + "Updating best solution and best score.");
            }
            bestSolutionStepIndex = localSearchSolver.getStepIndex();
            bestSolution = localSearchSolver.getCurrentSolution().cloneSolution();
            bestScore = newScore;
            // TODO BestSolutionChangedEvent
        } else {
            if (log.isInfoEnabled()) {
                log.info("New score (" + newScore + ") is not better then last best score (" + bestScore + ").");
            }
        }
    }

    public void solvingEnded() {
    }

}
