package org.optaplanner.constraint.streams.common.inliner;

import org.optaplanner.core.api.score.buildin.hardmediumsoftlong.HardMediumSoftLongScore;
import org.optaplanner.core.api.score.stream.Constraint;

final class HardMediumSoftLongScoreInliner extends AbstractScoreInliner<HardMediumSoftLongScore> {

    private long hardScore;
    private long mediumScore;
    private long softScore;

    HardMediumSoftLongScoreInliner(boolean constraintMatchEnabled) {
        super(constraintMatchEnabled);
    }

    @Override
    public WeightedScoreImpacter buildWeightedScoreImpacter(Constraint constraint, HardMediumSoftLongScore constraintWeight) {
        validateConstraintWeight(constraint, constraintWeight);
        long hardConstraintWeight = constraintWeight.getHardScore();
        long mediumConstraintWeight = constraintWeight.getMediumScore();
        long softConstraintWeight = constraintWeight.getSoftScore();
        if (mediumConstraintWeight == 0L && softConstraintWeight == 0L) {
            return WeightedScoreImpacter.of(constraintMatchEnabled,
                    (long matchWeight, JustificationsSupplier justificationsSupplier) -> {
                        long hardImpact = hardConstraintWeight * matchWeight;
                        this.hardScore += hardImpact;
                        UndoScoreImpacter undoScoreImpact = () -> this.hardScore -= hardImpact;
                        return impactAndMaybeConstraintMatch(undoScoreImpact, constraint, constraintWeight,
                                HardMediumSoftLongScore.ofHard(hardImpact),
                                justificationsSupplier);
                    });
        } else if (hardConstraintWeight == 0L && softConstraintWeight == 0L) {
            return WeightedScoreImpacter.of(constraintMatchEnabled,
                    (long matchWeight, JustificationsSupplier justificationsSupplier) -> {
                        long mediumImpact = mediumConstraintWeight * matchWeight;
                        this.mediumScore += mediumImpact;
                        UndoScoreImpacter undoScoreImpact = () -> this.mediumScore -= mediumImpact;
                        return impactAndMaybeConstraintMatch(undoScoreImpact, constraint, constraintWeight,
                                HardMediumSoftLongScore.ofMedium(mediumImpact),
                                justificationsSupplier);
                    });
        } else if (hardConstraintWeight == 0L && mediumConstraintWeight == 0L) {
            return WeightedScoreImpacter.of(constraintMatchEnabled,
                    (long matchWeight, JustificationsSupplier justificationsSupplier) -> {
                        long softImpact = softConstraintWeight * matchWeight;
                        this.softScore += softImpact;
                        UndoScoreImpacter undoScoreImpact = () -> this.softScore -= softImpact;
                        return impactAndMaybeConstraintMatch(undoScoreImpact, constraint, constraintWeight,
                                HardMediumSoftLongScore.ofSoft(softImpact),
                                justificationsSupplier);
                    });
        } else {
            return WeightedScoreImpacter.of(constraintMatchEnabled,
                    (long matchWeight, JustificationsSupplier justificationsSupplier) -> {
                        long hardImpact = hardConstraintWeight * matchWeight;
                        long mediumImpact = mediumConstraintWeight * matchWeight;
                        long softImpact = softConstraintWeight * matchWeight;
                        this.hardScore += hardImpact;
                        this.mediumScore += mediumImpact;
                        this.softScore += softImpact;
                        UndoScoreImpacter undoScoreImpact = () -> {
                            this.hardScore -= hardImpact;
                            this.mediumScore -= mediumImpact;
                            this.softScore -= softImpact;
                        };
                        return impactAndMaybeConstraintMatch(undoScoreImpact, constraint, constraintWeight,
                                HardMediumSoftLongScore.of(hardImpact, mediumImpact, softImpact),
                                justificationsSupplier);
                    });
        }
    }

    @Override
    public HardMediumSoftLongScore extractScore(int initScore) {
        return HardMediumSoftLongScore.ofUninitialized(initScore, hardScore, mediumScore, softScore);
    }

    @Override
    public String toString() {
        return HardMediumSoftLongScore.class.getSimpleName() + " inliner";
    }

}
