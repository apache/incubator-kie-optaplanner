package org.drools.solver.core.localsearch.finish;

import org.drools.solver.core.localsearch.LocalSearchSolverScope;
import org.drools.solver.core.localsearch.StepScope;

/**
 * @author Geoffrey De Smet
 */
public class FeasableScoreFinish extends AbstractFinish {

    private double feasableScore;

    private double startingScore;
    private double feasableDelta;
    
    public void setFeasableScore(double feasableScore) {
        this.feasableScore = feasableScore;
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    @Override
    public void solvingStarted(LocalSearchSolverScope localSearchSolverScope) {
        startingScore = localSearchSolverScope.getStartingScore();
        feasableDelta = startingScore - feasableScore;
    }

    public boolean isFinished(StepScope stepScope) {
        double bestScore = stepScope.getLocalSearchSolverScope().getBestScore();
        return bestScore >= feasableScore;
    }

    public double calculateTimeGradient(StepScope stepScope) {
        double stepScore = stepScope.getLocalSearchSolverScope().getLastCompletedStepScope().getScore();
        double stepDelta = startingScore - stepScore;
        double timeGradient = stepDelta / feasableDelta;
        return Math.min(timeGradient, 1.0);
    }
    
}
