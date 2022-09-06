package org.optaplanner.constraint.streams.common.uni;

import org.optaplanner.constraint.streams.common.AbstractTerminator;
import org.optaplanner.constraint.streams.common.ConstraintBuilder;
import org.optaplanner.constraint.streams.common.ScoreImpactType;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.stream.uni.UniTerminator;

public final class UniTerminatorImpl<A>
        extends AbstractTerminator<UniTerminator<A>>
        implements UniTerminator<A> {

    public UniTerminatorImpl(ConstraintBuilder constraintBuilder, ScoreImpactType impactType, Score<?> constraintWeight) {
        super(constraintBuilder, impactType, constraintWeight);
    }

}
