package org.drools.solver.core.localsearch.bestsolution;

import org.drools.solver.core.localsearch.LocalSearchSolver;
import org.drools.solver.core.localsearch.LocalSearchSolverAware;
import org.drools.solver.core.localsearch.LocalSearchSolverLifecycleListener;
import org.drools.solver.core.localsearch.LocalSearchSolverScope;
import org.drools.solver.core.localsearch.StepScope;
import org.drools.solver.core.score.Score;
import org.drools.solver.core.event.SolverEventSupport;
import org.drools.solver.core.solution.Solution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A BestSolutionRecaller remembers the best solution that the LocalSearchSolver encounters.
 * @author Geoffrey De Smet
 */
public class BestSolutionRecaller implements LocalSearchSolverAware, LocalSearchSolverLifecycleListener {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    protected LocalSearchSolver localSearchSolver;
    protected SolverEventSupport solverEventSupport;

    public void setLocalSearchSolver(LocalSearchSolver localSearchSolver) {
        this.localSearchSolver = localSearchSolver;
    }

    public void setSolverEventSupport(SolverEventSupport solverEventSupport) {
        this.solverEventSupport = solverEventSupport;
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    public void solvingStarted(LocalSearchSolverScope localSearchSolverScope) {
        Score initialScore = localSearchSolverScope.getStartingScore();
        logger.info("Initialization time spend ({}) for score ({}). Updating best solution and best score.",
                localSearchSolverScope.calculateTimeMillisSpend(), initialScore);
        localSearchSolverScope.setBestSolutionStepIndex(-1);
        localSearchSolverScope.setBestSolution(localSearchSolverScope.getWorkingSolution().cloneSolution());
        localSearchSolverScope.setBestScore(initialScore);
    }

    public void beforeDeciding(StepScope stepScope) {
    }

    public void stepDecided(StepScope stepScope) {
    }

    public void stepTaken(StepScope stepScope) {
        LocalSearchSolverScope localSearchSolverScope = stepScope.getLocalSearchSolverScope();
        Score newScore = stepScope.getScore();
        Score bestScore = localSearchSolverScope.getBestScore();
        if (newScore.compareTo(bestScore) > 0) {
            logger.info("New score ({}) is better then last best score ({}). Updating best solution and best score.",
                    newScore, bestScore);
            localSearchSolverScope.setBestSolutionStepIndex(stepScope.getStepIndex());
            Solution newBestSolution = stepScope.createOrGetClonedSolution();
            localSearchSolverScope.setBestSolution(newBestSolution);
            localSearchSolverScope.setBestScore(newBestSolution.getScore());
            solverEventSupport.fireBestSolutionChanged(newBestSolution);
        } else {
            logger.info("New score ({}) is not better then last best score ({}).", newScore, bestScore);
        }
    }

    public void solvingEnded(LocalSearchSolverScope localSearchSolverScope) {
    }

}
