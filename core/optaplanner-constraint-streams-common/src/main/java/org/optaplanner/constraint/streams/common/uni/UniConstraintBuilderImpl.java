package org.optaplanner.constraint.streams.common.uni;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;

import org.optaplanner.constraint.streams.common.AbstractConstraintBuilder;
import org.optaplanner.constraint.streams.common.ScoreImpactType;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.stream.uni.UniConstraintBuilder;

public final class UniConstraintBuilderImpl<A>
        extends AbstractConstraintBuilder<UniConstraintBuilder<A>>
        implements UniConstraintBuilder<A> {

    private Function<A, Object> justificationFunction;

    public UniConstraintBuilderImpl(UniConstraintConstructor<A> constraintConstructor, ScoreImpactType impactType,
            Score<?> constraintWeight) {
        super(constraintConstructor, impactType, constraintWeight);
    }

    @Override
    protected Function<A, Object> getJustificationFunction() {
        if (justificationFunction == null) {
            return null; // Will use the default.
        }
        return a -> {
            Object justification = justificationFunction.apply(a);
            if (justification instanceof Collection) {
                throw new IllegalStateException("Justification function returned a collection (" + justification + ").");
            }
            return justification;
        };
    }

    @Override
    public UniConstraintBuilder<A> justifiedWith(Function<A, Object> justificationFunction) {
        if (this.justificationFunction != null) {
            throw new IllegalStateException("Justification function already set (" + justificationFunction + ").");
        }
        this.justificationFunction = Objects.requireNonNull(justificationFunction);
        return this;
    }

}
