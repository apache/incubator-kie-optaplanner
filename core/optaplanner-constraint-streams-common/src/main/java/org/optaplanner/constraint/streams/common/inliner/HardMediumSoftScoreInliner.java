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
    public WeightedScoreImpacter<HardMediumSoftScore> buildWeightedScoreImpacter(Constraint constraint,
            HardMediumSoftScore constraintWeight) {
        validateConstraintWeight(constraint, constraintWeight);
        int hardConstraintWeight = constraintWeight.getHardScore();
        int mediumConstraintWeight = constraintWeight.getMediumScore();
        int softConstraintWeight = constraintWeight.getSoftScore();
        ScoreImpacterContext<HardMediumSoftScore> context =
                new ScoreImpacterContext<>(constraint, constraintWeight, constraintMatchEnabled);
        if (mediumConstraintWeight == 0 && softConstraintWeight == 0) {
            return WeightedScoreImpacter.of(context,
                    (ScoreImpacterContext<HardMediumSoftScore> ctx, int matchWeight,
                            JustificationsSupplier justificationsSupplier) -> {
                        int hardImpact = ctx.getConstraintWeight().getHardScore() * matchWeight;
                        this.hardScore += hardImpact;
                        UndoScoreImpacter undoScoreImpact = () -> this.hardScore -= hardImpact;
                        if (!ctx.isConstraintMatchEnabled()) {
                            return undoScoreImpact;
                        }
                        return impactWithConstraintMatch(ctx, undoScoreImpact, HardMediumSoftScore.ofHard(hardImpact),
                                justificationsSupplier);
                    });
        } else if (hardConstraintWeight == 0 && softConstraintWeight == 0) {
            return WeightedScoreImpacter.of(context,
                    (ScoreImpacterContext<HardMediumSoftScore> ctx, int matchWeight,
                            JustificationsSupplier justificationsSupplier) -> {
                        int mediumImpact = ctx.getConstraintWeight().getMediumScore() * matchWeight;
                        this.mediumScore += mediumImpact;
                        UndoScoreImpacter undoScoreImpact = () -> this.mediumScore -= mediumImpact;
                        if (!ctx.isConstraintMatchEnabled()) {
                            return undoScoreImpact;
                        }
                        return impactWithConstraintMatch(ctx, undoScoreImpact, HardMediumSoftScore.ofMedium(mediumImpact),
                                justificationsSupplier);
                    });
        } else if (hardConstraintWeight == 0 && mediumConstraintWeight == 0) {
            return WeightedScoreImpacter.of(context,
                    (ScoreImpacterContext<HardMediumSoftScore> ctx, int matchWeight,
                            JustificationsSupplier justificationsSupplier) -> {
                        int softImpact = ctx.getConstraintWeight().getSoftScore() * matchWeight;
                        this.softScore += softImpact;
                        UndoScoreImpacter undoScoreImpact = () -> this.softScore -= softImpact;
                        if (!ctx.isConstraintMatchEnabled()) {
                            return undoScoreImpact;
                        }
                        return impactWithConstraintMatch(ctx, undoScoreImpact, HardMediumSoftScore.ofSoft(softImpact),
                                justificationsSupplier);
                    });
        } else {
            return WeightedScoreImpacter.of(context,
                    (ScoreImpacterContext<HardMediumSoftScore> ctx, int matchWeight,
                            JustificationsSupplier justificationsSupplier) -> {
                        int hardImpact = ctx.getConstraintWeight().getHardScore() * matchWeight;
                        int mediumImpact = ctx.getConstraintWeight().getMediumScore() * matchWeight;
                        int softImpact = ctx.getConstraintWeight().getSoftScore() * matchWeight;
                        this.hardScore += hardImpact;
                        this.mediumScore += mediumImpact;
                        this.softScore += softImpact;
                        UndoScoreImpacter undoScoreImpact = () -> {
                            this.hardScore -= hardImpact;
                            this.mediumScore -= mediumImpact;
                            this.softScore -= softImpact;
                        };
                        if (!ctx.isConstraintMatchEnabled()) {
                            return undoScoreImpact;
                        }
                        return impactWithConstraintMatch(ctx, undoScoreImpact,
                                HardMediumSoftScore.of(hardImpact, mediumImpact, softImpact), justificationsSupplier);
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
