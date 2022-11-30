package org.optaplanner.constraint.streams.common.inliner;

import java.math.BigDecimal;
import java.util.Objects;

import org.optaplanner.core.api.score.Score;

final class IntWeightedScoreImpacter<Score_ extends Score<Score_>> implements WeightedScoreImpacter<Score_> {

    private final IntImpactFunction<Score_> impactFunction;
    private final ScoreImpacterContext<Score_> context;

    public IntWeightedScoreImpacter(IntImpactFunction<Score_> impactFunction, ScoreImpacterContext<Score_> context) {
        this.impactFunction = Objects.requireNonNull(impactFunction);
        this.context = context;
    }

    @Override
    public UndoScoreImpacter impactScore(int matchWeight, JustificationsSupplier justificationsSupplier) {
        return impactFunction.impact(context, matchWeight, justificationsSupplier);
    }

    @Override
    public UndoScoreImpacter impactScore(long matchWeight, JustificationsSupplier justificationsSupplier) {
        throw new UnsupportedOperationException("Impossible state: passing long into an int impacter.");
    }

    @Override
    public UndoScoreImpacter impactScore(BigDecimal matchWeight, JustificationsSupplier justificationsSupplier) {
        throw new UnsupportedOperationException("Impossible state: passing BigDecimal into an int impacter.");
    }

    @Override
    public ScoreImpacterContext<Score_> getContext() {
        return context;
    }

}
