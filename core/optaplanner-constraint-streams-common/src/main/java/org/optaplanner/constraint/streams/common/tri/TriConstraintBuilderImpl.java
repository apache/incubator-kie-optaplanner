package org.optaplanner.constraint.streams.common.tri;

import java.util.Collection;
import java.util.Objects;

import org.optaplanner.constraint.streams.common.AbstractConstraintBuilder;
import org.optaplanner.constraint.streams.common.ScoreImpactType;
import org.optaplanner.core.api.function.TriFunction;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.stream.tri.TriConstraintBuilder;

public final class TriConstraintBuilderImpl<A, B, C>
        extends AbstractConstraintBuilder<TriConstraintBuilder<A, B, C>>
        implements TriConstraintBuilder<A, B, C> {

    private TriFunction<A, B, C, Object> justificationFunction;

    public TriConstraintBuilderImpl(TriConstraintConstructor<A, B, C> constraintConstructor, ScoreImpactType impactType,
            Score<?> constraintWeight) {
        super(constraintConstructor, impactType, constraintWeight);
    }

    @Override
    protected TriFunction<A, B, C, Object> getJustificationFunction() {
        if (justificationFunction == null) {
            return null; // Will use the default.
        }
        return (a, b, c) -> {
            Object justification = justificationFunction.apply(a, b, c);
            if (justification instanceof Collection) {
                throw new IllegalStateException("Justification function returned a collection (" + justification + ").");
            }
            return justification;
        };
    }

    @Override
    public TriConstraintBuilder<A, B, C> justifiedWith(TriFunction<A, B, C, Object> justificationFunction) {
        if (this.justificationFunction != null) {
            throw new IllegalStateException("Justification function already set (" + justificationFunction + ").");
        }
        this.justificationFunction = Objects.requireNonNull(justificationFunction);
        return this;
    }

}
