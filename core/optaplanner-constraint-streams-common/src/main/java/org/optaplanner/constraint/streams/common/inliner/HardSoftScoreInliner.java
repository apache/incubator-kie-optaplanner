package org.optaplanner.constraint.streams.common.inliner;

import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.score.stream.Constraint;

final class HardSoftScoreInliner extends AbstractScoreInliner<HardSoftScore> {

    private int hardScore;
    private int softScore;

    HardSoftScoreInliner(boolean constraintMatchEnabled) {
        super(constraintMatchEnabled);
    }

    @Override
    public WeightedScoreImpacter buildWeightedScoreImpacter(Constraint constraint, HardSoftScore constraintWeight) {
        validateConstraintWeight(constraint, constraintWeight);
        int hardConstraintWeight = constraintWeight.getHardScore();
        int softConstraintWeight = constraintWeight.getSoftScore();
        if (softConstraintWeight == 0) {
            return WeightedScoreImpacter.of(constraintMatchEnabled,
                    (int matchWeight, JustificationsSupplier justificationsSupplier) -> {
                        int hardImpact = hardConstraintWeight * matchWeight;
                        this.hardScore += hardImpact;
                        UndoScoreImpacter undoScoreImpact = () -> this.hardScore -= hardImpact;
                        return impactAndMaybeConstraintMatch(undoScoreImpact, constraint, constraintWeight,
                                HardSoftScore.ofHard(hardImpact),
                                justificationsSupplier);
                    });
        } else if (hardConstraintWeight == 0) {
            return WeightedScoreImpacter.of(constraintMatchEnabled,
                    (int matchWeight, JustificationsSupplier justificationsSupplier) -> {
                        int softImpact = softConstraintWeight * matchWeight;
                        this.softScore += softImpact;
                        UndoScoreImpacter undoScoreImpact = () -> this.softScore -= softImpact;
                        return impactAndMaybeConstraintMatch(undoScoreImpact, constraint, constraintWeight,
                                HardSoftScore.ofSoft(softImpact),
                                justificationsSupplier);
                    });
        } else {
            return WeightedScoreImpacter.of(constraintMatchEnabled,
                    (int matchWeight, JustificationsSupplier justificationsSupplier) -> {
                        int hardImpact = hardConstraintWeight * matchWeight;
                        int softImpact = softConstraintWeight * matchWeight;
                        this.hardScore += hardImpact;
                        this.softScore += softImpact;
                        UndoScoreImpacter undoScoreImpact = () -> {
                            this.hardScore -= hardImpact;
                            this.softScore -= softImpact;
                        };
                        return impactAndMaybeConstraintMatch(undoScoreImpact, constraint, constraintWeight,
                                HardSoftScore.of(hardImpact, softImpact),
                                justificationsSupplier);
                    });
        }
    }

    @Override
    public HardSoftScore extractScore(int initScore) {
        return HardSoftScore.ofUninitialized(initScore, hardScore, softScore);
    }

    @Override
    public String toString() {
        return HardSoftScore.class.getSimpleName() + " inliner";
    }

}
