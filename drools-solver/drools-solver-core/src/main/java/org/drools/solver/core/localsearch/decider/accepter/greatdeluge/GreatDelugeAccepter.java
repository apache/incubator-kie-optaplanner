package org.drools.solver.core.localsearch.decider.accepter.greatdeluge;

import org.drools.solver.core.localsearch.LocalSearchSolverScope;
import org.drools.solver.core.localsearch.StepScope;
import org.drools.solver.core.localsearch.decider.MoveScope;
import org.drools.solver.core.localsearch.decider.accepter.AbstractAccepter;

/**
 * @author Geoffrey De Smet
 */
public class GreatDelugeAccepter extends AbstractAccepter {

    protected final double waterLevelUpperBoundRate;
    protected final double waterRisingRate;
    // TODO lowerboundRate when waterLevel rises on every MoveScope (not just every step) to reset waterlevel to upperbound
//    protected final double waterLevelLowerBoundRate;
    protected final double perfectScore;

    protected double waterLevelScore = Double.NaN;

    public GreatDelugeAccepter(double waterLevelUpperBoundRate, double waterRisingRate) {
        this(waterLevelUpperBoundRate, waterRisingRate, 0.0);
    }

    public GreatDelugeAccepter(double waterLevelUpperBoundRate, double waterRisingRate, double perfectScore) {
        this.waterLevelUpperBoundRate = waterLevelUpperBoundRate;
        this.waterRisingRate = waterRisingRate;
        this.perfectScore = perfectScore;
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    @Override
    public void solvingStarted(LocalSearchSolverScope localSearchSolverScope) {
        if (waterLevelUpperBoundRate < 1.0) {
            throw new IllegalArgumentException("The greatDelugeWaterLevelUpperBoundRate (" + waterLevelUpperBoundRate
                    + ") should be 1.0 or higher.");
        }
        if (waterRisingRate <= 0.0 || waterRisingRate >= 1.0) {
            throw new IllegalArgumentException("The greatDelugeWaterRisingRate (" + waterRisingRate
                    + ") should be between 0.0 and 1.0 (preferably very close to 0.0).");
        }
        waterLevelScore = localSearchSolverScope.getBestScore() * waterLevelUpperBoundRate;
        if (waterLevelScore >= perfectScore) {
            throw new IllegalArgumentException("The waterLevelScore (" + waterLevelScore
                    + ") should be higher than the perfectScore(" + perfectScore + ").");
        }
    }

    public double calculateAcceptChance(MoveScope moveScope) {
        if (moveScope.getScore() >= waterLevelScore) {
            return 1.0;
        }
        return 0.0;
    }

    @Override
    public void stepTaken(StepScope stepScope) {
        if (stepScope.getStepIndex() == stepScope.getLocalSearchSolverScope().getBestSolutionStepIndex()) {
            // New best score
            waterLevelScore = stepScope.getLocalSearchSolverScope().getBestScore() * waterLevelUpperBoundRate;
        } else {
            waterLevelScore += (perfectScore - waterLevelScore) * waterRisingRate;
            // TODO maybe if waterlevel is higher than bestScore, than ...
        }
    }

}