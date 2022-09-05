package org.optaplanner.constraint.streams.common.bi;

import java.util.Objects;

import org.optaplanner.constraint.streams.common.ConstraintBuilder;
import org.optaplanner.constraint.streams.common.ScoreImpactType;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.stream.Constraint;
import org.optaplanner.core.api.score.stream.bi.BiTerminator;
import org.optaplanner.core.api.score.stream.uni.UniTerminator;

public final class BiTerminatorImpl<A, B> implements BiTerminator<A, B> {

    private final ConstraintBuilder constraintBuilder;
    private final ScoreImpactType impactType;
    private Score<?> constraintWeight;

    public BiTerminatorImpl(ConstraintBuilder constraintBuilder, ScoreImpactType impactType) {
        this.constraintBuilder = Objects.requireNonNull(constraintBuilder);
        this.impactType = Objects.requireNonNull(impactType);
    }

    @Override
    public BiTerminator<A, B> withWeight(Score<?> constraintWeight) {
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
