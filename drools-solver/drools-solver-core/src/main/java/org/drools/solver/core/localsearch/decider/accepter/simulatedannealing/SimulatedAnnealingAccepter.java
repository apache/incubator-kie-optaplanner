package org.drools.solver.core.localsearch.decider.accepter.simulatedannealing;

import org.drools.solver.core.localsearch.decider.MoveScope;
import org.drools.solver.core.localsearch.decider.accepter.AbstractAccepter;

/**
 * TODO Under construction
 * @author Geoffrey De Smet
 */
public class SimulatedAnnealingAccepter extends AbstractAccepter {

    protected double scoreDeltaNormalizer = 10000.0;
    protected boolean compareToBestScore = false;

    public void setScoreDeltaNormalizer(double scoreDeltaNormalizer) {
        this.scoreDeltaNormalizer = scoreDeltaNormalizer;
    }

    public void setCompareToBestScore(boolean compareToBestScore) {
        this.compareToBestScore = compareToBestScore;
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    @Override
    public double calculateAcceptChance(MoveScope moveScope) {
        double compareScore = compareToBestScore
                ? moveScope.getStepScope().getLocalSearchSolverScope().getBestScore()
                : moveScope.getStepScope().getLocalSearchSolverScope().getLastCompletedStepScope().getScore();
        // TODO Support for decision score
        double scoreDelta = moveScope.getScore() - compareScore;
        if (scoreDelta > 0.0) { // TODO if scoreDelta 0 then it will end up 1.0 anyway?
            return 1.0;
        } else {
            double timeGradient = moveScope.getStepScope().getTimeGradient();
            double acceptChance = Math.exp(scoreDelta * timeGradient / scoreDeltaNormalizer);
//            double acceptChance = Math.min(Math.exp(scoreDelta / scoreDeltaNormalizer), 1.0) * (1.0 - timeGradient);
            // Math.min(acceptChance, 1.0) is oboselete because scoreDelta <= 0.0
            return acceptChance;
        }
    }
    
}
