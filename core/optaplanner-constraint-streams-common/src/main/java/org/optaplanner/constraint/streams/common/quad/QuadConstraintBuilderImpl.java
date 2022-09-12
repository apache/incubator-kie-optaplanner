package org.optaplanner.constraint.streams.common.quad;

import java.util.Objects;

import org.optaplanner.constraint.streams.common.AbstractConstraintBuilder;
import org.optaplanner.constraint.streams.common.ScoreImpactType;
import org.optaplanner.core.api.function.QuadFunction;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.stream.ConstraintJustification;
import org.optaplanner.core.api.score.stream.quad.QuadConstraintBuilder;

public final class QuadConstraintBuilderImpl<A, B, C, D>
        extends AbstractConstraintBuilder<QuadConstraintBuilder<A, B, C, D>>
        implements QuadConstraintBuilder<A, B, C, D> {

    private QuadFunction<A, B, C, D, ConstraintJustification> justificationFunction;

    public QuadConstraintBuilderImpl(QuadConstraintConstructor<A, B, C, D> constraintConstructor,
            ScoreImpactType impactType, Score<?> constraintWeight) {
        super(constraintConstructor, impactType, constraintWeight);
    }

    @Override
    protected QuadFunction<A, B, C, D, ConstraintJustification> getJustificationFunction() {
        if (justificationFunction == null) {
            return null; // Will use the default.
        }
        return justificationFunction;
    }

    @Override
    public <ConstraintJustification_ extends ConstraintJustification> QuadConstraintBuilder<A, B, C, D> justifiedWith(
            QuadFunction<A, B, C, D, ConstraintJustification_> justificationFunction) {
        if (this.justificationFunction != null) {
            throw new IllegalStateException("Justification function already set (" + justificationFunction + ").");
        }
        this.justificationFunction =
                (QuadFunction<A, B, C, D, ConstraintJustification>) Objects.requireNonNull(justificationFunction);
        return this;
    }

}
