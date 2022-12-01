package org.optaplanner.constraint.streams.common.inliner;

import org.optaplanner.core.api.score.buildin.simplelong.SimpleLongScore;
import org.optaplanner.core.api.score.stream.Constraint;

final class SimpleLongScoreInliner extends AbstractScoreInliner<SimpleLongScore> {

    private long score;

    SimpleLongScoreInliner(boolean constraintMatchEnabled) {
        super(constraintMatchEnabled);
    }

    @Override
    public WeightedScoreImpacter<SimpleLongScore, SimpleLongScoreContext> buildWeightedScoreImpacter(Constraint constraint,
            SimpleLongScore constraintWeight) {
        validateConstraintWeight(constraint, constraintWeight);
        SimpleLongScoreContext context = new SimpleLongScoreContext(constraint, constraintWeight, constraintMatchEnabled,
                impact -> this.score += impact);
        return WeightedScoreImpacter.of(context,
                (SimpleLongScoreContext ctx, long matchWeight, JustificationsSupplier justificationsSupplier) -> {
                    long impact = ctx.getConstraintWeight().getScore() * matchWeight;
                    UndoScoreImpacter undoScoreImpact = ctx.changeScoreBy(impact);
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
