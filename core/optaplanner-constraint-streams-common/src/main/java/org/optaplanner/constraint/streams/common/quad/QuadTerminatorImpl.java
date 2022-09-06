package org.optaplanner.constraint.streams.common.quad;

import org.optaplanner.constraint.streams.common.AbstractTerminator;
import org.optaplanner.constraint.streams.common.ConstraintBuilder;
import org.optaplanner.constraint.streams.common.ScoreImpactType;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.stream.quad.QuadTerminator;

public final class QuadTerminatorImpl<A, B, C, D>
        extends AbstractTerminator<QuadTerminator<A, B, C, D>>
        implements QuadTerminator<A, B, C, D> {

    public QuadTerminatorImpl(ConstraintBuilder constraintBuilder, ScoreImpactType impactType, Score<?> constraintWeight) {
        super(constraintBuilder, impactType, constraintWeight);
    }

}
