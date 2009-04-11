package org.drools.solver.core.localsearch.finish;

import org.drools.solver.core.localsearch.LocalSearchSolverScope;
import org.drools.solver.core.localsearch.StepScope;

/**
 * @author Geoffrey De Smet
 */
public class FeasableScoreFinish extends AbstractFinish {

    private double feasableScore;
    
    public void setFeasableScore(double feasableScore) {
        this.feasableScore = feasableScore;
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    public boolean isFinished(StepScope stepScope) {
        double bestScore = stepScope.getLocalSearchSolverScope().getBestScore();
        return bestScore >= feasableScore;
    }

    public double calculateTimeGradient(StepScope stepScope) {
        LocalSearchSolverScope localSearchSolverScope = stepScope.getLocalSearchSolverScope();
        double startingScore = localSearchSolverScope.getStartingScore();
        double stepScore = localSearchSolverScope.getLastCompletedStepScope().getScore();
        if (feasableScore <= stepScore) {
            return 1.0;
        } else if (stepScore <= startingScore) {
            return 0.0;
        } else {
            return (stepScore - startingScore) / (feasableScore - startingScore);
        }
    }
    
}
