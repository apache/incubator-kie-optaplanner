package org.drools.solver.core.localsearch.bestsolution;

import org.drools.solver.core.localsearch.LocalSearchSolver;
import org.drools.solver.core.localsearch.LocalSearchSolverAware;
import org.drools.solver.core.localsearch.LocalSearchSolverLifecycleListener;
import org.drools.solver.core.move.Move;
import org.drools.solver.core.solution.Solution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Geoffrey De Smet
 */
public class BestSolutionRecaller implements LocalSearchSolverAware, LocalSearchSolverLifecycleListener {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

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
        logger.info("Initial score ({}) is starting best score. Updating best solution and best score.", initialScore);
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
            logger.info("New score ({}) is better then last best score ({}). Updating best solution and best score.",
                    newScore, bestScore);
            bestSolutionStepIndex = localSearchSolver.getStepIndex();
            bestSolution = localSearchSolver.getCurrentSolution().cloneSolution();
            bestScore = newScore;
            // TODO BestSolutionChangedEvent
        } else {
            logger.info("New score ({}) is not better then last best score ({}).", newScore, bestScore);
        }
    }

    public void solvingEnded() {
    }

}
