package org.drools.solver.core.localsearch.finish;

import org.drools.solver.core.localsearch.LocalSearchSolverScope;
import org.drools.solver.core.localsearch.StepScope;
import org.drools.solver.core.score.Score;

/**
 * @author Geoffrey De Smet
 */
public class FeasableScoreFinish extends AbstractFinish {

    private Score feasableScore;
    
    public void setFeasableScore(Score feasableScore) {
        this.feasableScore = feasableScore;
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    public boolean isFinished(StepScope stepScope) {
        Score bestScore = stepScope.getLocalSearchSolverScope().getBestScore();
        return bestScore.compareTo(feasableScore) >= 0;
    }

    public double calculateTimeGradient(StepScope stepScope) {
        LocalSearchSolverScope localSearchSolverScope = stepScope.getLocalSearchSolverScope();
        Score startingScore = localSearchSolverScope.getStartingScore();
        Score stepScore = localSearchSolverScope.getLastCompletedStepScope().getScore();
        return localSearchSolverScope.getScoreDefinition()
                .calculateTimeGradient(startingScore, feasableScore, stepScore);
    }
    
}
