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
    public WeightedScoreImpacter<HardSoftScore> buildWeightedScoreImpacter(Constraint constraint,
            HardSoftScore constraintWeight) {
        validateConstraintWeight(constraint, constraintWeight);
        ScoreImpacterContext<HardSoftScore> context =
                new ScoreImpacterContext<>(constraint, constraintWeight, constraintMatchEnabled);
        if (constraintWeight.getSoftScore() == 0) {
            return WeightedScoreImpacter.of(context,
                    (ScoreImpacterContext<HardSoftScore> ctx, int matchWeight,
                            JustificationsSupplier justificationsSupplier) -> {
                        int hardImpact = ctx.getConstraintWeight().getHardScore() * matchWeight;
                        this.hardScore += hardImpact;
                        UndoScoreImpacter undoScoreImpact = () -> this.hardScore -= hardImpact;
                        return impactAndMaybeConstraintMatch(undoScoreImpact, ctx, HardSoftScore.ofHard(hardImpact),
                                justificationsSupplier);
                    });
        } else if (constraintWeight.getHardScore() == 0) {
            return WeightedScoreImpacter.of(context,
                    (ScoreImpacterContext<HardSoftScore> ctx, int matchWeight,
                            JustificationsSupplier justificationsSupplier) -> {
                        int softImpact = ctx.getConstraintWeight().getSoftScore() * matchWeight;
                        this.softScore += softImpact;
                        UndoScoreImpacter undoScoreImpact = () -> this.softScore -= softImpact;
                        return impactAndMaybeConstraintMatch(undoScoreImpact, ctx, HardSoftScore.ofSoft(softImpact),
                                justificationsSupplier);
                    });
        } else {
            return WeightedScoreImpacter.of(context,
                    (ScoreImpacterContext<HardSoftScore> ctx, int matchWeight,
                            JustificationsSupplier justificationsSupplier) -> {
                        int hardImpact = ctx.getConstraintWeight().getHardScore() * matchWeight;
                        int softImpact = ctx.getConstraintWeight().getSoftScore() * matchWeight;
                        this.hardScore += hardImpact;
                        this.softScore += softImpact;
                        UndoScoreImpacter undoScoreImpact = () -> {
                            this.hardScore -= hardImpact;
                            this.softScore -= softImpact;
                        };
                        return impactAndMaybeConstraintMatch(undoScoreImpact, ctx, HardSoftScore.of(hardImpact, softImpact),
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
