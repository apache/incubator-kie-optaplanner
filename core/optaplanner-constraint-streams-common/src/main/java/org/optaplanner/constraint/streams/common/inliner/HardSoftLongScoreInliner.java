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
    public WeightedScoreImpacter<HardSoftLongScore> buildWeightedScoreImpacter(Constraint constraint,
            HardSoftLongScore constraintWeight) {
        validateConstraintWeight(constraint, constraintWeight);
        ScoreImpacterContext<HardSoftLongScore> context =
                new ScoreImpacterContext<>(constraint, constraintWeight, constraintMatchEnabled);
        if (constraintWeight.getSoftScore() == 0L) {
            return WeightedScoreImpacter.of(context,
                    (ScoreImpacterContext<HardSoftLongScore> ctx, long matchWeight,
                            JustificationsSupplier justificationsSupplier) -> {
                        long hardImpact = ctx.getConstraintWeight().getHardScore() * matchWeight;
                        this.hardScore += hardImpact;
                        UndoScoreImpacter undoScoreImpact = () -> this.hardScore -= hardImpact;
                        if (!ctx.isConstraintMatchEnabled()) {
                            return undoScoreImpact;
                        }
                        return impactWithConstraintMatch(ctx, undoScoreImpact, HardSoftLongScore.ofHard(hardImpact),
                                justificationsSupplier);
                    });
        } else if (constraintWeight.getHardScore() == 0L) {
            return WeightedScoreImpacter.of(context,
                    (ScoreImpacterContext<HardSoftLongScore> ctx, long matchWeight,
                            JustificationsSupplier justificationsSupplier) -> {
                        long softImpact = ctx.getConstraintWeight().getSoftScore() * matchWeight;
                        this.softScore += softImpact;
                        UndoScoreImpacter undoScoreImpact = () -> this.softScore -= softImpact;
                        if (!ctx.isConstraintMatchEnabled()) {
                            return undoScoreImpact;
                        }
                        return impactWithConstraintMatch(ctx, undoScoreImpact, HardSoftLongScore.ofSoft(softImpact),
                                justificationsSupplier);
                    });
        } else {
            return WeightedScoreImpacter.of(context,
                    (ScoreImpacterContext<HardSoftLongScore> ctx, long matchWeight,
                            JustificationsSupplier justificationsSupplier) -> {
                        long hardImpact = ctx.getConstraintWeight().getHardScore() * matchWeight;
                        long softImpact = ctx.getConstraintWeight().getSoftScore() * matchWeight;
                        this.hardScore += hardImpact;
                        this.softScore += softImpact;
                        UndoScoreImpacter undoScoreImpact = () -> {
                            this.hardScore -= hardImpact;
                            this.softScore -= softImpact;
                        };
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
