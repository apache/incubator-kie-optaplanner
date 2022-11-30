package org.optaplanner.constraint.streams.common.inliner;

import org.optaplanner.core.api.score.buildin.simple.SimpleScore;
import org.optaplanner.core.api.score.stream.Constraint;

final class SimpleScoreInliner extends AbstractScoreInliner<SimpleScore> {

    private int score;

    SimpleScoreInliner(boolean constraintMatchEnabled) {
        super(constraintMatchEnabled);
    }

    @Override
    public WeightedScoreImpacter<SimpleScore> buildWeightedScoreImpacter(Constraint constraint, SimpleScore constraintWeight) {
        validateConstraintWeight(constraint, constraintWeight);
        ScoreImpacterContext<SimpleScore> context =
                new ScoreImpacterContext<>(constraint, constraintWeight, constraintMatchEnabled);
        return WeightedScoreImpacter.of(context,
                (ScoreImpacterContext<SimpleScore> ctx, int matchWeight, JustificationsSupplier justificationsSupplier) -> {
                    int impact = ctx.getConstraintWeight().getScore() * matchWeight;
                    this.score += impact;
                    UndoScoreImpacter undoScoreImpact = () -> this.score -= impact;
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
