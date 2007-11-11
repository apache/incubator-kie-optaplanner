package org.drools.solver.core.localsearch.decider.accepter.simulatedannealing;

import org.drools.solver.core.localsearch.decider.accepter.TimeGradientBasedAccepter;
import org.drools.solver.core.move.Move;

/**
 * @author Geoffrey De Smet
 */
public class SimulatedAnnealingAccepter extends TimeGradientBasedAccepter {

    protected double scoreDeltaNormalizer = 10000.0;
    protected boolean compareToBestScore = false;

    protected double cachedAcceptChancePart;

    public void setScoreDeltaNormalizer(double scoreDeltaNormalizer) {
        this.scoreDeltaNormalizer = scoreDeltaNormalizer;
    }

    public void setCompareToBestScore(boolean compareToBestScore) {
        this.compareToBestScore = compareToBestScore;
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    public void beforeDeciding() {
        super.beforeDeciding();
        cachedAcceptChancePart = timeGradient / scoreDeltaNormalizer;
    }

    public double calculateAcceptChance(Move move, double score) {
        double compareScore = compareToBestScore
                ? localSearchSolver.getBestScore()
                : localSearchSolver.getStepScore();
        double scoreDelta = score - compareScore;
        if (scoreDelta > 0.0) { // TODO if scoreDelta 0 then it will end up 1.0 anyway?
            return 1.0;
        } else {
            double acceptChance = Math.exp(scoreDelta * cachedAcceptChancePart);
//            double acceptChance = Math.min(Math.exp(scoreDelta / scoreDeltaNormalizer), 1.0) * (1.0 - timeGradient);
            // Math.min(acceptChance, 1.0) is oboselete because scoreDelta <= 0.0
            return acceptChance;
        }
    }
    
}
