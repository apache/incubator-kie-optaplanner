package org.optaplanner.constraint.streams.common.tri;

import java.util.Objects;

import org.optaplanner.constraint.streams.common.AbstractConstraintBuilder;
import org.optaplanner.constraint.streams.common.ScoreImpactType;
import org.optaplanner.core.api.function.TriFunction;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.stream.ConstraintJustification;
import org.optaplanner.core.api.score.stream.tri.TriConstraintBuilder;

public final class TriConstraintBuilderImpl<A, B, C>
        extends AbstractConstraintBuilder<TriConstraintBuilder<A, B, C>>
        implements TriConstraintBuilder<A, B, C> {

    private TriFunction<A, B, C, ConstraintJustification> justificationFunction;

    public TriConstraintBuilderImpl(TriConstraintConstructor<A, B, C> constraintConstructor, ScoreImpactType impactType,
            Score<?> constraintWeight) {
        super(constraintConstructor, impactType, constraintWeight);
    }

    @Override
    protected TriFunction<A, B, C, ConstraintJustification> getJustificationFunction() {
        if (justificationFunction == null) {
            return null; // Will use the default.
        }
        return justificationFunction;
    }

    @Override
    public <ConstraintJustification_ extends ConstraintJustification> TriConstraintBuilder<A, B, C> justifyWith(
            TriFunction<A, B, C, ConstraintJustification_> justificationMapping) {
        if (this.justificationFunction != null) {
            throw new IllegalStateException("Justification function already set (" + justificationMapping + ").");
        }
        this.justificationFunction =
                (TriFunction<A, B, C, ConstraintJustification>) Objects.requireNonNull(justificationMapping);
        return this;
    }

}
