package org.optaplanner.constraint.streams.common.uni;

import java.util.Objects;
import java.util.function.Function;

import org.optaplanner.constraint.streams.common.AbstractConstraintBuilder;
import org.optaplanner.constraint.streams.common.ScoreImpactType;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.stream.ConstraintJustification;
import org.optaplanner.core.api.score.stream.uni.UniConstraintBuilder;

public final class UniConstraintBuilderImpl<A>
        extends AbstractConstraintBuilder<UniConstraintBuilder<A>>
        implements UniConstraintBuilder<A> {

    private Function<A, ConstraintJustification> justificationFunction;

    public UniConstraintBuilderImpl(UniConstraintConstructor<A> constraintConstructor, ScoreImpactType impactType,
            Score<?> constraintWeight) {
        super(constraintConstructor, impactType, constraintWeight);
    }

    @Override
    protected Function<A, ConstraintJustification> getJustificationFunction() {
        if (justificationFunction == null) {
            return null; // Will use the default.
        }
        return justificationFunction;
    }

    @Override
    public <ConstraintJustification_ extends ConstraintJustification> UniConstraintBuilder<A> justifiedWith(
            Function<A, ConstraintJustification_> justificationFunction) {
        if (this.justificationFunction != null) {
            throw new IllegalStateException("Justification function already set (" + justificationFunction + ").");
        }
        this.justificationFunction = (Function<A, ConstraintJustification>) Objects.requireNonNull(justificationFunction);
        return this;
    }

}
