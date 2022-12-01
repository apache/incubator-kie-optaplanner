package org.optaplanner.constraint.streams.common.inliner;

import java.math.BigDecimal;

import org.optaplanner.core.api.score.buildin.simplebigdecimal.SimpleBigDecimalScore;
import org.optaplanner.core.api.score.stream.Constraint;

final class SimpleBigDecimalScoreInliner extends AbstractScoreInliner<SimpleBigDecimalScore> {

    private BigDecimal score = BigDecimal.ZERO;

    SimpleBigDecimalScoreInliner(boolean constraintMatchEnabled) {
        super(constraintMatchEnabled);
    }

    @Override
    public WeightedScoreImpacter<SimpleBigDecimalScore, SimpleBigDecimalScoreContext> buildWeightedScoreImpacter(
            Constraint constraint, SimpleBigDecimalScore constraintWeight) {
        validateConstraintWeight(constraint, constraintWeight);
        SimpleBigDecimalScoreContext context = new SimpleBigDecimalScoreContext(constraint, constraintWeight,
                constraintMatchEnabled, impact -> this.score = this.score.add(impact));
        return WeightedScoreImpacter.of(context,
                (SimpleBigDecimalScoreContext ctx, BigDecimal matchWeight, JustificationsSupplier justificationsSupplier) -> {
                    BigDecimal impact = ctx.getConstraintWeight().getScore().multiply(matchWeight);
                    UndoScoreImpacter undoScoreImpact = ctx.changeScoreBy(impact);
                    if (!ctx.isConstraintMatchEnabled()) {
                        return undoScoreImpact;
                    }
                    return impactWithConstraintMatch(ctx, undoScoreImpact, SimpleBigDecimalScore.of(impact),
                            justificationsSupplier);
                });
    }

    @Override
    public SimpleBigDecimalScore extractScore(int initScore) {
        return SimpleBigDecimalScore.ofUninitialized(initScore, score);
    }

    @Override
    public String toString() {
        return SimpleBigDecimalScore.class.getSimpleName() + " inliner";
    }

}
