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
    public WeightedScoreImpacter<HardMediumSoftScore, HardMediumSoftScoreContext> buildWeightedScoreImpacter(
            Constraint constraint, HardMediumSoftScore constraintWeight) {
        validateConstraintWeight(constraint, constraintWeight);
        int hardConstraintWeight = constraintWeight.getHardScore();
        int mediumConstraintWeight = constraintWeight.getMediumScore();
        int softConstraintWeight = constraintWeight.getSoftScore();
        HardMediumSoftScoreContext context =
                new HardMediumSoftScoreContext(constraint, constraintWeight, constraintMatchEnabled,
                        impact -> this.hardScore += impact, impact -> this.mediumScore += impact,
                        impact -> this.softScore += impact);
        if (mediumConstraintWeight == 0 && softConstraintWeight == 0) {
            return WeightedScoreImpacter.of(context,
                    (HardMediumSoftScoreContext ctx, int matchWeight, JustificationsSupplier justificationsSupplier) -> {
                        int hardImpact = ctx.getConstraintWeight().getHardScore() * matchWeight;
                        UndoScoreImpacter undoScoreImpact = ctx.changeHardScoreBy(hardImpact);
                        if (!ctx.isConstraintMatchEnabled()) {
                            return undoScoreImpact;
                        }
                        return impactWithConstraintMatch(ctx, undoScoreImpact, HardMediumSoftScore.ofHard(hardImpact),
                                justificationsSupplier);
                    });
        } else if (hardConstraintWeight == 0 && softConstraintWeight == 0) {
            return WeightedScoreImpacter.of(context,
                    (HardMediumSoftScoreContext ctx, int matchWeight, JustificationsSupplier justificationsSupplier) -> {
                        int mediumImpact = ctx.getConstraintWeight().getMediumScore() * matchWeight;
                        UndoScoreImpacter undoScoreImpact = ctx.changeMediumScoreBy(mediumImpact);
                        if (!ctx.isConstraintMatchEnabled()) {
                            return undoScoreImpact;
                        }
                        return impactWithConstraintMatch(ctx, undoScoreImpact, HardMediumSoftScore.ofMedium(mediumImpact),
                                justificationsSupplier);
                    });
        } else if (hardConstraintWeight == 0 && mediumConstraintWeight == 0) {
            return WeightedScoreImpacter.of(context,
                    (HardMediumSoftScoreContext ctx, int matchWeight, JustificationsSupplier justificationsSupplier) -> {
                        int softImpact = ctx.getConstraintWeight().getSoftScore() * matchWeight;
                        UndoScoreImpacter undoScoreImpact = ctx.changeSoftScoreBy(softImpact);
                        if (!ctx.isConstraintMatchEnabled()) {
                            return undoScoreImpact;
                        }
                        return impactWithConstraintMatch(ctx, undoScoreImpact, HardMediumSoftScore.ofSoft(softImpact),
                                justificationsSupplier);
                    });
        } else {
            return WeightedScoreImpacter.of(context,
                    (HardMediumSoftScoreContext ctx, int matchWeight, JustificationsSupplier justificationsSupplier) -> {
                        int hardImpact = ctx.getConstraintWeight().getHardScore() * matchWeight;
                        int mediumImpact = ctx.getConstraintWeight().getMediumScore() * matchWeight;
                        int softImpact = ctx.getConstraintWeight().getSoftScore() * matchWeight;
                        UndoScoreImpacter undoScoreImpact = ctx.changeScoreBy(hardImpact, mediumImpact, softImpact);
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
