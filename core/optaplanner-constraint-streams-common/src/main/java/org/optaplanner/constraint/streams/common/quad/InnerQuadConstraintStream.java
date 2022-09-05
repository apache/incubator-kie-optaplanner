package org.optaplanner.constraint.streams.common.quad;

import org.optaplanner.core.api.score.stream.quad.QuadConstraintStream;

public interface InnerQuadConstraintStream<A, B, C, D> extends QuadConstraintStream<A, B, C, D> {

    /**
     * This method will return true if the constraint stream is guaranteed to only produce distinct tuples.
     * See {@link #distinct()} for details.
     *
     * @return true if the guarantee of distinct tuples is provided
     */
    boolean guaranteesDistinct();

    @Override
    default QuadConstraintStream<A, B, C, D> distinct() {
        if (guaranteesDistinct()) {
            return this;
        } else {
            return groupBy((a, b, c, d) -> a, (a, b, c, d) -> b, (a, b, c, d) -> c, (a, b, c, d) -> d);
        }
    }

}
