package org.optaplanner.constraint.streams.common.inliner;

import org.optaplanner.core.api.score.buildin.simple.SimpleScore;
import org.optaplanner.core.api.score.stream.Constraint;

final class SimpleScoreInliner extends AbstractScoreInliner<SimpleScore> {

    private int score;

    SimpleScoreInliner(boolean constraintMatchEnabled) {
        super(constraintMatchEnabled);
    }

    @Override
    public WeightedScoreImpacter buildWeightedScoreImpacter(Constraint constraint, SimpleScore constraintWeight) {
        validateConstraintWeight(constraint, constraintWeight);
        int simpleConstraintWeight = constraintWeight.getScore();
        return WeightedScoreImpacter.of(constraintMatchEnabled,
                (int matchWeight, JustificationsSupplier justificationsSupplier) -> {
                    int impact = simpleConstraintWeight * matchWeight;
                    this.score += impact;
                    UndoScoreImpacter undoScoreImpact = () -> this.score -= impact;
                    return impactAndMaybeConstraintMatch(undoScoreImpact, constraint, constraintWeight,
                            SimpleScore.of(impact),
                            justificationsSupplier);
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
