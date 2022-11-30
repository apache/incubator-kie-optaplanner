package org.optaplanner.constraint.streams.common.inliner;

import java.math.BigDecimal;
import java.util.Objects;

final class BigDecimalWeightedScoreImpacter implements WeightedScoreImpacter {

    private final BigDecimalImpactFunction impactFunction;
    private final boolean constraintMatchEnabled;

    public BigDecimalWeightedScoreImpacter(BigDecimalImpactFunction impactFunction, boolean constraintMatchEnabled) {
        this.impactFunction = Objects.requireNonNull(impactFunction);
        this.constraintMatchEnabled = constraintMatchEnabled;
    }

    @Override
    public UndoScoreImpacter impactScore(int matchWeight, JustificationsSupplier justificationsSupplier) {
        return impactFunction.impact(BigDecimal.valueOf(matchWeight), justificationsSupplier);
    }

    @Override
    public UndoScoreImpacter impactScore(long matchWeight, JustificationsSupplier justificationsSupplier) {
        return impactFunction.impact(BigDecimal.valueOf(matchWeight), justificationsSupplier);
    }

    @Override
    public UndoScoreImpacter impactScore(BigDecimal matchWeight, JustificationsSupplier justificationsSupplier) {
        return impactFunction.impact(matchWeight, justificationsSupplier);
    }

    @Override
    public boolean isConstraintMatchEnabled() {
        return constraintMatchEnabled;
    }

}
