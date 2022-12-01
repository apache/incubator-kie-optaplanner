package org.optaplanner.constraint.streams.common.inliner;

import org.optaplanner.core.api.score.buildin.simple.SimpleScore;
import org.optaplanner.core.api.score.stream.Constraint;

final class SimpleScoreInliner extends AbstractScoreInliner<SimpleScore> {

    private int score;

    SimpleScoreInliner(boolean constraintMatchEnabled) {
        super(constraintMatchEnabled);
    }

    @Override
    public WeightedScoreImpacter<SimpleScore, SimpleScoreContext> buildWeightedScoreImpacter(Constraint constraint,
            SimpleScore constraintWeight) {
        validateConstraintWeight(constraint, constraintWeight);
        SimpleScoreContext context = new SimpleScoreContext(constraint, constraintWeight,
                constraintMatchEnabled, impact -> this.score += impact);
        return WeightedScoreImpacter.of(context,
                (SimpleScoreContext ctx, int matchWeight, JustificationsSupplier justificationsSupplier) -> {
                    int impact = ctx.getConstraintWeight().getScore() * matchWeight;
                    UndoScoreImpacter undoScoreImpact = ctx.changeScoreBy(impact);
                    if (!ctx.isConstraintMatchEnabled()) {
                        return undoScoreImpact;
                    }
                    return impactWithConstraintMatch(ctx, undoScoreImpact, SimpleScore.of(impact), justificationsSupplier);
                });
    }

    @Override
    public SimpleScore extractScore(int initScore) {
        return SimpleScore.ofUninitialized(initScore, score);
    }

    @Override
    public String toString() {
        return SimpleScore.class.getSimpleName() + " inliner";
    }

}
