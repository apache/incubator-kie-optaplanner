package org.optaplanner.constraint.streams.common.bi;

import static org.optaplanner.constraint.streams.common.RetrievalSemantics.STANDARD;

import org.optaplanner.constraint.streams.common.RetrievalSemantics;
import org.optaplanner.core.api.score.stream.bi.BiConstraintStream;
import org.optaplanner.core.api.score.stream.tri.TriConstraintStream;
import org.optaplanner.core.api.score.stream.tri.TriJoiner;

public interface InnerBiConstraintStream<A, B> extends BiConstraintStream<A, B> {

    RetrievalSemantics getRetrievalSemantics();

    /**
     * This method will return true if the constraint stream is guaranteed to only produce distinct tuples.
     * See {@link #distinct()} for details.
     *
     * @return true if the guarantee of distinct tuples is provided
     */
    boolean guaranteesDistinct();

    @Override
    default <C> TriConstraintStream<A, B, C> join(Class<C> otherClass, TriJoiner<A, B, C>... joiners) {
        if (getRetrievalSemantics() == STANDARD) {
            return join(getConstraintFactory().forEach(otherClass), joiners);
        } else {
            return join(getConstraintFactory().from(otherClass), joiners);
        }
    }

    @Override
    default BiConstraintStream<A, B> distinct() {
        if (guaranteesDistinct()) {
            return this;
        } else {
            return groupBy((a, b) -> a, (a, b) -> b);
        }
    }

}
