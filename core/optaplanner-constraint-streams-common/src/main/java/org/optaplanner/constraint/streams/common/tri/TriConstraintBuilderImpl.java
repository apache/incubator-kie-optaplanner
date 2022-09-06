package org.optaplanner.constraint.streams.common.tri;

import org.optaplanner.constraint.streams.common.AbstractConstraintBuilder;
import org.optaplanner.constraint.streams.common.ConstraintBuilder;
import org.optaplanner.constraint.streams.common.ScoreImpactType;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.stream.tri.TriConstraintBuilder;

public final class TriConstraintBuilderImpl<A, B, C>
        extends AbstractConstraintBuilder<TriConstraintBuilder<A, B, C>>
        implements TriConstraintBuilder<A, B, C> {

    public TriConstraintBuilderImpl(ConstraintBuilder constraintBuilder, ScoreImpactType impactType,
            Score<?> constraintWeight) {
        super(constraintBuilder, impactType, constraintWeight);
    }

}
