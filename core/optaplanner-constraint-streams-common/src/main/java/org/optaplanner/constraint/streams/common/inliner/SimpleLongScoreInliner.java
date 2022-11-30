package org.optaplanner.constraint.streams.common.inliner;

import org.optaplanner.core.api.score.buildin.simplelong.SimpleLongScore;
import org.optaplanner.core.api.score.stream.Constraint;

final class SimpleLongScoreInliner extends AbstractScoreInliner<SimpleLongScore> {

    private long score;

    SimpleLongScoreInliner(boolean constraintMatchEnabled) {
        super(constraintMatchEnabled);
    }

    @Override
    public WeightedScoreImpacter<SimpleLongScore> buildWeightedScoreImpacter(Constraint constraint,
            SimpleLongScore constraintWeight) {
        validateConstraintWeight(constraint, constraintWeight);
        ScoreImpacterContext<SimpleLongScore> context =
                new ScoreImpacterContext<>(constraint, constraintWeight, constraintMatchEnabled);
        return WeightedScoreImpacter.of(context,
                (ScoreImpacterContext<SimpleLongScore> ctx, long matchWeight,
                        JustificationsSupplier justificationsSupplier) -> {
                    long impact = ctx.getConstraintWeight().getScore() * matchWeight;
                    this.score += impact;
                    UndoScoreImpacter undoScoreImpact = () -> this.score -= impact;
                    if (!ctx.isConstraintMatchEnabled()) {
                        return undoScoreImpact;
                    }
                    return impactWithConstraintMatch(ctx, undoScoreImpact, SimpleLongScore.of(impact), justificationsSupplier);
                });
    }

    @Override
    public SimpleLongScore extractScore(int initScore) {
        return SimpleLongScore.ofUninitialized(initScore, score);
    }

    @Override
    public String toString() {
        return SimpleLongScore.class.getSimpleName() + " inliner";
    }

}
