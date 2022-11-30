package org.optaplanner.constraint.streams.common.inliner;

import org.optaplanner.core.api.score.buildin.hardmediumsoft.HardMediumSoftScore;
import org.optaplanner.core.api.score.stream.Constraint;

final class HardMediumSoftScoreInliner extends AbstractScoreInliner<HardMediumSoftScore> {

    private int hardScore;
    private int mediumScore;
    private int softScore;

    HardMediumSoftScoreInliner(boolean constraintMatchEnabled) {
        super(constraintMatchEnabled);
    }

    @Override
    public WeightedScoreImpacter buildWeightedScoreImpacter(Constraint constraint, HardMediumSoftScore constraintWeight) {
        validateConstraintWeight(constraint, constraintWeight);
        int hardConstraintWeight = constraintWeight.getHardScore();
        int mediumConstraintWeight = constraintWeight.getMediumScore();
        int softConstraintWeight = constraintWeight.getSoftScore();
        if (mediumConstraintWeight == 0 && softConstraintWeight == 0) {
            return WeightedScoreImpacter.of(constraintMatchEnabled,
                    (int matchWeight, JustificationsSupplier justificationsSupplier) -> {
                        int hardImpact = hardConstraintWeight * matchWeight;
                        this.hardScore += hardImpact;
                        UndoScoreImpacter undoScoreImpact = () -> this.hardScore -= hardImpact;
                        return impactAndMaybeConstraintMatch(undoScoreImpact, constraint, constraintWeight,
                                HardMediumSoftScore.ofHard(hardImpact),
                                justificationsSupplier);
                    });
        } else if (hardConstraintWeight == 0 && softConstraintWeight == 0) {
            return WeightedScoreImpacter.of(constraintMatchEnabled,
                    (int matchWeight, JustificationsSupplier justificationsSupplier) -> {
                        int mediumImpact = mediumConstraintWeight * matchWeight;
                        this.mediumScore += mediumImpact;
                        UndoScoreImpacter undoScoreImpact = () -> this.mediumScore -= mediumImpact;
                        return impactAndMaybeConstraintMatch(undoScoreImpact, constraint, constraintWeight,
                                HardMediumSoftScore.ofMedium(mediumImpact),
                                justificationsSupplier);
                    });
        } else if (hardConstraintWeight == 0 && mediumConstraintWeight == 0) {
            return WeightedScoreImpacter.of(constraintMatchEnabled,
                    (int matchWeight, JustificationsSupplier justificationsSupplier) -> {
                        int softImpact = softConstraintWeight * matchWeight;
                        this.softScore += softImpact;
                        UndoScoreImpacter undoScoreImpact = () -> this.softScore -= softImpact;
                        return impactAndMaybeConstraintMatch(undoScoreImpact, constraint, constraintWeight,
                                HardMediumSoftScore.ofSoft(softImpact),
                                justificationsSupplier);
                    });
        } else {
            return WeightedScoreImpacter.of(constraintMatchEnabled,
                    (int matchWeight, JustificationsSupplier justificationsSupplier) -> {
                        int hardImpact = hardConstraintWeight * matchWeight;
                        int mediumImpact = mediumConstraintWeight * matchWeight;
                        int softImpact = softConstraintWeight * matchWeight;
                        this.hardScore += hardImpact;
                        this.mediumScore += mediumImpact;
                        this.softScore += softImpact;
                        UndoScoreImpacter undoScoreImpact = () -> {
                            this.hardScore -= hardImpact;
                            this.mediumScore -= mediumImpact;
                            this.softScore -= softImpact;
                        };
                        return impactAndMaybeConstraintMatch(undoScoreImpact, constraint, constraintWeight,
                                HardMediumSoftScore.of(hardImpact, mediumImpact, softImpact),
                                justificationsSupplier);
                    });
        }
    }

    @Override
    public HardMediumSoftScore extractScore(int initScore) {
        return HardMediumSoftScore.ofUninitialized(initScore, hardScore, mediumScore, softScore);
    }

    @Override
    public String toString() {
        return HardMediumSoftScore.class.getSimpleName() + " inliner";
    }

}
