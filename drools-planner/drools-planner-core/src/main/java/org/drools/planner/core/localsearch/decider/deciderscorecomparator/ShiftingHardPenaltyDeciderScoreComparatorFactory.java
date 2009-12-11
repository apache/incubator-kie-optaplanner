package org.drools.planner.core.localsearch.decider.deciderscorecomparator;

import java.util.Comparator;

import org.drools.planner.core.localsearch.StepScope;
import org.drools.planner.core.localsearch.LocalSearchSolverScope;
import org.drools.planner.core.score.HardAndSoftScore;
import org.drools.planner.core.score.Score;
import org.drools.planner.core.score.comparator.NaturalScoreComparator;

/**
 * Implementation of {@link HardAndSoftScore}.
 * Compares by
 * @see DeciderScoreComparatorFactory
 * @author Geoffrey De Smet
 */
public class ShiftingHardPenaltyDeciderScoreComparatorFactory extends AbstractDeciderScoreComparatorFactory {

    private int hardScoreActivationThreshold = 0;
    private int successiveNoHardChangeMinimum = 2;
    private int successiveNoHardChangeMaximum = 20;
    private double successiveNoHardChangeRepetitionMultiplicand = 20.0;
    private double hardWeightSurvivalRatio = 0.8;

    private int startingHardWeight = 1000; // TODO determine dynamically

    private int successiveNoHardScoreChange;
    private boolean shiftingPenaltyActive;
    private int hardWeight;

    private Comparator<Score> naturalDeciderScoreComparator = new NaturalScoreComparator();

    public void setHardScoreActivationThreshold(int hardScoreActivationThreshold) {
        this.hardScoreActivationThreshold = hardScoreActivationThreshold;
    }

    public void setSuccessiveNoHardChangeMinimum(int successiveNoHardChangeMinimum) {
        this.successiveNoHardChangeMinimum = successiveNoHardChangeMinimum;
    }

    public void setSuccessiveNoHardChangeMaximum(int successiveNoHardChangeMaximum) {
        this.successiveNoHardChangeMaximum = successiveNoHardChangeMaximum;
    }

    public void setSuccessiveNoHardChangeRepetitionMultiplicand(double successiveNoHardChangeRepetitionMultiplicand) {
        this.successiveNoHardChangeRepetitionMultiplicand = successiveNoHardChangeRepetitionMultiplicand;
    }

    public void setHardWeightSurvivalRatio(double hardWeightSurvivalRatio) {
        this.hardWeightSurvivalRatio = hardWeightSurvivalRatio;
    }

    public void setStartingHardWeight(int startingHardWeight) {
        this.startingHardWeight = startingHardWeight;
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    @Override
    public void solvingStarted(LocalSearchSolverScope localSearchSolverScope) {
        successiveNoHardScoreChange = 0;
        shiftingPenaltyActive = false;
    }

    @Override
    public void stepTaken(StepScope stepScope) {
        if (stepScope.getStepIndex() == stepScope.getLocalSearchSolverScope().getBestSolutionStepIndex()) {
            successiveNoHardScoreChange = 0;
            shiftingPenaltyActive = false;
        } else {
            HardAndSoftScore lastStepScore = (HardAndSoftScore) stepScope.getLocalSearchSolverScope()
                    .getLastCompletedStepScope().getScore();
            HardAndSoftScore stepScore = (HardAndSoftScore) stepScope.getScore();
            if (stepScore.getHardScore() >= hardScoreActivationThreshold
                    && lastStepScore.getHardScore() == stepScore.getHardScore()) {
                successiveNoHardScoreChange++;
            } else {
                successiveNoHardScoreChange--;
                if (successiveNoHardScoreChange < 0) {
                    successiveNoHardScoreChange = 0;
                }
            }
            int min = successiveNoHardChangeMinimum;
            int max = successiveNoHardChangeMaximum;
            while (true) {
                if (successiveNoHardScoreChange < min) {
                    shiftingPenaltyActive = false;
                    break;
                } else if (successiveNoHardScoreChange <= max) {
                    shiftingPenaltyActive = true;
                    if (successiveNoHardScoreChange == min) {
                        hardWeight = startingHardWeight;
                    } else {
                        hardWeight = (int) Math.round(((double) hardWeight) * hardWeightSurvivalRatio);
                    }
                    break;
                }
                min = (int) Math.round(((double) min) * successiveNoHardChangeRepetitionMultiplicand);
                max = (int) Math.round(((double) max) * successiveNoHardChangeRepetitionMultiplicand);
            }
        }
    }

    public Comparator<Score> createDeciderScoreComparator() {
        if (shiftingPenaltyActive) {
            return new HardPenaltyDeciderScoreComparator(hardWeight);
        } else {
            return naturalDeciderScoreComparator;
        }
    }

}