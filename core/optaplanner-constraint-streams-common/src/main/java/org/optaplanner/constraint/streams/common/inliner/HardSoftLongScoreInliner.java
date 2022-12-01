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
    public WeightedScoreImpacter<HardSoftLongScore, HardSoftLongScoreContext> buildWeightedScoreImpacter(Constraint constraint,
            HardSoftLongScore constraintWeight) {
        validateConstraintWeight(constraint, constraintWeight);
        HardSoftLongScoreContext context = new HardSoftLongScoreContext(constraint, constraintWeight, constraintMatchEnabled,
                impact -> this.hardScore += impact, impact -> this.softScore += impact);
        if (constraintWeight.getSoftScore() == 0L) {
            return WeightedScoreImpacter.of(context,
                    (HardSoftLongScoreContext ctx, long matchWeight, JustificationsSupplier justificationsSupplier) -> {
                        long hardImpact = ctx.getConstraintWeight().getHardScore() * matchWeight;
                        UndoScoreImpacter undoScoreImpact = ctx.changeHardScoreBy(hardImpact);
                        if (!ctx.isConstraintMatchEnabled()) {
                            return undoScoreImpact;
                        }
                        return impactWithConstraintMatch(ctx, undoScoreImpact, HardSoftLongScore.ofHard(hardImpact),
                                justificationsSupplier);
                    });
        } else if (constraintWeight.getHardScore() == 0L) {
            return WeightedScoreImpacter.of(context,
                    (HardSoftLongScoreContext ctx, long matchWeight, JustificationsSupplier justificationsSupplier) -> {
                        long softImpact = ctx.getConstraintWeight().getSoftScore() * matchWeight;
                        UndoScoreImpacter undoScoreImpact = ctx.changeSoftScoreBy(softImpact);
                        if (!ctx.isConstraintMatchEnabled()) {
                            return undoScoreImpact;
                        }
                        return impactWithConstraintMatch(ctx, undoScoreImpact, HardSoftLongScore.ofSoft(softImpact),
                                justificationsSupplier);
                    });
        } else {
            return WeightedScoreImpacter.of(context,
                    (HardSoftLongScoreContext ctx, long matchWeight, JustificationsSupplier justificationsSupplier) -> {
                        long hardImpact = ctx.getConstraintWeight().getHardScore() * matchWeight;
                        long softImpact = ctx.getConstraintWeight().getSoftScore() * matchWeight;
                        UndoScoreImpacter undoScoreImpact = ctx.changeScoreBy(hardImpact, softImpact);
                        if (!ctx.isConstraintMatchEnabled()) {
                            return undoScoreImpact;
                        }
                        return impactWithConstraintMatch(ctx, undoScoreImpact, HardSoftLongScore.of(hardImpact, softImpact),
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
