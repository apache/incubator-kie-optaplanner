package org.drools.planner.core.localsearch.decider.acceptor.greatdeluge;

import org.drools.planner.core.localsearch.LocalSearchSolverScope;
import org.drools.planner.core.localsearch.StepScope;
import org.drools.planner.core.localsearch.decider.MoveScope;
import org.drools.planner.core.localsearch.decider.acceptor.AbstractAcceptor;
import org.drools.planner.core.score.Score;

/**
 * TODO Under construction. Feel free to create a patch to improve this acceptor!
 * @author Geoffrey De Smet
 */
public class GreatDelugeAcceptor extends AbstractAcceptor {

    protected final double waterLevelUpperBoundRate;
    protected final double waterRisingRate;
    // TODO lowerboundRate when waterLevel rises on every MoveScope (not just every step) to reset waterlevel to upperbound
//    protected final double waterLevelLowerBoundRate;

    protected Score waterLevelScore = null;

    public GreatDelugeAcceptor(double waterLevelUpperBoundRate, double waterRisingRate) {
        this.waterLevelUpperBoundRate = waterLevelUpperBoundRate;
        this.waterRisingRate = waterRisingRate;
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
        waterLevelScore = localSearchSolverScope.getBestScore().multiply(waterLevelUpperBoundRate);
        Score perfectMaximumScore = localSearchSolverScope.getScoreDefinition().getPerfectMaximumScore();
        if (waterLevelScore.compareTo(perfectMaximumScore) > 0) {
            throw new IllegalArgumentException("The waterLevelScore (" + waterLevelScore
                    + ") should not be higher than the perfectMaximumScore(" + perfectMaximumScore + ").");
        }
    }

    public double calculateAcceptChance(MoveScope moveScope) {
        if (moveScope.getScore().compareTo(waterLevelScore) >= 0) {
            return 1.0;
        } else {
            return 0.0;
        }
    }

    @Override
    public void stepTaken(StepScope stepScope) {
        if (stepScope.getStepIndex() == stepScope.getLocalSearchSolverScope().getBestSolutionStepIndex()) {
            // New best score
            waterLevelScore = stepScope.getLocalSearchSolverScope().getBestScore().multiply(waterLevelUpperBoundRate);
        } else {
            Score perfectMaximumScore = stepScope.getLocalSearchSolverScope().getScoreDefinition()
                    .getPerfectMaximumScore();
            Score waterLevelAugend = perfectMaximumScore.substract(waterLevelScore).multiply(waterRisingRate);
            waterLevelScore = waterLevelScore.add(waterLevelAugend);
            // TODO maybe if waterlevel is higher than bestScore, than ...
        }
    }

}