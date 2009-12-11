package org.drools.planner.core.localsearch.termination;

import org.drools.planner.core.localsearch.LocalSearchSolverScope;
import org.drools.planner.core.localsearch.StepScope;
import org.drools.planner.core.score.Score;

/**
 * @author Geoffrey De Smet
 */
public class ScoreAttainedTermination extends AbstractTermination {

    private Score scoreAttained;

    public void setScoreAttained(Score scoreAttained) {
        this.scoreAttained = scoreAttained;
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    public boolean isTerminated(StepScope stepScope) {
        Score bestScore = stepScope.getLocalSearchSolverScope().getBestScore();
        return bestScore.compareTo(scoreAttained) >= 0;
    }

    public double calculateTimeGradient(StepScope stepScope) {
        LocalSearchSolverScope localSearchSolverScope = stepScope.getLocalSearchSolverScope();
        Score startingScore = localSearchSolverScope.getStartingScore();
        Score stepScore = localSearchSolverScope.getLastCompletedStepScope().getScore();
        return localSearchSolverScope.getScoreDefinition()
                .calculateTimeGradient(startingScore, scoreAttained, stepScore);
    }

}
