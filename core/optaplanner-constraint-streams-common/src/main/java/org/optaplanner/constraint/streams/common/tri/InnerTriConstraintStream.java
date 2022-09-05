package org.optaplanner.constraint.streams.common.tri;

import org.optaplanner.constraint.streams.common.RetrievalSemantics;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.stream.Constraint;
import org.optaplanner.core.api.score.stream.quad.QuadConstraintStream;
import org.optaplanner.core.api.score.stream.quad.QuadJoiner;
import org.optaplanner.core.api.score.stream.tri.TriConstraintStream;

public interface InnerTriConstraintStream<A, B, C> extends TriConstraintStream<A, B, C> {

    RetrievalSemantics getRetrievalSemantics();

    /**
     * This method will return true if the constraint stream is guaranteed to only produce distinct tuples.
     * See {@link #distinct()} for details.
     *
     * @return true if the guarantee of distinct tuples is provided
     */
    boolean guaranteesDistinct();

    @Override
    default <D> QuadConstraintStream<A, B, C, D> join(Class<D> otherClass, QuadJoiner<A, B, C, D>... joiners) {
        if (getRetrievalSemantics() == RetrievalSemantics.STANDARD) {
            return join(getConstraintFactory().forEach(otherClass), joiners);
        } else {
            return join(getConstraintFactory().from(otherClass), joiners);
        }
    }

    @Override
    default TriConstraintStream<A, B, C> distinct() {
        if (guaranteesDistinct()) {
            return this;
        } else {
            return groupBy((a, b, c) -> a, (a, b, c) -> b, (a, b, c) -> c);
        }
    }

    @Override
    default Constraint penalize(String constraintName, Score<?> constraintWeight) {
        return penalize(constraintWeight)
                .as(constraintName);
    }

    @Override
    default Constraint penalize(String constraintPackage, String constraintName, Score<?> constraintWeight) {
        return penalize(constraintWeight)
                .as(constraintPackage, constraintName);
    }

    @Override
    default Constraint penalizeConfigurable(String constraintName) {
        return penalize()
                .as(constraintName);
    }

    @Override
    default Constraint penalizeConfigurable(String constraintPackage, String constraintName) {
        return penalize()
                .as(constraintPackage, constraintName);
    }

    @Override
    default Constraint reward(String constraintName, Score<?> constraintWeight) {
        return reward(constraintWeight)
                .as(constraintName);
    }

    @Override
    default Constraint reward(String constraintPackage, String constraintName, Score<?> constraintWeight) {
        return reward(constraintWeight)
                .as(constraintPackage, constraintName);
    }

    @Override
    default Constraint rewardConfigurable(String constraintName) {
        return reward()
                .as(constraintName);
    }

    @Override
    default Constraint rewardConfigurable(String constraintPackage, String constraintName) {
        return penalize()
                .as(constraintPackage, constraintName);
    }

    @Override
    default Constraint impact(String constraintName, Score<?> constraintWeight) {
        return impact(constraintWeight)
                .as(constraintName);
    }

    @Override
    default Constraint impact(String constraintPackage, String constraintName, Score<?> constraintWeight) {
        return impact(constraintWeight)
                .as(constraintPackage, constraintName);
    }

}
