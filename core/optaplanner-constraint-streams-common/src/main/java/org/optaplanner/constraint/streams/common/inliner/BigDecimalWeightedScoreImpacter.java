package org.optaplanner.constraint.streams.common.inliner;

import java.math.BigDecimal;
import java.util.Objects;

import org.optaplanner.core.api.score.Score;

final class BigDecimalWeightedScoreImpacter<Score_ extends Score<Score_>> implements WeightedScoreImpacter<Score_> {

    private final BigDecimalImpactFunction<Score_> impactFunction;
    private final ScoreImpacterContext<Score_> context;

    public BigDecimalWeightedScoreImpacter(BigDecimalImpactFunction<Score_> impactFunction,
            ScoreImpacterContext<Score_> context) {
        this.impactFunction = Objects.requireNonNull(impactFunction);
        this.context = context;
    }

    @Override
    public UndoScoreImpacter impactScore(int matchWeight, JustificationsSupplier justificationsSupplier) {
        return impactFunction.impact(context, BigDecimal.valueOf(matchWeight), justificationsSupplier);
    }

    @Override
    public UndoScoreImpacter impactScore(long matchWeight, JustificationsSupplier justificationsSupplier) {
        return impactFunction.impact(context, BigDecimal.valueOf(matchWeight), justificationsSupplier);
    }

    @Override
    public UndoScoreImpacter impactScore(BigDecimal matchWeight, JustificationsSupplier justificationsSupplier) {
        return impactFunction.impact(context, matchWeight, justificationsSupplier);
    }

    @Override
    public ScoreImpacterContext<Score_> getContext() {
        return context;
    }

}
