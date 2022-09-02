package org.optaplanner.constraint.streams.common.uni;

import org.optaplanner.constraint.streams.common.ConstraintBuilder;
import org.optaplanner.constraint.streams.common.ScoreImpactType;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.stream.Constraint;
import org.optaplanner.core.api.score.stream.uni.UniTerminator;

import java.util.Objects;

public final class UniTerminatorImpl<A> implements UniTerminator<A> {

    private final ConstraintBuilder constraintBuilder;
    private final ScoreImpactType impactType;
    private Score<?> constraintWeight;

    public UniTerminatorImpl(ConstraintBuilder constraintBuilder, ScoreImpactType impactType) {
        this.constraintBuilder = Objects.requireNonNull(constraintBuilder);
        this.impactType = Objects.requireNonNull(impactType);
    }

    @Override
    public UniTerminator<A> withWeight(Score<?> constraintWeight) {
        this.constraintWeight = Objects.requireNonNull(constraintWeight);
        return this;
    }

    @Override
    public Constraint as(String constraintName) {
        return constraintBuilder.apply(null, constraintName, constraintWeight, impactType);
    }

    @Override
    public Constraint as(String constraintPackage, String constraintName) {
        return constraintBuilder.apply(constraintPackage, constraintName, constraintWeight, impactType);
    }

}
