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
        if (scoreDelta >= 0) {
            return 1.0;
        } else {
            double acceptChance = Math.exp(scoreDelta * cachedAcceptChancePart);
            return Math.min(acceptChance, 1.0);
        }
    }
    
}
