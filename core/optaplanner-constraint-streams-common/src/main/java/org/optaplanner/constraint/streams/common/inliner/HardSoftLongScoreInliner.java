package org.optaplanner.constraint.streams.common.inliner;

import org.optaplanner.core.api.score.buildin.hardsoftlong.HardSoftLongScore;
import org.optaplanner.core.api.score.stream.Constraint;

final class HardSoftLongScoreInliner extends AbstractScoreInliner<HardSoftLongScore> {

    private long hardScore;
    private long softScore;

    HardSoftLongScoreInliner(boolean constraintMatchEnabled) {
        super(constraintMatchEnabled);
    }

    @Override
    public WeightedScoreImpacter buildWeightedScoreImpacter(Constraint constraint, HardSoftLongScore constraintWeight) {
        validateConstraintWeight(constraint, constraintWeight);
        long hardConstraintWeight = constraintWeight.getHardScore();
        long softConstraintWeight = constraintWeight.getSoftScore();
        if (softConstraintWeight == 0L) {
            return WeightedScoreImpacter.of(constraintMatchEnabled,
                    (long matchWeight, JustificationsSupplier justificationsSupplier) -> {
                        long hardImpact = hardConstraintWeight * matchWeight;
                        this.hardScore += hardImpact;
                        UndoScoreImpacter undoScoreImpact = () -> this.hardScore -= hardImpact;
                        return impactAndMaybeConstraintMatch(undoScoreImpact, constraint, constraintWeight,
                                HardSoftLongScore.ofHard(hardImpact),
                                justificationsSupplier);
                    });
        } else if (hardConstraintWeight == 0L) {
            return WeightedScoreImpacter.of(constraintMatchEnabled,
                    (long matchWeight, JustificationsSupplier justificationsSupplier) -> {
                        long softImpact = softConstraintWeight * matchWeight;
                        this.softScore += softImpact;
                        UndoScoreImpacter undoScoreImpact = () -> this.softScore -= softImpact;
                        return impactAndMaybeConstraintMatch(undoScoreImpact, constraint, constraintWeight,
                                HardSoftLongScore.ofSoft(softImpact),
                                justificationsSupplier);
                    });
        } else {
            return WeightedScoreImpacter.of(constraintMatchEnabled,
                    (long matchWeight, JustificationsSupplier justificationsSupplier) -> {
                        long hardImpact = hardConstraintWeight * matchWeight;
                        long softImpact = softConstraintWeight * matchWeight;
                        this.hardScore += hardImpact;
                        this.softScore += softImpact;
                        UndoScoreImpacter undoScoreImpact = () -> {
                            this.hardScore -= hardImpact;
                            this.softScore -= softImpact;
                        };
                        return impactAndMaybeConstraintMatch(undoScoreImpact, constraint, constraintWeight,
                                HardSoftLongScore.of(hardImpact, softImpact),
                                justificationsSupplier);
                    });
        }
    }

    @Override
    public HardSoftLongScore extractScore(int initScore) {
        return HardSoftLongScore.ofUninitialized(initScore, hardScore, softScore);
    }

    @Override
    public String toString() {
        return HardSoftLongScore.class.getSimpleName() + " inliner";
    }

}
