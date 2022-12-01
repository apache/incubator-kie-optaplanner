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
    public WeightedScoreImpacter<HardSoftScore, HardSoftScoreContext> buildWeightedScoreImpacter(Constraint constraint,
            HardSoftScore constraintWeight) {
        validateConstraintWeight(constraint, constraintWeight);
        HardSoftScoreContext context = new HardSoftScoreContext(constraint, constraintWeight, constraintMatchEnabled,
                impact -> this.hardScore += impact, impact -> this.softScore += impact);
        if (constraintWeight.getSoftScore() == 0) {
            return WeightedScoreImpacter.of(context,
                    (HardSoftScoreContext ctx, int matchWeight, JustificationsSupplier justificationsSupplier) -> {
                        int hardImpact = ctx.getConstraintWeight().getHardScore() * matchWeight;
                        UndoScoreImpacter undoScoreImpact = ctx.changeHardScoreBy(hardImpact);
                        if (!ctx.isConstraintMatchEnabled()) {
                            return undoScoreImpact;
                        }
                        return impactWithConstraintMatch(ctx, undoScoreImpact, HardSoftScore.ofHard(hardImpact),
                                justificationsSupplier);
                    });
        } else if (constraintWeight.getHardScore() == 0) {
            return WeightedScoreImpacter.of(context,
                    (HardSoftScoreContext ctx, int matchWeight, JustificationsSupplier justificationsSupplier) -> {
                        int softImpact = ctx.getConstraintWeight().getSoftScore() * matchWeight;
                        UndoScoreImpacter undoScoreImpact = ctx.changeSoftScoreBy(softImpact);
                        if (!ctx.isConstraintMatchEnabled()) {
                            return undoScoreImpact;
                        }
                        return impactWithConstraintMatch(ctx, undoScoreImpact, HardSoftScore.ofSoft(softImpact),
                                justificationsSupplier);
                    });
        } else {
            return WeightedScoreImpacter.of(context,
                    (HardSoftScoreContext ctx, int matchWeight, JustificationsSupplier justificationsSupplier) -> {
                        int hardImpact = ctx.getConstraintWeight().getHardScore() * matchWeight;
                        int softImpact = ctx.getConstraintWeight().getSoftScore() * matchWeight;
                        UndoScoreImpacter undoScoreImpact = ctx.changeScoreBy(hardImpact, softImpact);
                        if (!ctx.isConstraintMatchEnabled()) {
                            return undoScoreImpact;
                        }
                        return impactWithConstraintMatch(ctx, undoScoreImpact, HardSoftScore.of(hardImpact, softImpact),
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
